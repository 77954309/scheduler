package com.lm.scheduler.queue.fifoqueue

import com.lm.scheduler.{AbstractScheduler, SchedulerContext}
import com.lm.scheduler.queue.{ConsumerManager, GroupFactory}

/**
 * @Classname FIFOScheduler
 * @Description TODO
 * @Date 2020/9/27 17:22
 * @Created by limeng
 */
class FIFOScheduler(val schedulerContext: SchedulerContext) extends AbstractScheduler  {
  private var consumerManager:ConsumerManager = _

  private var groupFactory:GroupFactory = _

  override def init()={
    consumerManager =schedulerContext.getOrCreateConsumerManager
    groupFactory = schedulerContext.getOrCreateGroupFactory
  }

  override def getName: String = "FIFOScheduler"

  override def getSchedulerContext: SchedulerContext = schedulerContext
}
