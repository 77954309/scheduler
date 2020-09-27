package com.lm.scheduler

import com.lm.scheduler.event.{ScheduleEvent, SchedulerEventListener}
import com.lm.scheduler.executor.ExecutorManager
import com.lm.scheduler.listener.ListenerEventBus
import com.lm.scheduler.queue.{Consumer, ConsumerManager, GroupFactory}

/**
 * @Classname SchedulerContext
 * @Description TODO
 * @Date 2020/9/25 17:24
 * @Created by limeng
 *  上下文
 */
trait SchedulerContext {

  def getOrCreateGroupFactory:GroupFactory

  def getOrCreateConsumerManager:ConsumerManager

  def getOrCreateExecutorManager:ExecutorManager

  def getOrCreateSchedulerListenerBus:ListenerEventBus[_ <: SchedulerEventListener, _<: ScheduleEvent]




}
