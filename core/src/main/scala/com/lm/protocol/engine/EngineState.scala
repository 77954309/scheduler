package com.lm.protocol.engine

/**
 * @Classname EngineState
 * @Description TODO
 * @Date 2020/9/25 16:53
 * @Created by limeng
 */
object EngineState extends Enumeration {
  type EngineState = Value
  val Starting, Idle, Busy, ShuttingDown, Error, Dead, Success = Value
  def isCompleted(executorState: EngineState): Boolean = executorState match {
    case Error | Dead | Success => true
    case _ => false
  }

  def isAvailable(executorState:EngineState): Boolean = executorState match {
    case Idle | Busy => true
    case _ => false
  }
}
