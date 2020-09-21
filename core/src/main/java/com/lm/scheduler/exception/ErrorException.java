package com.lm.scheduler.exception;

/**
 * @Classname ErrorException
 * @Description TODO
 * @Date 2020/9/21 17:17
 * @Created by limeng
 */
public class ErrorException extends DWCException {
    private ExceptionLevel level = ExceptionLevel.ERROR;
    public ErrorException(int errCode, String desc){
        super(errCode, desc);
    }
    public ErrorException(int errCode, String desc, String ip, int port, String serviceKind){
        super(errCode, desc, ip, port, serviceKind);
    }

    @Override
    public ExceptionLevel getLevel(){
        return this.level;
    }
}
