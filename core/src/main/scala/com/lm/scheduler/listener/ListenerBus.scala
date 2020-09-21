package com.lm.scheduler.listener

import java.util.concurrent.CopyOnWriteArrayList
import java.util.logging.Logger

import com.lm.scheduler.utils.{ByteTimeUtils, Logging, Utils}

import scala.util.control.NonFatal

/**
 * @Classname ListenerBus
 * @Description TODO
 * @Date 2020/9/21 16:59
 * @Created by limeng
 */
private[lm] trait ListenerBus[L<:EventListener,E<:Event] extends Logger{
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

}
