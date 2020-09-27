package com.lm.scheduler.queue

import com.lm.scheduler.queue.GroupStatus.GroupStatus

/**
 * @Classname AbstractGroup
 * @Description TODO
 * @Date 2020/9/25 17:23
 * @Created by limeng
 */
abstract class AbstractGroup extends Group {
  private var _status:GroupStatus = _
  private var maxRunningJobs:Int = _
  private var maxAskExecutorTimes:Long = 0L

  def setMaxRunningJobs(maxRunningJobs:Int) :Unit = this.maxRunningJobs = maxRunningJobs

  def getMaxRunningJobs:Int = maxRunningJobs

  def setMaxAskExecutorTimes(maxAskExecutorTimes:Long):Unit = this.maxAskExecutorTimes = maxAskExecutorTimes

  def getMaxAskExecutorTimes:Long = maxAskExecutorTimes

  override def getStatus: GroupStatus = _status

  def setStatus(status: GroupStatus) = this._status = status
}
