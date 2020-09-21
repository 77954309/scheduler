package com.lm.scheduler.utils

import org.slf4j.LoggerFactory


/**
 * @Classname Logging
 * @Description TODO
 * @Date 2020/9/21 17:00
 * @Created by limeng
 */
trait Logging {
  protected lazy implicit val logger = LoggerFactory.getLogger(getClass)
  def trace(message: => String) = {
    if (logger.isTraceEnabled) {
      logger.trace(message.toString)
    }
  }

  def debug(message: => String): Unit = {
    if (logger.isDebugEnabled) {
      logger.debug(message.toString)
    }
  }

  def info(message: => String): Unit = {
    if (logger.isInfoEnabled) {
      logger.info(message.toString)
    }
  }

  def info(message: => String, t: Throwable): Unit = {
    logger.info(message.toString, t)
  }

  def warn(message: => String): Unit = {
    logger.warn(message.toString)
  }

  def warn(message: => String, t: Throwable): Unit = {
    logger.warn(message.toString, t)
  }

  def error(message: => String, t: Throwable): Unit = {
    logger.error(message.toString, t)
  }

  def error(message: => String): Unit = {
    logger.error(message.toString)
  }
}
