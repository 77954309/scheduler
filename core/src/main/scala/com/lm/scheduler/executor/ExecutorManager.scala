package com.lm.scheduler.executor

import com.lm.scheduler.listener.ExecutorListener
import com.lm.scheduler.queue.SchedulerEvent

import scala.concurrent.duration.Duration

/**
 * @Classname ExecutorManager
 * @Description TODO
 * @Date 2020/9/25 17:05
 * @Created by limeng
 */
abstract class ExecutorManager {

  def setExecutorListener(executorListener:ExecutorListener):Unit

  protected def createExecutor(event:SchedulerEvent):Executor

  def askExecutor(event: SchedulerEvent):Option[Executor]

  def askExecutor(event: SchedulerEvent,wait:Duration):Option[Executor]

  def getById(id:Long):Option[Executor]

  def getByGroup(groupName:String):Array[Executor]

  protected def delete(executor: Executor):Unit

  def shutdown(): Unit

}
