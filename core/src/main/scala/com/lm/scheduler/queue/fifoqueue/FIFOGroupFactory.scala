package com.lm.scheduler.queue.fifoqueue

import com.lm.scheduler.queue.{Group, GroupFactory, SchedulerEvent}

import scala.collection.mutable

/**
 * @Classname FIFOGroupFactory
 * @Description TODO
 * @Date 2020/9/27 17:30
 * @Created by limeng
 */
class FIFOGroupFactory extends GroupFactory{

  private val groupMap = new mutable.HashMap[String,Group]()

  private val UJES_CONTEXT_CONSTRUCTOR_LOCK = new Object()

  //Obtained from the database(从数据库获取)
  def getInitCapacity(groupName: String): Int = 1000

  def getMaxCapacity(groupName: String): Int = 10000


  override def getOrCreateGroup(groupName: String): Group = {
    UJES_CONTEXT_CONSTRUCTOR_LOCK.synchronized{
      if(groupMap.get(groupName).isDefined){
        groupMap.get(groupName).get
      }else{
        val group = new FIFOGroup(groupName, getInitCapacity(groupName), getMaxCapacity(groupName))
        groupMap.put(groupName, group)
        group
      }
    }
  }

  override def getGroupNameByEvent(event: SchedulerEvent): String =  "FIFOGROUP"
}
