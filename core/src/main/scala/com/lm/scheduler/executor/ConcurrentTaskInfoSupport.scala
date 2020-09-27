package com.lm.scheduler.executor

import com.lm.protocol.engine.JobProgressInfo

/**
 * @Classname ConcurrentTaskInfoSupport
 * @Description TODO
 * @Date 2020/9/23 20:12
 * @Created by limeng
 *  test
 */
trait ConcurrentTaskInfoSupport {
  def progress(jobId: String): Float
  def getProgressInfo(jobId: String): Array[JobProgressInfo]
  def log(jobId: String): String

}
