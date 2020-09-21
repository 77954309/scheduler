package com.lm.scheduler.exception;

/**
 * @Classname FatalException
 * @Description TODO
 * @Date 2020/9/21 17:20
 * @Created by limeng
 */
public class FatalException extends DWCException{
    private ExceptionLevel level = ExceptionLevel.FATAL;
    public FatalException(int errCode, String desc){
        super(errCode, desc);
    }
    public FatalException(int errCode, String desc, String ip, int port, String serviceKind){
        super(errCode, desc, ip, port, serviceKind);
    }

    @Override
    public ExceptionLevel getLevel(){
        return this.level;
    }

}
