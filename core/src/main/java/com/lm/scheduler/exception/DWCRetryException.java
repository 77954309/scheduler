package com.lm.scheduler.exception;

/**
 * @Classname DWCRetryException
 * @Description TODO
 * @Date 2020/9/21 17:17
 * @Created by limeng
 */
public class DWCRetryException extends DWCException  {
    DWCRetryException(int errCode, String desc, String ip, int port, String serviceKind) {
        super(errCode, desc, ip, port, serviceKind);
    }

    public DWCRetryException(int errCode, String desc) {
        super(errCode, desc);
    }

    @Override
    ExceptionLevel getLevel() {
        return ExceptionLevel.RETRY;
    }
}
