package com.lm.scheduler.listener

import com.lm.scheduler.executor.Executor
import com.lm.scheduler.executor.ExecutorState.ExecutorState

/**
 * @Classname ExecutorListener
 * @Description TODO
 * @Date 2020/9/25 17:05
 * @Created by limeng
 */
trait ExecutorListener extends SchedulerListener {
  def onExecutorCreated(executor: Executor): Unit
  def onExecutorCompleted(executor: Executor, message: String): Unit
  def onExecutorStateChanged(executor: Executor, fromState: ExecutorState, toState: ExecutorState): Unit
}
