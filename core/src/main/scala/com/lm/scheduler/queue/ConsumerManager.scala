package com.lm.scheduler.queue

import java.util.concurrent.ExecutorService

import com.lm.scheduler.SchedulerContext
import com.lm.scheduler.listener.ConsumerListener

/**
 * @Classname ConsumerManager
 * @Description TODO
 * @Date 2020/9/25 17:22
 * @Created by limeng
 */
abstract class ConsumerManager {

  private var schedulerContext:SchedulerContext = _

  def setSchedulerContext(schedulerContext:SchedulerContext) :Unit = this.schedulerContext = schedulerContext

  def getSchedulerContext:SchedulerContext = schedulerContext

  def setConsumerListener(consumerListener:ConsumerListener):Unit

  def getOrCreateExecutorService:ExecutorService

  def getOrCreateConsumer(groupName:String):Consumer

  protected def createConsumer(groupName:String):Consumer

  def destroyConsumer(groupName:String):Unit

  def shutdown():Unit

  def listConsumers():Array[Consumer]

}
