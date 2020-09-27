package com.lm.scheduler.executor

import com.lm.protocol.engine.JobProgressInfo

/**
 * @Classname SingleTaskInfoSupport
 * @Description TODO
 * @Date 2020/9/25 17:03
 * @Created by limeng
 */
trait SingleTaskInfoSupport {
    def progress(): Float
    def getProgressInfo: Array[JobProgressInfo]
    def log():String
}
