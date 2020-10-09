package com.lm.scheduler.queue.parallelqueue

import com.lm.scheduler.queue.SchedulerEvent
import com.lm.scheduler.queue.fifoqueue.FIFOGroup

/**
 * @Classname ParallelGroup
 * @Description TODO
 * @Date 2020/10/9 14:28
 * @Created by limeng
 */
class ParallelGroup(groupName:String,initCapacity:Int,maxCapacity:Int) extends FIFOGroup(groupName,initCapacity,maxCapacity){
  override def belongTo(event: SchedulerEvent): Boolean = {
    val evenId = event.id.split("_")
    if(evenId.nonEmpty){
      val name = evenId(0)
      if(name.equals(groupName)) true else false
    }else{
      false
    }
  }
}
