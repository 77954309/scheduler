package com.lm.scheduler.executor

import com.lm.scheduler.exception.SchedulerErrorException
import com.lm.scheduler.executor.ExecutorState.ExecutorState
import com.lm.scheduler.executor.ExecutorState._
import com.lm.scheduler.listener.ExecutorListener
import com.lm.scheduler.utils.{Logging, Utils}

/**
 * @Classname AbstractExecutor
 * @Description TODO
 * @Date 2020/9/28 16:51
 * @Created by limeng
 */
abstract class AbstractExecutor(id: Long) extends Executor with Logging {

  private var _state:ExecutorState = Starting
  private var lastActivityTime = System.currentTimeMillis()
  private var executorListener:Option[ExecutorListener] = None

  def setExecutorListener(executorListener: ExecutorListener) :Unit = this.executorListener = Some(executorListener)

  protected def callback():Unit

  protected def isIdle = _state == Idle

  protected def isBusy = _state = Busy

  protected def whenState[A](state: ExecutorState, f: => A) = if(_state == state) f

  protected def whenBusy[A](f: => A) = whenState(Busy,f)

  protected def whenIdle[A](f: => A) = whenState(Idle, f)

  protected def ensureBusy[A](f: => A):A = {
    lastActivityTime = System.currentTimeMillis()
    if(_state == Busy) synchronized {
      if(_state == Busy) return f
    }
    throw new SchedulerErrorException(20001, "%s is in state %s." format (toString, _state))
  }

  protected def ensureIdle[A](f: => A,transitionState:Boolean):A ={
    if(_state == Idle) synchronized {
      if(_state == Idle){
        if(transitionState) transition(Busy)
        return Utils.tryFinally(f){
          if(transitionState) transition(Idle)
          callback()
        }
      }
    }
    throw new SchedulerErrorException(20001, "%s is in state %s." format (toString, _state))
  }

  protected def ensureAvailable[A](f: => A):A={
    if(ExecutorState.isAvailable(_state)) synchronized {
      if(ExecutorState.isAvailable(_state)) return Utils.tryFinally(f)(callback())
    }
    throw new SchedulerErrorException(20001, "%s is in state %s." format (toString, _state))
  }

  protected def whenAvailable[A](f: => A):A = {
    if(ExecutorState.isAvailable(_state)) return Utils.tryFinally(f)(callback())
    throw new SchedulerErrorException(20001, "%s is in state %s." format (toString, _state))
  }

  protected def transition(state:ExecutorState):Unit = this synchronized {
    lastActivityTime = System.currentTimeMillis()
    this._state match {
      case Error | Dead | Success =>
        warn(s"$toString attempt to change state ${this._state} => $state, ignore it.")
      case ShuttingDown =>
        state match {
          case Error | Dead | Success =>
            val oldState = _state
            executorListener.foreach(_.onExecutorStateChanged(this, oldState, state))
          case _ => warn(s"$toString attempt to change a ShuttingDown session to $state, ignore it.")
        }
      case _=>
        info(s"$toString change state ${_state} => $state.")
        val oldState = _state
        this._state = state
        executorListener.foreach(_.onExecutorStateChanged(this, oldState, state))
    }
  }

  override def getId: Long = id

  override def state: ExecutorState = _state

  override def getExecutorInfo: ExecutorInfo = ExecutorInfo(id,_state)

  def getLastActivityTime:Long = lastActivityTime
}
