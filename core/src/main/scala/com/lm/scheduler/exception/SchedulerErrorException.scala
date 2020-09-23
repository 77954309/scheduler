package com.lm.scheduler.exception

/**
 * @Classname SchedulerErrorException
 * @Description TODO
 * @Date 2020/9/23 19:54
 * @Created by limeng
 */
class SchedulerErrorException(errCode: Int, desc: String) extends ErrorException(errCode,desc) {

}
