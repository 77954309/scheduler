package com.lm.scheduler.queue

import java.io.Closeable
import java.util.concurrent.Future

import com.lm.protocol.engine.JobProgressInfo
import com.lm.scheduler.exception.{DWCJobRetryException, DWCRetryException, ErrorException}
import com.lm.scheduler.executor.{AsynReturnExecuteResponse, CompletedExecuteResponse, ConcurrentTaskInfoSupport, ConcurrentTaskOperateSupport, ErrorExecuteResponse, ExecuteRequest, Executor, IncompleteExecuteResponse, SingleTaskInfoSupport, SingleTaskOperateSupport, SuccessExecuteResponse}
import com.lm.scheduler.future.BDPFuture
import com.lm.scheduler.listener.{JobListener, ListenerEventBus, LogListener, ProgressListener, SchedulerListener}
import com.lm.scheduler.utils.{LogUtils, Logging, Utils}
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.exception.ExceptionUtils

/**
 * @Classname Job
 * @Description TODO
 * @Date 2020/9/23 20:00
 * @Created by limeng
 */
abstract class Job extends  Runnable with SchedulerEvent with Closeable with Logging{
  import SchedulerEventState._
  private[queue] var future: Future[_] = _

  private[queue] var consumerFuture: BDPFuture = _

  protected var jobDaemon: Option[JobDaemon] = None

  private var eventListenerBus:ListenerEventBus[_ <: SchedulerListener,_ <: SchedulerEvent] = _

  private var executor:Executor = _

  private var jobListener:Option[JobListener] = None

  private var logListener:Option[LogListener] = None

  private var progressListener: Option[ProgressListener] = None

  private var interrupt = false

  private var progress: Float = 0f

  private var retryNum = 0

  def getName: String

  def getJobInfo:JobInfo

  def getErrorResponse = errorExecuteResponse

  def init():Unit

  protected def jobToExecuteRequest:ExecuteRequest

  private var errorExecuteResponse: ErrorExecuteResponse = _

  override def isWaiting = super.isWaiting && !interrupt
  override def isCompleted = super.isCompleted || interrupt

  def setListenerEventBus(eventListenerBus:ListenerEventBus[_ <: SchedulerListener,_ <: SchedulerEvent]):Unit ={
    this.eventListenerBus = eventListenerBus
  }

  def setExecutor(executor: Executor):Unit = this.executor = executor

  protected def getExecutor = executor

  def setJobListener(jobListener: JobListener):Unit = this.jobListener = Some(jobListener)

  def setLogListener(logListener: LogListener):Unit = this.logListener = Some(logListener)

  def getLogListener:Option[LogListener] = logListener

  def setProgressListener(progressListener: ProgressListener):Unit = this.progressListener = Some(progressListener)

  def getProgressListener:Option[ProgressListener] = progressListener

  def getProgress:Float = progress

  def setProgress(progress:Float):Unit = this.progress = progress

  def kill(): Unit = onFailure("Job is killed by user!", null)


  private [queue] def getJobDaemon ={
    if(!existsJobDaemon) None else {
      if(jobDaemon.isEmpty) synchronized {
        if(jobDaemon.isEmpty) jobDaemon = Some(createJobDaemon)
      }
      jobDaemon
    }
  }

  override def cancel(): Unit = kill()

  override def pause(): Unit = if(executor != null) executor match  {
    case s: SingleTaskOperateSupport => s.pause()
    case c: ConcurrentTaskOperateSupport => c.pause(getId)
    case _ =>
  }

  override def resume():Unit = if(executor != null) executor match {
    case s: SingleTaskOperateSupport => s.resume()
    case c: ConcurrentTaskOperateSupport => c.resume(getId)
    case _ =>
  }

  protected def existsJobDaemon: Boolean = false

  protected def createJobDaemon: JobDaemon = new JobDaemon(this, jobDaemonUpdateInterval, executor)

  protected def jobDaemonUpdateInterval: Long = 1000L


  private def killByExecutor():Unit = if(executor != null) executor match {
    case s:SingleTaskOperateSupport => s.kill()
    case c: ConcurrentTaskOperateSupport => c.kill(getId)
    case _=>
  }

  def onFailure(errorMsg: String, t: Throwable): Unit = if(!SchedulerEventState.isCompleted(getState)){
    info(s"job $toString is onFailure on state $getState with errorMsg: $errorMsg.")
    Utils.tryAndWarn{
      logListener.foreach(_.onLogUpdate(this, LogUtils.generateERROR(errorMsg)))
      if(t != null){
        logListener.foreach(_.onLogUpdate(this,LogUtils.generateERROR(ExceptionUtils.getFullStackTrace(t))))
      }
    }
    errorExecuteResponse = ErrorExecuteResponse(errorMsg,t)

    jobDaemon.foreach(_.kill())
    interrupt = true
    if(future != null && !SchedulerEventState.isCompleted(getState)){
      Utils.tryCatch(killByExecutor()){
        t:Throwable => logger.error(s"kill job $getName failed", t)
          val s = new ErrorException(23333,s"kill job $getName failed")
          s.initCause(t)
          forceCancel(s)
      }
      future.cancel(true)
    }

    if(consumerFuture != null && executor == null){
      warn(s"This executor of job($toString) in starting status,When kill job need to interrupter consumer Future")
      this.consumerFuture.cancel()
      this.consumerFuture = null
    }
    if(super.isWaiting || super.isScheduled) transitionCompleted(errorExecuteResponse)
    info(s"$toString execute failed. Reason: $errorMsg.",t)
  }

  /**
   *一些Job调用kill之后，不能正确kill,导致状态不能翻转
   * @param t
   */
  private def forceCancel(t:Throwable):Unit = {
    logger.info(s"force to cancel job $getName")
    val executeCompleted = ErrorExecuteResponse("force to transition Failed",t)
    transitionCompleted(executeCompleted)
  }

  protected def transitionCompleted(executeCompleted: CompletedExecuteResponse): Unit ={
    val state = getState
    executeCompleted match {
      case _:SuccessExecuteResponse =>
        if(!interrupt) Utils.tryAndWarnMsg(transition(Succeed))(s"update Job $toString from $state to Succeed failed.")
        else transitionCompleted(errorExecuteResponse)
      case e:ErrorExecuteResponse =>
        Utils.tryCatch()

    }
  }

  override def afterStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit = toState match {
    case Inited => jobListener.foreach(_.onJobInited(this))
    case Scheduled => jobListener.foreach(_.onJobScheduled(this))
    case Running =>  jobListener.foreach(_.onJobRunning(this))
    case WaitForRetry => jobListener.foreach(_.onJobWaitForRetry(this))
    case _=>
      jobDaemon.foreach(_.kill())
      jobListener.foreach(_.onJobCompleted(this))
  }

  def isJobSupportRetry: Boolean = true
  def getRetryNum = retryNum
  protected def getMaxRetryNum: Int = 2

  protected def isJobShouldRetry(errorExecuteResponse: ErrorExecuteResponse):Boolean = {
    isJobSupportRetry && errorExecuteResponse != null && (errorExecuteResponse.t match {
      case t:DWCRetryException =>
        warn(s"Job $toString is desired to retry.", t)
        t.getErrCode == DWCJobRetryException.JOB_RETRY_ERROR_CODE
      case _=>false
    })
  }

  final def isJobCanRetry:Boolean = if(!isJobSupportRetry || getState != WaitForRetry) false else synchronized {
    if(getState == WaitForRetry && (getMaxRetryNum < 1 || retryNum < getMaxRetryNum)) true
    else if(WaitForRetry == getState && getMaxRetryNum > 0 && retryNum >= getMaxRetryNum ){
      logListener.foreach(_.onLogUpdate(this,  LogUtils.generateInfo( s"Job cancelled since reached maxRetryNum $getMaxRetryNum.")))
      transition(Failed)
      false
    }else false
  }

  final def turnToRetry():Boolean = if(!isJobSupportRetry || getState != WaitForRetry) false else synchronized (Utils.tryThrow{
    if(isJobCanRetry){
      transition(Scheduled)
      retryNum += 1
      true
    }else false
  }{
    t=>
      retryNum += 1
      t
  })


  override def run(): Unit = {
    if(!isScheduled || interrupt) return
    startTime = System.currentTimeMillis()
    Utils.tryAndWarn(transition(Running))
    if(interrupt){
      endTime = System.currentTimeMillis()
      transition(Cancelled)
      close()
      return
    }

    val rs =Utils.tryCatch(executor.execute(jobToExecuteRequest)){
      case t: InterruptedException =>
        warn(s"job $toString is interrupted by user!", t)
        ErrorExecuteResponse("job is interrupted by user!", t)
      case t=>
        warn(s"execute job $toString failed!", t)
        ErrorExecuteResponse("execute job failed!", t)
    }
    rs match {
      case r: CompletedExecuteResponse =>
        transitionCompleted(r)
      case r: IncompleteExecuteResponse =>
        transitionCompleted(ErrorExecuteResponse(if(StringUtils.isNotEmpty(r.message)) r.message else "incomplete code.", null))
      case r: AsynReturnExecuteResponse =>
        r.notify(r1 => {
          val realRS = if(interrupt) errorExecuteResponse else r1 match {
            case r: IncompleteExecuteResponse =>
              ErrorExecuteResponse(if(StringUtils.isNotEmpty(r.message)) r.message else "incomplete code.", null)
            case r: CompletedExecuteResponse => r
          }
          transitionCompleted(realRS)
        })
    }
  }

  override def toString: String = if(StringUtils.isNotBlank(getName)) getName else getId
}

/**
 * 主要用于获取状态和日志，如果Executor做不到直接通知ProgressListener和LogListener，则Consumer在提交一个Job时，
 * 必须同时提交一个JobDaemon，确保进度和日志能及时通知出去
 * @param job 需要监听的Job
 * @param listenerUpdateIntervalMs 监听的间隔
 * @param executor 对应的执行器
 */
class JobDaemon(job:Job,listenerUpdateIntervalMs: Long, executor: Executor) extends Runnable with Logging{
  private var terminate = false
  private[queue] var future: Future[_] = _
  private var lastProgress = 0f
  protected def getProgress:(Float,Array[JobProgressInfo]) = executor match {
    case s: SingleTaskInfoSupport => (s.progress(), s.getProgressInfo)
    case c: ConcurrentTaskInfoSupport => (c.progress(job.getId), c.getProgressInfo(job.getId))
    case _ => (0, null)
  }

  protected def getLog: String = executor match {
    case s: SingleTaskInfoSupport => s.log()
    case c: ConcurrentTaskInfoSupport => c.log(job.getId)
    case _ => ""
  }

  override def run(): Unit = {
    if(listenerUpdateIntervalMs < 10) return
    executor match {
      case _: SingleTaskInfoSupport =>
      case _: ConcurrentTaskInfoSupport =>
      case _ => return
    }

  }

  def kill(): Unit ={
    terminate = true
    if(future != null && !future.isDone) future.cancel(true)
  }
}