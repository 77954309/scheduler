package com.lm.scheduler.executor

/**
 * @Classname ConcurrentTaskOperateSupport
 * @Description TODO
 * @Date 2020/9/23 20:12
 * @Created by limeng
 */
trait ConcurrentTaskOperateSupport {
  def kill(jobId: String): Boolean
  def killAll(): Boolean
  def pause(jobId: String): Boolean
  def pauseAll(): Boolean
  def resume(jobId: String): Boolean
  def resumeAll(): Boolean
}
