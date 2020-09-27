package com.lm.protocol.engine

/**
 * @Classname ResponseTaskExecute
 * @Description TODO
 * @Date 2020/9/25 17:03
 * @Created by limeng
 */
case class JobProgressInfo(id: String, totalTasks: Int, runningTasks: Int, failedTasks: Int, succeedTasks: Int)