package com.lm.scheduler.exception;

/**
 * @Classname DwcCommonErrorException
 * @Description TODO
 * @Date 2020/9/21 17:17
 * @Created by limeng
 */
public class DwcCommonErrorException  extends ErrorException{
    public DwcCommonErrorException(int errCode, String desc) {
        super(errCode, desc);
    }

    public DwcCommonErrorException(int errCode, String desc, String ip, int port, String serviceKind) {
        super(errCode, desc, ip, port, serviceKind);
    }
}
