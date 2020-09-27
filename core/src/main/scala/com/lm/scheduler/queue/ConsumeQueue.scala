package com.lm.scheduler.queue

/**
 * @Classname ConsumeQueue
 * @Description TODO
 * @Date 2020/9/25 17:22
 * @Created by limeng
 */
abstract class ConsumeQueue {
   def remove(event:SchedulerEvent):Unit
   def getWaitingEvents:Array[SchedulerEvent]
   def size:Int
   def isEmpty:Boolean
   def isFull:Boolean
   def clearAll(): Unit
   def get(event: SchedulerEvent):Option[SchedulerEvent]
   def get(index: Int): Option[SchedulerEvent]
   def getGroup: Group
   def setGroup(group: Group): Unit

   //添加一个，如果队列满了，将会一直阻塞，直到队列可用
   def put(event: SchedulerEvent): Int

   //添加一个，如果队列满了，返回None
   def offer(event: SchedulerEvent): Option[Int]

   //获取某个group最新的SchedulerEvent,如果不存在，就一直阻塞
   def take(): SchedulerEvent

   def take(mills:Long):Option[SchedulerEvent]

   //获取某个group最新SchedulerEvent，并移动指针到一个，如果没有，直接返回None
   def poll(): Option[SchedulerEvent]

   //只获取某个group最新的SchedulerEvent，并不移动指针。如果没有，直接返回None
   def peek(): Option[SchedulerEvent]

    //获取某个group满足条件的最新的SchedulerEvent，并不移动指针。如果没有，直接返回None
   def peek(op: SchedulerEvent => Boolean): Option[SchedulerEvent]
}
