package com.lm.scheduler.queue

/**
 * @Classname UserJob
 * @Description TODO
 * @Date 2020/9/27 16:53
 * @Created by limeng
 */
case class UserJob() extends Job{
  override def init() = {}

  override protected def jobToExecuteRequest = null

  override def getName = null

  override def getJobInfo = null

  override def close() = {}
}
