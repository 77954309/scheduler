package com.lm.scheduler.queue.parallelqueue

import com.lm.scheduler.SchedulerContext
import com.lm.scheduler.event.{ScheduleEvent, SchedulerEventListener}
import com.lm.scheduler.executor.ExecutorManager
import com.lm.scheduler.listener.ListenerEventBus
import com.lm.scheduler.queue.{ConsumerManager, GroupFactory}
import com.lm.scheduler.utils.Logging

/**
 * @Classname ParallelSchedulerContextImpl
 * @Description TODO
 * @Date 2020/10/9 14:52
 * @Created by limeng
 */
class ParallelSchedulerContextImpl(val maxParallelismUsers: Int) extends  SchedulerContext with Logging {
  private var consumerManager: ParallelConsumerManager = _
  private var groupFactory: ParallelGroupFactory = _
  private val UJES_CONTEXT_CONSTRUCTOR_LOCK = new Object()

  override def getOrCreateGroupFactory: GroupFactory = {
    UJES_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      if (groupFactory == null) {
        groupFactory = new ParallelGroupFactory()
      }
      groupFactory
    }
  }

  override def getOrCreateConsumerManager: ConsumerManager = {
    UJES_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      if (consumerManager == null) {
        consumerManager = new ParallelConsumerManager(maxParallelismUsers)
        consumerManager.setSchedulerContext(this)
      }
      consumerManager
    }
  }

  override def getOrCreateExecutorManager: ExecutorManager = null

  override def getOrCreateSchedulerListenerBus: ListenerEventBus[_ <: SchedulerEventListener, _ <: ScheduleEvent] = null
}
