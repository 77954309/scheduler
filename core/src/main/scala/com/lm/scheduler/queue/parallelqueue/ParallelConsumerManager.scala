package com.lm.scheduler.queue.parallelqueue

import java.util.concurrent.ExecutorService

import com.lm.scheduler.listener.ConsumerListener
import com.lm.scheduler.queue.fifoqueue.FIFOUserConsumer
import com.lm.scheduler.queue.{Consumer, ConsumerManager, LoopArrayQueue}
import com.lm.scheduler.utils.Utils

import scala.collection.mutable

/**
 * @Classname ParallelConsumerManager
 * @Description TODO
 * @Date 2020/10/9 14:39
 * @Created by limeng
 */
class ParallelConsumerManager(maxParallelismUsers:Int) extends ConsumerManager{

  private val UJES_CONTEXT_CONSTRUCTOR_LOCK = new Object()

  private var consumerListener: Option[ConsumerListener] = None

  private var executorService: ExecutorService = _

  private val consumerGroupMap = new mutable.HashMap[String, FIFOUserConsumer]()


  override def setConsumerListener(consumerListener: ConsumerListener): Unit = {
    this.consumerListener = Some(consumerListener)
  }

  override def getOrCreateExecutorService: ExecutorService = {
    if(executorService != null) executorService
    else UJES_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      if(executorService == null) {
        executorService =  Utils.newCachedThreadPool(5 * maxParallelismUsers + 1, "Engine-Scheduler-ThreadPool-", true)
      }
      executorService
    }
  }


  override def getOrCreateConsumer(groupName: String): Consumer = {
    if(consumerGroupMap.contains(groupName)) consumerGroupMap(groupName)
    else UJES_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      consumerGroupMap.getOrElse(groupName,{
        val newConsumer = createConsumer(groupName)
        val group = getSchedulerContext.getOrCreateGroupFactory.getOrCreateGroup(groupName)
        newConsumer.setGroup(group)
        newConsumer.setConsumeQueue(new LoopArrayQueue(group))
        consumerGroupMap.put(groupName, newConsumer)
        consumerListener.foreach(_.onConsumerCreated(newConsumer))
        newConsumer.start()
        newConsumer
      })
    }
  }

  override protected def createConsumer(groupName: String) = {
    val group = getSchedulerContext.getOrCreateGroupFactory.getOrCreateGroup(groupName)
    new FIFOUserConsumer(getSchedulerContext, getOrCreateExecutorService, group)
  }

  override def destroyConsumer(groupName: String): Unit =
    consumerGroupMap.get(groupName).foreach { tmpConsumer =>
    tmpConsumer.shutdown()
    consumerGroupMap.remove(groupName)
    consumerListener.foreach(_.onConsumerDestroyed(tmpConsumer))
  }

  override def shutdown(): Unit =  consumerGroupMap.iterator.foreach(x => x._2.shutdown())

  override def listConsumers(): Array[Consumer] = consumerGroupMap.values.toArray
}
