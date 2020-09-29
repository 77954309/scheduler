package com.lm.scheduler.utils

import java.text.SimpleDateFormat
import java.util.Date

/**
 * @Classname LogUtils
 * @Description TODO
 * @Date 2020/9/28 20:40
 * @Created by limeng
 */
object LogUtils {
  private def getTimeFormat:String = {
    val simpleDateFormat:SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.mmm")
    val now = new Date(System.currentTimeMillis())
    simpleDateFormat.format(now)
    //now.toString(ISODateTimeFormat.yearMonthDay()) + " " + now.toString(ISODateTimeFormat.hourMinuteSecondMillis())
  }

  def generateInfo(rawLog:String):String = {
    getTimeFormat + " " + "INFO" + " " + rawLog
  }

  def generateERROR(rawLog:String):String = {
    getTimeFormat + " " + "ERROR" + " " + rawLog
  }

  def generateWarn(rawLog:String):String = {
    getTimeFormat + " " + "WARN" + " " + rawLog
  }

  def generateSystemInfo(rawLog:String):String = {
    getTimeFormat + " " + "SYSTEM-INFO" + " " + rawLog
  }

  def generateSystemError(rawLog:String):String = {
    getTimeFormat + " " + "SYSTEM-ERROR" + " " + rawLog
  }

  def generateSystemWarn(rawLog:String):String = {
    getTimeFormat + " " + "SYSTEM-WARN" + " " + rawLog
  }

}
