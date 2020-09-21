package com.lm.scheduler.exception;

import java.util.HashMap;
import java.util.Map;

import static com.lm.scheduler.exception.DWCException.applicationName;
import static com.lm.scheduler.exception.DWCException.hostPort;
import static com.lm.scheduler.exception.DWCException.hostname;
/**
 * @Classname DWCRuntimeException
 * @Description TODO
 * @Date 2020/9/21 17:18
 * @Created by limeng
 */
public abstract class DWCRuntimeException extends RuntimeException {
        private int errCode;
        private String desc;
        private String ip;
        private int port;
        private String serviceKind;

    public DWCRuntimeException(int errCode, String desc){
            this(errCode, desc, hostname, hostPort, applicationName);
        }
    public DWCRuntimeException(int errCode, String desc, String ip, int port, String serviceKind){
            super("errCode: " + errCode + " ,desc: " + desc + " ,ip: " + ip +
                    " ,port: " + port + " ,serviceKind: " + serviceKind);
            this.errCode = errCode;
            this.desc = desc;
            this.ip = ip;
            this.port = port;
            this.serviceKind = serviceKind;
        }

        public int getErrCode() {
            return errCode;
        }

        public void setErrCode(int errCode) {
            this.errCode = errCode;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getServiceKind() {
            return serviceKind;
        }

        public void setServiceKind(String serviceKind) {
            this.serviceKind = serviceKind;
        }

        public Map<String, Object> toMap(){
            Map<String, Object> retMap = new HashMap<String, Object>();
            retMap.put("errCode", getErrCode());
            retMap.put("desc", getDesc());
            retMap.put("ip", getIp());
            retMap.put("port", getPort());
            retMap.put("level", getLevel().getLevel());
            retMap.put("serviceKind", getServiceKind());
            return retMap;
        }

        public abstract ExceptionLevel getLevel();

        @Override
        public String toString() {
            return "DWCException{" +
                    "errCode=" + errCode +
                    ", desc='" + desc + '\'' +
                    ", ip='" + ip + '\'' +
                    ", port=" + port +
                    ", serviceKind='" + serviceKind + '\'' +
                    '}';
        }

    }
