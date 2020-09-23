package com.lm.scheduler.queue

/**
 * @Classname SchedulerEventState
 * @Description TODO
 * @Date 2020/9/23 19:41
 * @Created by limeng
 */
object SchedulerEventState extends Enumeration {
  type SchedulerEventState = Value
  val Inited, WaitForRetry, Scheduled, Running, Succeed, Failed, Cancelled, Timeout = Value

  def isRunning(jobState: SchedulerEventState) = jobState == Running

  def isScheduled(jobState: SchedulerEventState) = jobState != Inited


  def isCompleted(jobState: SchedulerEventState) = jobState match {
    case Inited | Scheduled | Running | WaitForRetry => false
    case _ => true
  }


  def isSucceed(jobState: SchedulerEventState) = jobState == Succeed
}
