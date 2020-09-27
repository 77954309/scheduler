package com.lm.scheduler.queue

/**
 * @Classname GroupFactory
 * @Description TODO
 * @Date 2020/9/25 17:19
 * @Created by limeng
 */
abstract class GroupFactory {
  def getOrCreateGroup(groupName: String): Group

  def getGroupNameByEvent(event: SchedulerEvent): String

}
