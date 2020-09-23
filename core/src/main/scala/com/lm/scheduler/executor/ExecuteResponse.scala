package com.lm.scheduler.executor

/**
 * @Classname ExecuteResponse
 * @Description TODO
 * @Date 2020/9/23 20:06
 * @Created by limeng
 */
trait ExecuteResponse
trait CompletedExecuteResponse extends ExecuteResponse
case class SuccessExecuteResponse() extends CompletedExecuteResponse
trait OutputExecuteResponse extends ExecuteResponse {
  def getOutput: String
}
case class AliasOutputExecuteResponse(alias: String, output: String) extends OutputExecuteResponse {
  override def getOutput: String = output
}
case class ErrorExecuteResponse(message: String, t: Throwable) extends CompletedExecuteResponse
case class IncompleteExecuteResponse(message: String) extends ExecuteResponse
trait AsynReturnExecuteResponse extends ExecuteResponse {
  def notify(rs: ExecuteResponse => Unit): Unit
}