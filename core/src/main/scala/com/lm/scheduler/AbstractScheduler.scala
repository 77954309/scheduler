package com.lm.scheduler

import com.lm.scheduler.exception.SchedulerErrorException
import com.lm.scheduler.queue.SchedulerEvent
import com.lm.scheduler.utils.Utils
import org.apache.commons.lang.StringUtils

/**
 * @Classname AbstractScheduler
 * @Description TODO
 * @Date 2020/9/25 17:25
 * @Created by limeng
 */
abstract class AbstractScheduler extends Scheduler {
  override def init(): Unit = {}

  override def start(): Unit = {}

  private def getEventId(index:Int,groupName:String):String = groupName +"_"+index

  private def getIndexAndGroupName(eventId:String):(Int,String) ={
    if(StringUtils.isBlank(eventId) || !eventId.contains("_")) throw new SchedulerErrorException(12011, s"Unrecognized execId $eventId.（不能识别的execId $eventId.)")
    val index = eventId.lastIndexOf("_")
    if(index < 1) throw new SchedulerErrorException(12011, s"Unrecognized execId $eventId.（不能识别的execId $eventId.)")
    (eventId.substring(index + 1).toInt, eventId.substring(0, index))
  }

  override def submit(event: SchedulerEvent): Unit = {
    val groupName =getSchedulerContext.getOrCreateGroupFactory.getGroupNameByEvent(event)
    val consumer = getSchedulerContext.getOrCreateConsumerManager.getOrCreateConsumer(groupName)
    val index = consumer.getConsumeQueue.offer(event)
    index.map(getEventId(_, groupName)).foreach(event.setId)
    if(index.isEmpty) throw  new SchedulerErrorException(12001,"The submission job failed and the queue is full!(提交作业失败，队列已满！)")
  }

  override def get(event: SchedulerEvent): Option[SchedulerEvent] = get(event.getId)

  override def get(eventId: String): Option[SchedulerEvent] = {
    val (index,groupName) = getIndexAndGroupName(eventId)
    val consumer = getSchedulerContext.getOrCreateConsumerManager.getOrCreateConsumer(groupName)
    consumer.getRunningEvents.find(_.getId == eventId).orElse(consumer.getConsumeQueue.get(index))
  }

  override def shutdown(): Unit =if(getSchedulerContext != null) {
    if(getSchedulerContext.getOrCreateConsumerManager != null)
      Utils.tryQuietly(getSchedulerContext.getOrCreateConsumerManager.shutdown())
    if(getSchedulerContext.getOrCreateExecutorManager != null)
      Utils.tryQuietly(getSchedulerContext.getOrCreateExecutorManager)
    if(getSchedulerContext.getOrCreateSchedulerListenerBus != null)
      Utils.tryQuietly(getSchedulerContext.getOrCreateSchedulerListenerBus.stop())

  }


}
