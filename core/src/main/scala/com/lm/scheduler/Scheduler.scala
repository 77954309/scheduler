package com.lm.scheduler

import com.lm.scheduler.queue.SchedulerEvent
import com.lm.scheduler.queue.fifoqueue.FIFOScheduler
import com.lm.scheduler.utils.Logging

/**
 * @Classname Scheduler
 * @Description TODO
 * @Date 2020/9/25 17:25
 * @Created by limeng
 */
abstract class Scheduler {
  def init():Unit
  def start():Unit
  def getName:String
  def submit(event:SchedulerEvent):Unit
  def get(event: SchedulerEvent): Option[SchedulerEvent]
  def get(eventId: String): Option[SchedulerEvent]
  def shutdown():Unit
  def getSchedulerContext:SchedulerContext
}
object Scheduler extends Logging{
  def createScheduler(scheduleType: String, schedulerContext: SchedulerContext): Option[Scheduler] = {
    scheduleType match {
      case "FIFO" => Some(new FIFOScheduler(schedulerContext))
      case "PARA" =>  Some(new ParallelScheduler(schedulerContext))
      case _ => {
        error("Please enter the correct scheduling type!(请输入正确的调度类型!)")
        None
      }
    }
  }

}
