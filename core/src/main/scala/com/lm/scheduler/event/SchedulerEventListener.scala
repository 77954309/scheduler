package com.lm.scheduler.event

import com.lm.scheduler.listener.EventListener

/**
 * @Classname SchedulerEventListener
 * @Description TODO
 * @Date 2020/9/23 20:10
 * @Created by limeng
 */
trait SchedulerEventListener extends EventListener{
  def onEvent(scheduleEvent: ScheduleEvent): Unit
}
