package com.lm.scheduler.queue.fifoqueue

import java.util.concurrent.{ExecutorService, Future}

import com.lm.scheduler.SchedulerContext
import com.lm.scheduler.exception.{ErrorException, SchedulerErrorException, WarnException}
import com.lm.scheduler.executor.Executor
import com.lm.scheduler.future.BDPFutureTask
import com.lm.scheduler.queue.{ConsumeQueue, Consumer, Group, Job, SchedulerEvent}
import com.lm.scheduler.utils.Utils

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.TimeoutException

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
      val waitForRetryJobs = runningJobs.filter(job => job != null && job.isJobCanRetry)
      waitForRetryJobs.find{
        job =>
          isRetryJob = Utils.tryCatch(job.turnToRetry()){t=>
            job.onFailure("Job state flipped to Scheduled failed in Retry(Retry时，job状态翻转为Scheduled失败)！", t)
            false
          }
          isRetryJob
      }
    }
    while (event.isEmpty){
      val takeEvent = if(getRunningEvents.isEmpty) Option(queue.take()) else queue.take(3000)
      event = if(takeEvent.exists(e=>Utils.tryCatch(e.turnToScheduled()){
        t=>
          takeEvent.get.asInstanceOf[Job].onFailure("Job state flipped to Scheduled failed(Job状态翻转为Scheduled失败)！", t)
          false
      })) takeEvent else getWaitForRetryEvent
    }
    event.foreach{case job:Job =>{
      Utils.tryCatch{
        val (totalDuration, askDuration) = (fifoGroup.getMaxAskExecutorDuration,fifoGroup.getAskExecutorInterval)
        var  executor: Option[Executor] = None
        job.consumerFuture  = new BDPFutureTask(this.future)
        Utils.waitUntil(() =>{
          executor = Utils.tryCatch(schedulerContext.getOrCreateExecutorManager.askExecutor(job,askDuration)) {
            case warn: WarnException =>
              job.getLogListener.foreach(_.onLogUpdate(job, warn.getDesc))
              None
            case e:ErrorException =>
              job.getLogListener.foreach(_.onLogUpdate(job, e.getDesc))
              throw e
            case error: Throwable =>
              throw error
          }
          Utils.tryQuietly(askExecutorGap())
          executor.isDefined
        },totalDuration)

        job.consumerFuture = null
        executor.foreach{executor=>
          job.setExecutor(executor)
          job.future = executeService.submit(job)
          job.getJobDaemon.foreach(jobDaemon => jobDaemon.future = executeService.submit(jobDaemon))
          if(!isRetryJob) putToRunningJobs(job)
        }

      }{
        case _: TimeoutException =>
          warn(s"Ask executor for Job $job timeout!")
          job.onFailure("The request engine times out and the cluster cannot provide enough resources(请求引擎超时，集群不能提供足够的资源).",
            new SchedulerErrorException(11055, "Insufficient resources, requesting available engine timeout(资源不足，请求可用引擎超时)！"))
        case error: Throwable =>
          job.onFailure("Request engine failed, possibly due to insufficient resources or background process error(请求引擎失败，可能是由于资源不足或后台进程错误)!", error)
          if(job.isWaitForRetry) {
            warn(s"Ask executor for Job $job failed, wait for the next retry!", error)
            if(!isRetryJob)  putToRunningJobs(job)
          } else warn(s"Ask executor for Job $job failed!", error)
      }
    }}

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
