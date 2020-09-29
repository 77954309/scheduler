package com.lm.scheduler.queue.fifoqueue

import java.util.concurrent.{ExecutorService, Future}

import com.lm.scheduler.SchedulerContext
import com.lm.scheduler.queue.{ConsumeQueue, Consumer, Group, Job, SchedulerEvent}
import com.lm.scheduler.utils.Utils

import scala.collection.mutable.ArrayBuffer

/**
 * @Classname FIFOUserConsumer
 * @Description TODO
 * @Date 2020/9/29 16:12
 * @Created by limeng
 */
class FIFOUserConsumer(schedulerContext: SchedulerContext,
                       executeService: ExecutorService, private var group: Group) extends Consumer(schedulerContext,executeService) {
  private var fifoGroup = group.asInstanceOf[FIFOGroup]
  private var queue:ConsumeQueue = _
  private val maxRunningJobsNum = fifoGroup.getMaxRunningJobs
  private val runningJobs = new Array[Job](maxRunningJobsNum)
  private var future: Future[_] = _

  def this(schedulerContext: SchedulerContext,executorService: ExecutorService){
    this(schedulerContext,executorService,null)
  }


  override def setConsumeQueue(consumeQueue: ConsumeQueue): Unit = {
    queue = consumeQueue
  }

  private def getEvents(op:SchedulerEvent => Boolean):Array[SchedulerEvent] = {
    val result =new ArrayBuffer[SchedulerEvent]()
    runningJobs.filter(_ != null).filter(x => op(x)).foreach(result += _)
    result.toArray
  }

  override def getConsumeQueue: ConsumeQueue = queue

  override def getGroup: Group = fifoGroup

  override def setGroup(group: Group): Unit = {
    this.fifoGroup = group.asInstanceOf[FIFOGroup]
  }
  override def getRunningEvents: Array[SchedulerEvent] = getEvents(_.isRunning)

  override def start(): Unit = future = executeService.submit(this)

  override def run(): Unit = {
    Thread.currentThread().setName(s"${toString}Thread")
    info(s"$toString thread started!")
    while (!terminate){
      Utils.tryAndError(loop())
      Utils.tryAndError(Thread.sleep(10))
    }
    info(s"$toString thread stopped!")
  }

  protected def askExecutorGap():Unit = {}

  protected def loop(): Unit ={
    val completedNums = runningJobs.filter(e=> e == null || e.isCompleted)
    if(completedNums.length < 1){
      Utils.tryQuietly(Thread.sleep(1000))
      return
    }
    var isRetryJob = false
    var event:Option[SchedulerEvent] = None
    def getWaitForRetryEvent:Option[SchedulerEvent] = {

    }


  }

  private def putToRunningJobs(job:Job):Unit ={
    val index = runningJobs.indexWhere(f=> f == null || f.isCompleted)
    runningJobs(index) = job
  }

  override def shutdown(): Unit = {
    future.cancel(true)
    super.shutdown()
  }
}
