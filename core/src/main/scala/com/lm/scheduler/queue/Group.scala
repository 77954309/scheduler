package com.lm.scheduler.queue

/**
 * @Classname Group
 * @Description TODO
 * @Date 2020/9/25 17:10
 * @Created by limeng
 */
trait Group {
  def getGroupName:String
  //等待Job占整个ConsumeQueue的百分比
  def getInitCapacity:Int
  //等待的Job占整个ConsumeQueue的最大百分比
  def getMaximumCapacity:Int
  def getStatus: GroupStatus.GroupStatus

  def belongTo(event: SchedulerEvent): Boolean
}
object GroupStatus extends Enumeration {
  type GroupStatus = Value
  val USING, UNUSED = Value
}