package com.lm.scheduler.queue

import com.lm.scheduler.exception.SchedulerErrorException
import com.lm.scheduler.queue.SchedulerEventState._
import com.lm.scheduler.utils.Logging

/**
 * @Classname SchedulerEvent
 * @Description TODO
 * @Date 2020/9/23 19:41
 * @Created by limeng
 */
trait SchedulerEvent extends Logging{

  private[queue] var id: String = _
  private var state: SchedulerEventState = Inited
  val createTime = System.currentTimeMillis

  protected var scheduledTime: Long = 0l
  protected var startTime: Long = 0l
  protected var endTime: Long = 0l

  def getEndTime:Long = endTime
  def getStartTime:Long = startTime
  def getScheduledTime:Long = scheduledTime

  def getId:String = id

  def setId(id:String): Unit ={
    this.id = id
    this synchronized notify()
  }

  def pause(): Unit
  def resume(): Unit

  def isWaiting = state == Inited
  def isScheduled = state == Scheduled
  def isRunning = state == Running

  def isCompleted = SchedulerEventState.isCompleted(state)
  def isSucceed: Boolean = SchedulerEventState.isSucceed(state)
  def isWaitForRetry: Boolean = state == WaitForRetry


  def getState = state

  def afterStateChanged(fromState: SchedulerEventState, toState: SchedulerEventState): Unit

  protected def transition(state: SchedulerEventState): Unit = synchronized {
    if(state.id < this.state.id && state != WaitForRetry)
      throw new SchedulerErrorException(12000, s"Task status flip error! Cause: Failed to flip from ${this.state} to $state.（任务状态翻转出错！原因：不允许从${this.state} 翻转为$state.）")//抛异常
    info(s"$toString change state ${this.state} => $state.")
    val oldState = this.state
    this.state = state
    afterStateChanged(oldState, state)
  }

  def turnToScheduled():Boolean = if(!isWaiting) false else  this synchronized{
    if(!isWaiting) false else {
      scheduledTime = System.currentTimeMillis
      while(id == null) wait(100)
      transition(Scheduled)
      true
    }
  }

}
