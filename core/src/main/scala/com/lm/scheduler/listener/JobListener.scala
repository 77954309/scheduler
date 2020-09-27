package com.lm.scheduler.listener

import com.lm.scheduler.queue.Job

/**
 * @Classname JobListener
 * @Description TODO
 * @Date 2020/9/25 17:06
 * @Created by limeng
 */
trait JobListener extends SchedulerListener{
  def onJobScheduled(job: Job): Unit
  def onJobInited(job: Job): Unit
  def onJobWaitForRetry(job: Job): Unit
  def onJobRunning(job: Job): Unit
  def onJobCompleted(job: Job): Unit
}
