package com.lm.scheduler.queue.fifoqueue

import java.util.concurrent.TimeUnit

import com.lm.scheduler.queue.{AbstractGroup, SchedulerEvent}

import scala.concurrent.duration.{Duration, FiniteDuration}

/**
 * @Classname FIFOGroup
 * @Description TODO
 * @Date 2020/9/27 17:08
 * @Created by limeng
 */
class FIFOGroup(groupName: String, initCapacity: Int, maxCapacity: Int) extends AbstractGroup {

  private var maxAskInterval = 30000L
  private var minAskInterval = 10000L

  def getMaxAskInterval:Long = maxAskInterval

  def setMaxAskInterval(maxAskInterval:Long):Unit = this.maxAskInterval = maxAskInterval

  def getMinAskInterval:Long = minAskInterval

  def setMinAskInterval(minAskInterval:Long):Unit = this.minAskInterval = minAskInterval


  def getMaxAskExecutorDuration = if(getMaxAskExecutorTimes <= 0 ) Duration.Inf else Duration(getMaxAskExecutorTimes,TimeUnit.MILLISECONDS)

  def getAskExecutorInterval:FiniteDuration = if(getMaxAskExecutorTimes <= 0) Duration(maxAskInterval,TimeUnit.MILLISECONDS)
  else if(getMaxAskExecutorTimes > maxAskInterval) Duration(math.min(math.max(getMaxAskExecutorTimes / 10,minAskInterval),maxAskInterval), TimeUnit.MILLISECONDS)
  else if(getMaxAskExecutorTimes > minAskInterval) Duration(minAskInterval, TimeUnit.MILLISECONDS)
  else Duration(getMaxAskExecutorTimes, TimeUnit.MILLISECONDS)

  override def getGroupName: String = groupName

  //等待Job占整个ConsumeQueue的百分比
  override def getInitCapacity: Int = initCapacity

  //等待的Job占整个ConsumeQueue的最大百分比
  override def getMaximumCapacity: Int = maxCapacity

  override def belongTo(event: SchedulerEvent): Boolean = true
}
