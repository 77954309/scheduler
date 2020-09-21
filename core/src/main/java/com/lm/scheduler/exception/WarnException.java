package com.lm.scheduler.exception;

/**
 * @Classname WarnException
 * @Description TODO
 * @Date 2020/9/21 17:21
 * @Created by limeng
 */
public class WarnException extends DWCRuntimeException{
    private ExceptionLevel level = ExceptionLevel.WARN;
    public WarnException(int errCode, String desc){
        super(errCode, desc);
    }
    public WarnException(int errCode, String desc, String ip, int port, String serviceKind){
        super(errCode, desc, ip, port, serviceKind);
    }

    @Override
    public ExceptionLevel getLevel(){
        return this.level;
    }
}
