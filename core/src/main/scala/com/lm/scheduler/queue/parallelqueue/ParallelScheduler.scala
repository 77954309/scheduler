package com.lm.scheduler.queue.parallelqueue

import com.lm.scheduler.queue.{ConsumerManager, GroupFactory}
import com.lm.scheduler.{AbstractScheduler, SchedulerContext}

/**
 * @Classname ParallelScheduler
 * @Description TODO
 * @Date 2020/10/9 14:48
 * @Created by limeng
 */
class ParallelScheduler(val schedulerContext: SchedulerContext) extends AbstractScheduler {

  private var consumerManager: ConsumerManager = _
  private var groupFactory: GroupFactory = _

  override def init() = {
    consumerManager = schedulerContext.getOrCreateConsumerManager
    groupFactory = schedulerContext.getOrCreateGroupFactory
  }

  override def getName = "ParallelScheduler"

  override def getSchedulerContext = schedulerContext
}
