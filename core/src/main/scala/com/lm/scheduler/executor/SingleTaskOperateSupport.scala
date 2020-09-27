package com.lm.scheduler.executor

/**
 * @Classname SingleTaskOperateSupport
 * @Description TODO
 * @Date 2020/9/25 17:04
 * @Created by limeng
 */
trait SingleTaskOperateSupport {
  def kill(): Boolean
  def pause(): Boolean
  def resume(): Boolean
}
