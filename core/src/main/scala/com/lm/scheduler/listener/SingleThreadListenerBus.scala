package com.lm.scheduler.listener

/**
 * @Classname SingleThreadListenerBus
 * @Description TODO
 * @Date 2020/9/21 17:05
 * @Created by limeng
 */
abstract  class SingleThreadListenerBus[L <: EventListener, E <: Event](eventQueueCapacity: Int, name: String)  extends ListenerEventBus[L, E](eventQueueCapacity, name)(1, -1){

}
