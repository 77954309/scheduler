package com.lm.scheduler.listener

import java.util.concurrent.{CopyOnWriteArrayList, Future, TimeoutException}
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong}

import com.lm.scheduler.collection.BlockingLoopArray
import com.lm.scheduler.utils.{ByteTimeUtils, Logging, Utils}
import org.apache.commons.lang.time.DateFormatUtils

import scala.util.control.NonFatal

/**
 * @Classname ListenerBus
 * @Description TODO
 * @Date 2020/9/21 16:59
 * @Created by limeng
 */
private[lm] trait ListenerBus[L<:EventListener,E<:Event] extends Logging{
  val self = this

  private val listeners = new CopyOnWriteArrayList[L]

  final def addListener(listener:L): Unit ={
    listeners.add(listener)
    info(toString + " add a new listener => " + listener.getClass)
  }

  final def removeListener(listener:L): Unit ={
    listeners.remove(listener)
  }

  final def postToAll(event: E): Unit = {
      val iter = listeners.iterator()
      while (iter.hasNext){
        val listener = iter.next()
        Utils.tryCatch{
          doPostEvent(listener, event)
        }{
          case NonFatal(e)=>Utils.tryAndError(listener.onEventError(event,e))
          case t: Throwable => throw t
        }
      }
  }

  protected def doPostEvent(listener: L, event: E): Unit



}

 abstract class  ListenerEventBus[L <: EventListener,E <: Event] (val eventQueueCapacity: Int, name: String)
                                                                            (listenerConsumerThreadSize: Int = 5, listenerThreadMaxFreeTime: Long = ByteTimeUtils.timeStringAsMs("2m")) extends ListenerBus[L,E] with Logging{


  private lazy val eventQueue = new BlockingLoopArray[E](eventQueueCapacity)
  protected val executorService = Utils.newCachedThreadPool(listenerConsumerThreadSize + 2, name + "-Consumer-ThreadPool", true)
  private val eventDealThreads = Array.tabulate(listenerConsumerThreadSize)(new ListenerEventThread(_))
  private val started = new AtomicBoolean(false)
  private val stopped = new AtomicBoolean(false)

   private var listenerThread: Future[_] = _

   def start(): Unit ={
     if(started.compareAndSet(false,true)){
       listenerThread = executorService.submit(new Runnable {
         override def run(): Unit = {
           while (!stopped.get){
             val event = Utils.tryCatch(eventQueue.take()){
               case t: InterruptedException => info(s"stopped $name thread.")
                 return
             }
             while(!eventDealThreads.exists(_.putEvent(event)) && !stopped.get) Utils.tryAndError(Thread.sleep(1))
           }
         }
       })
     }else{
       throw new IllegalStateException(s"$name already started!")
     }
   }


   protected val dropEvent: DropEvent = new IgnoreDropEvent

   private def queueIsEmpty: Boolean = synchronized { !eventQueue.nonEmpty && !eventDealThreads.exists(_.isRunning) }

   def listenerThreadIsAlive: Boolean = !listenerThread.isDone

   def post(event: E): Unit = {
     if (stopped.get || executorService.isTerminated || (listenerThread.isDone && started.get())) {
       dropEvent.onBusStopped(event)
     } else if(!eventQueue.offer(event)) {
       dropEvent.onDropEvent(event)
     }
   }

   @throws(classOf[TimeoutException])
   def waitUntilEmpty(timeoutMillis: Long): Unit = {
     val finishTime = System.currentTimeMillis + timeoutMillis
     while (!queueIsEmpty) {
       if (System.currentTimeMillis > finishTime) {
         throw new TimeoutException(
           s"The event queue is not empty after $timeoutMillis milliseconds")
       }
       Thread.sleep(10)
     }
   }

   def stop(): Unit ={
     if (!started.get()) {
       throw new IllegalStateException(s"Attempted to stop $name that has not yet started!")
     }

     if(stopped.compareAndSet(false, true)){
       info(s"try to stop $name thread.")
       listenerThread.cancel(true)
       eventDealThreads.foreach(_.shutdown())
     }else{

     }
   }


   trait DropEvent{
     def onDropEvent(event: E): Unit
     def onBusStopped(event: E): Unit
   }

   class IgnoreDropEvent extends DropEvent{
     private val droppedEventsCounter = new AtomicLong(0L)
     @volatile private var lastReportTimestamp = 0L

     private val logDroppedEvent = new AtomicBoolean(false)
     private val logStoppedEvent = new AtomicBoolean(false)

     executorService.submit(new Runnable {
       override def run(): Unit = {
         while (true){
           val droppedEvents = droppedEventsCounter.get
           if(droppedEvents > 0){
             if (System.currentTimeMillis() - lastReportTimestamp >= 60 * 1000) {
               if (droppedEventsCounter.compareAndSet(droppedEvents, 0)) {
                 val prevLastReportTimestamp = lastReportTimestamp
                 lastReportTimestamp = System.currentTimeMillis()
                 warn(s"Dropped $droppedEvents ListenerEvents since " +
                   DateFormatUtils.format(prevLastReportTimestamp, "yyyy-MM-dd HH:mm:ss"))
               }
             }
           }
         }
       }
     })

     override def onDropEvent(event: E): Unit = {
       droppedEventsCounter.incrementAndGet()
       if (logDroppedEvent.compareAndSet(false, true)) {
         error("Dropping ListenerEvent because no remaining room in event queue. " +
           "This likely means one of the Listeners is too slow and cannot keep up with " +
           "the rate at which tasks are being started by the scheduler.")
       }
     }
     override def onBusStopped(event: E): Unit = {
       droppedEventsCounter.incrementAndGet()
       if (logStoppedEvent.compareAndSet(false, true)) {
         error(s"$name has already stopped! Dropping event $event.")
       }
     }
   }



  protected class ListenerEventThread(index: Int) extends Runnable {
    private var future:Option[Future[_]] = None
    private var continue = true
    private var event:Option[E] = None
    private var lastEventDealData = 0L

    def releaseFreeThread(): Unit = if(listenerThreadMaxFreeTime > 0 && future.isDefined && event.isEmpty
      && lastEventDealData > 0 && (System.currentTimeMillis() - lastEventDealData >= listenerThreadMaxFreeTime)) synchronized {
      if(lastEventDealData == 0 && future.isEmpty) return
      lastEventDealData = 0L
      continue = false
      future.foreach(_.cancel(true))
      future = None
    }

    override def run(): Unit = {
      val threadName = Thread.currentThread().getName
      val currentThreadName =  s"$name-Thread-$index"
      Thread.currentThread().setName(currentThreadName)
      info(s"$currentThreadName begin.")
      def threadRelease(): Unit={
        info(s"$currentThreadName released.")
        Thread.currentThread().setName(threadName)
      }

      while(continue) {
        synchronized{
          while (event.isEmpty) Utils.tryQuietly(wait(),_=>{
            threadRelease()
            return
          })
        }
        Utils.tryFinally(event.foreach(postToAll)) (synchronized {
          lastEventDealData = System.currentTimeMillis()
          event = None
        })
      }
      threadRelease()
    }

    def isRunning: Boolean = event.isDefined

    def putEvent(event: E): Boolean = if(this.event.nonEmpty) false else synchronized{
      if(this.event.isDefined) false
      else{
        lastEventDealData = System.currentTimeMillis()
        this.event = Some(event)
        if(future.isEmpty) future = Some(executorService.submit(this))
        else notify()
        true
      }
    }

    def shutdown(): Unit = {
      continue = false
      future.foreach(_.cancel(true))
    }

  }



}
