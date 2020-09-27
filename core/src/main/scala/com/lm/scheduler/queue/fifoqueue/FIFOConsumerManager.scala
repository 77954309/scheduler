package com.lm.scheduler.queue.fifoqueue

import java.util.concurrent.{ExecutorService, ThreadPoolExecutor}

import com.lm.scheduler.listener.ConsumerListener
import com.lm.scheduler.queue.{Consumer, ConsumerManager, Group, LoopArrayQueue}

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

  override def setConsumerListener(consumerListener: ConsumerListener): Unit = this.consumerListener = consumerListener

  override def getOrCreateExecutorService: ExecutorService = executorService

  override def getOrCreateConsumer(groupName: String): Consumer = consumer

  override protected def createConsumer(groupName: String): Consumer = {
    getSchedulerContext
  }

  override def destroyConsumer(groupName: String): Unit = ???

  override def shutdown(): Unit = ???

  override def listConsumers(): Array[Consumer] = ???
}
