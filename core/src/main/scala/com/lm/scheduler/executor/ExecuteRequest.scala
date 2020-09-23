package com.lm.scheduler.executor

/**
 * @Classname ExecuteRequest
 * @Description TODO
 * @Date 2020/9/23 20:11
 * @Created by limeng
 */
trait ExecuteRequest {
  val code: String
}
trait JobExecuteRequest {
  val jobId: String
}
trait RunTypeExecuteRequest{
  val runType:String
}
