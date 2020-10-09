package com.lm.scheduler.queue.parallelqueue

import com.lm.scheduler.queue.{Group, GroupFactory, SchedulerEvent}

import scala.collection.mutable

/**
 * @Classname ParallelGroupFactory
 * @Description TODO
 * @Date 2020/10/9 14:35
 * @Created by limeng
 */
class ParallelGroupFactory extends GroupFactory{
  private val groupMap = new mutable.HashMap[String, Group]()

  def getInitCapacity(groupName: String): Int= 100

  def getMaxCapacity(groupName: String): Int = 1000

  private val UJES_CONTEXT_CONSTRUCTOR_LOCK = new Object()


  override def getOrCreateGroup(groupName: String): Group = {
    UJES_CONTEXT_CONSTRUCTOR_LOCK.synchronized {
      if(groupMap.get(groupName).isDefined){
        groupMap.get(groupName).get
      }else{
        val group = new ParallelGroup(groupName, getInitCapacity(groupName), getMaxCapacity(groupName))
        groupMap.put(groupName, group)
        group
      }
    }
  }

  override def getGroupNameByEvent(event: SchedulerEvent): String = {
    val belongList = groupMap.values.filter(x => x.belongTo(event)).map(x => x.getGroupName).toList
    if(belongList.size > 0){
      belongList(0)
    }else{
      "NULL"
    }
  }
}
