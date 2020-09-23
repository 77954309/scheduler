package com.lm.scheduler.executor

import java.io.Closeable

/**
 * @Classname Executor
 * @Description TODO
 * @Date 2020/9/23 20:11
 * @Created by limeng
 */
trait Executor extends Closeable{
  def getId: Long


}
