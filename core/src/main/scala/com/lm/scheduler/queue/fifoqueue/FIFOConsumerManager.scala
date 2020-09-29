package com.lm.scheduler.queue.fifoqueue

import java.util.concurrent.{ExecutorService, ThreadPoolExecutor}

import com.lm.scheduler.SchedulerContext
import com.lm.scheduler.exception.SchedulerErrorException
import com.lm.scheduler.listener.ConsumerListener
import com.lm.scheduler.queue.{Consumer, ConsumerManager, Group, LoopArrayQueue}
import com.lm.scheduler.utils.Utils


/**
 * @Classname FIFOConsumerManager
 * @Description TODO
 * @Date 2020/9/27 17:23
 * @Created by limeng
 */
class FIFOConsumerManager(groupName: String) extends ConsumerManager{

  def this() =  this("FIFO_GROUP")

  private var group: Group = _

  private var executorService:ThreadPoolExecutor = _

  private var consumerListener:ConsumerListener = _

  private var consumerQueue:LoopArrayQueue = _

  private var consumer:Consumer = _


  override def setSchedulerContext(schedulerContext: SchedulerContext): Unit = {
    super.setSchedulerContext(schedulerContext)
    group = getSchedulerContext.getOrCreateGroupFactory.getOrCreateGroup(groupName)
    executorService = group match {
      case g:FIFOGroup => Utils.newCachedThreadPool(g.getMaxRunningJobs + 2, groupName + "-Thread-")
      case _=> throw new SchedulerErrorException(13000, s"FIFOConsumerManager need a FIFOGroup, but ${group.getClass} is supported.")
    }
    consumerQueue = new LoopArrayQueue(getSchedulerContext.getOrCreateGroupFactory.getOrCreateGroup(null))
    consumer = createConsumer(null)
  }

  override def setConsumerListener(consumerListener: ConsumerListener): Unit = this.consumerListener = consumerListener

  override def getOrCreateExecutorService: ExecutorService = executorService

  override def getOrCreateConsumer(groupName: String): Consumer = consumer

  override protected def createConsumer(groupName: String): Consumer = {
    val group = getSchedulerContext.getOrCreateGroupFactory.getOrCreateGroup(null)
    val consumer = new FIFOUserConsumer(getSchedulerContext,getOrCreateExecutorService,group)
    consumer.setGroup(group)
    consumer.setConsumeQueue(consumerQueue)
    if(consumerListener != null) consumerListener.onConsumerCreated(consumer)
    consumer.start()
    consumer
  }

  override def destroyConsumer(groupName: String): Unit = {}

  override def shutdown(): Unit = {
    if(consumerListener != null) consumerListener.onConsumerDestroyed(consumer)
    consumer.shutdown()
    executorService.shutdownNow()
  }

  override def listConsumers(): Array[Consumer] = Array(consumer)
}
