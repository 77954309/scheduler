package com.lm.scheduler.executor

import java.io.Closeable

import com.lm.protocol.engine.EngineState._
import com.lm.protocol.engine.EngineState

/**
 * @Classname Executor
 * @Description TODO
 * @Date 2020/9/23 20:11
 * @Created by limeng
 */
trait Executor extends Closeable{
  def getId: Long
  def execute(executeRequest: ExecuteRequest): ExecuteResponse
  def state: ExecutorState.ExecutorState

  def getExecutorInfo: ExecutorInfo

}
object ExecutorState {
  type ExecutorState = EngineState
  val Starting = EngineState.Starting
  val Idle = EngineState.Idle
  val Busy = EngineState.Busy
  val ShuttingDown = EngineState.ShuttingDown
  val Error = EngineState.Error
  val Dead = EngineState.Dead
  val Success = EngineState.Success

  def apply(x: Int): ExecutorState = EngineState(x)
  def isCompleted(state: ExecutorState) = EngineState.isCompleted(state.asInstanceOf[EngineState])
  def isAvailable(state: ExecutorState) = EngineState.isAvailable(state.asInstanceOf[EngineState])

}

