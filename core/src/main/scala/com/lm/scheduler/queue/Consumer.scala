package com.lm.scheduler.queue

import java.util.concurrent.ExecutorService

import com.lm.scheduler.SchedulerContext
import com.lm.scheduler.utils.Logging

/**
 * @Classname Consumer
 * @Description TODO
 * @Date 2020/9/25 17:09
 * @Created by limeng
 */
abstract class Consumer(schedulerContext: SchedulerContext,
                        executeService: ExecutorService) extends Runnable with Logging {

  var terminate = false

  def setConsumeQueue(consumeQueue: ConsumeQueue):Unit

  def getConsumeQueue: ConsumeQueue

  def getGroup:Group

  def setGroup(group: Group): Unit

  def getRunningEvents:Array[SchedulerEvent]

  def start():Unit

  def shutdown():Unit = {
    info(s"$toString is ready to stop!")
    terminate = true
    info(s"$toString stopped!")
  }

  override def toString: String = getGroup.getGroupName + "Consumer"

}
