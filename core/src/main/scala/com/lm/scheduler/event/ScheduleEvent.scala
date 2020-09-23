package com.lm.scheduler.event

import com.lm.scheduler.listener.Event
import com.lm.scheduler.queue.Job

/**
 * @Classname ScheduleEvent
 * @Description TODO
 * @Date 2020/9/23 20:10
 * @Created by limeng
 */
trait ScheduleEvent extends Event{
  val job: Job
}
