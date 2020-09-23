package com.lm.scheduler.event

import com.lm.scheduler.listener.Event
import com.lm.scheduler.queue.Job

/**
 * @Classname LogEvent
 * @Description TODO
 * @Date 2020/9/23 20:08
 * @Created by limeng
 */
class LogEvent(source:Job,
               t:Int) extends Event{
  def getT:Int = t
}
object LogEvent{
  val read:Int = 1
  val write:Int = 2

  def apply(source: Job,
            t: Int): LogEvent = new LogEvent(source, t)
}