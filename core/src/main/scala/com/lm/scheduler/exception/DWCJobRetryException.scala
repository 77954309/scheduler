package com.lm.scheduler.exception

/**
 * @Classname DWCJobRetryException
 * @Description TODO
 * @Date 2020/9/23 19:53
 * @Created by limeng
 */
class DWCJobRetryException(desc: String) extends DWCRetryException(DWCJobRetryException.JOB_RETRY_ERROR_CODE, desc) {

}
object DWCJobRetryException {
  val JOB_RETRY_ERROR_CODE = 25000
}
