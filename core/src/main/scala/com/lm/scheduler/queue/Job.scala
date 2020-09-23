package com.lm.scheduler.queue

import java.io.Closeable
import java.util.concurrent.Future

import com.lm.scheduler.utils.Logging

/**
 * @Classname Job
 * @Description TODO
 * @Date 2020/9/23 20:00
 * @Created by limeng
 */
abstract class Job extends  Runnable with SchedulerEvent with Closeable with Logging{
  import SchedulerEventState._
  private[queue] var future: Future[_] = _




}
