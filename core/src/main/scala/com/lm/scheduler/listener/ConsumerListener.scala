package com.lm.scheduler.listener

import com.lm.scheduler.queue.Consumer

/**
 * @Classname ConsumerListener
 * @Description TODO
 * @Date 2020/9/25 17:09
 * @Created by limeng
 */
trait ConsumerListener extends SchedulerListener {
  def onConsumerCreated(consumer: Consumer): Unit
  def onConsumerDestroyed(consumer: Consumer): Unit
}
