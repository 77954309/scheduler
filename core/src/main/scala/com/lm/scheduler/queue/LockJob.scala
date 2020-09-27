package com.lm.scheduler.queue

/**
 * @Classname LockJob
 * @Description TODO
 * @Date 2020/9/25 17:23
 * @Created by limeng
 */
abstract class LockJob extends Job {

  private var lock:String = _

  def setLock(lock:String) :Unit =this.lock =lock

  def getLock:String = lock
}
