package com.lm.scheduler.future

import java.util.concurrent.{Future, FutureTask}

import com.lm.scheduler.utils.{Logging, Utils}

/**
 * @Classname BDPFutureTask
 * @Description TODO
 * @Date 2020/9/23 20:01
 * @Created by limeng
 */
class BDPFutureTask(future:Future[_])  extends BDPFuture with Logging  {
  override def cancel(): Unit = {
    Utils.tryAndErrorMsg{
      future match {
        case futureTask: FutureTask[_] =>
          info("Start to interrupt BDPFutureTask")
          val futureType = futureTask.getClass
          val field = futureType.getDeclaredField("runner")
          field.setAccessible(true)
          val runner = field.get(futureTask).asInstanceOf[Thread]
          runner.interrupt()
          info(s"Finished to interrupt BDPFutureTask of ${runner.getName}")
      }
    }("Failed to interrupt BDPFutureTask")
  }
}
