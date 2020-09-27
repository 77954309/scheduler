package com.lm.scheduler.queue.fifoqueue

import com.lm.scheduler.SchedulerContext
import com.lm.scheduler.event.{ScheduleEvent, SchedulerEventListener}
import com.lm.scheduler.executor.ExecutorManager
import com.lm.scheduler.listener.ListenerEventBus
import com.lm.scheduler.queue.{ConsumerManager, GroupFactory}
import com.lm.scheduler.utils.Logging

/**
 * @Classname FIFOSchedulerContextImpl
 * @Description TODO
 * @Date 2020/9/27 17:28
 * @Created by limeng
 */
class FIFOSchedulerContextImpl(val maxParallelismUsers: Int)  extends  SchedulerContext with Logging{
  private var consumerManager: FIFOConsumerManager = _
  private var groupFactory: FIFOGroupFactory = _
  private val UJES_CONTEXT_CONSTRUCTOR_LOCK = new Object()


  override def getOrCreateGroupFactory: GroupFactory ={
    UJES_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      if(groupFactory == null){
        groupFactory = new FIFOGroupFactory()
      }
      groupFactory
    }
  }

  override def getOrCreateConsumerManager: ConsumerManager = {
    UJES_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      if (consumerManager == null) {
        consumerManager = new FIFOConsumerManager
        consumerManager.setSchedulerContext(this)
      }
      consumerManager
    }
  }

  override def getOrCreateExecutorManager: ExecutorManager = null

  override def getOrCreateSchedulerListenerBus: ListenerEventBus[_ <: SchedulerEventListener, _ <: ScheduleEvent] = null
}
