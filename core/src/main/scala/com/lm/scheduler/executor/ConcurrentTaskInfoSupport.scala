package com.lm.scheduler.executor

/**
 * @Classname ConcurrentTaskInfoSupport
 * @Description TODO
 * @Date 2020/9/23 20:12
 * @Created by limeng
 */
trait ConcurrentTaskInfoSupport {
  def progress(jobId: String): Float
  def getProgressInfo(jobId: String): Array[JobProgressInfo]
  def log(jobId: String): String

}
case class JobProgressInfo(id: String, totalTasks: Int, runningTasks: Int, failedTasks: Int, succeedTasks: Int)