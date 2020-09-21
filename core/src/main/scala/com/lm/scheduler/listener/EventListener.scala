package com.lm.scheduler.listener

/**
 * @Classname EventListener
 * @Description TODO
 * @Date 2020/9/21 14:02
 * @Created by limeng
 */
trait EventListener {
  def onEventError(event:Event,t:Throwable) : Unit
}
