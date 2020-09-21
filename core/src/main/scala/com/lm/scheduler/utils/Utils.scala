package com.lm.scheduler.utils

import com.lm.scheduler.exception.{ErrorException, FatalException, WarnException}
import org.slf4j.Logger

import scala.util.control.ControlThrowable

/**
 * @Classname Utils
 * @Description TODO
 * @Date 2020/9/21 17:14
 * @Created by limeng
 */
object Utils extends Logging {
  def tryQuietly[T](tryOp: => T): T = try tryOp catch {
    case c: ControlThrowable => throw c
    case fatal: FatalException =>
      error("Fatal Error, system exit...", fatal)
      System.exit(fatal.getErrCode)
      null.asInstanceOf[T]
    case _ => null.asInstanceOf[T]
  }

  def tryCatch[T](tryOp: => T)(catchOp: Throwable => T): T = {
    try tryOp catch {
      case t: ControlThrowable => throw t
      case fatal: FatalException =>
        error("Fatal Error, system exit...", fatal)
        System.exit(fatal.getErrCode)
        null.asInstanceOf[T]
      case t: Throwable => catchOp(t)
    }
  }

  def tryThrow[T](tryOp: => T)(exception: Throwable => Throwable): T = tryCatch(tryOp){
    t: Throwable => throw exception(t)
  }

  def tryFinally[T](tryOp: => T)(finallyOp: => Unit): T = try tryOp finally finallyOp

  def tryQuietly[T](tryOp: => T, catchOp: Throwable => Unit): T = tryCatch(tryOp){
    case fatal: FatalException =>
      error("Fatal Error, system exit...", fatal)
      System.exit(fatal.getErrCode)
      null.asInstanceOf[T]
    case t: Throwable =>
      catchOp(t)
      null.asInstanceOf[T]
  }

  def tryAndWarn[T](tryOp: => T)(implicit log: Logger): T = tryCatch(tryOp){
    case error: ErrorException =>
      val errorMsg = s"error code（错误码）: ${error.getErrCode}, Error message（错误信息）: ${error.getDesc}."
      log.error(errorMsg, error)
      null.asInstanceOf[T]
    case warn: WarnException =>
      val warnMsg = s"Warning code（警告码）: ${warn.getErrCode}, Warning message（警告信息）: ${warn.getDesc}."
      log.warn(warnMsg, warn)
      null.asInstanceOf[T]
    case t: Throwable =>
      log.warn("", t)
      null.asInstanceOf[T]
  }

  def tryAndWarnMsg[T](tryOp: => T)(message: String)(implicit log: Logger): T = tryCatch(tryOp){
    case error: ErrorException =>
      log.error(s"error code（错误码）: ${error.getErrCode}, Error message（错误信息）: ${error.getDesc}.")
      log.error(message, error)
      null.asInstanceOf[T]
    case warn: WarnException =>
      log.warn(s"Warning code（警告码）: ${warn.getErrCode}, Warning message（警告信息）: ${warn.getDesc}.")
      log.warn(message, warn)
      null.asInstanceOf[T]
    case t: Throwable =>
      log.warn(message, t)
      null.asInstanceOf[T]
  }

  def tryAndError[T](tryOp: => T)(implicit log: Logger): T = tryCatch(tryOp){
    case error: ErrorException =>
      val errorMsg = s"error code（错误码）: ${error.getErrCode}, Error message（错误信息）: ${error.getDesc}."
      log.error(errorMsg, error)
      null.asInstanceOf[T]
    case warn: WarnException =>
      val warnMsg = s"Warning code（警告码）: ${warn.getErrCode}, Warning message（警告信息）: ${warn.getDesc}."
      log.warn(warnMsg, warn)
      null.asInstanceOf[T]
    case t: Throwable =>
      log.error("", t)
      null.asInstanceOf[T]
  }

  def tryAndErrorMsg[T](tryOp: => T)(message: String)(implicit log: Logger): T = tryCatch(tryOp){
    case error: ErrorException =>
      log.error(s"error code（错误码）: ${error.getErrCode}, Error message（错误信息）: ${error.getDesc}.")
      log.error(message, error)
      null.asInstanceOf[T]
    case warn: WarnException =>
      log.warn(s"Warning code（警告码）: ${warn.getErrCode}, Warning message（警告信息）: ${warn.getDesc}.")
      log.warn(message, warn)
      null.asInstanceOf[T]
    case t: Throwable =>
      log.error(message, t)
      null.asInstanceOf[T]
  }

  def sleepQuietly(mills: Long): Unit = tryQuietly(Thread.sleep(mills))
}
