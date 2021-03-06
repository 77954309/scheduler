package com.lm.scheduler.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * @Classname DWCException
 * @Description TODO
 * @Date 2020/9/21 17:15
 * @Created by limeng
 */
public abstract class DWCException extends Exception {

    static String applicationName;
    static String hostname;
    static int hostPort;

    public static void setApplicationName(String applicationName) {
        DWCException.applicationName = applicationName;
    }

    public static void setHostname(String hostname) {
        DWCException.hostname = hostname;
    }

    public static void setHostPort(int hostPort) {
        DWCException.hostPort = hostPort;
    }

    /**
     * Errcode error code(errcode 错误码)
     * Desc error description(desc 错误描述)
     * Ip abnormal server ip(ip 发生异常的服务器ip)
     * Port An abnormal process port(port 发生异常的进程端口)
     * serviceKind microservice type with exception(serviceKind 发生异常的微服务类型)
     */
    private int errCode;
    private String desc;
    private String ip;
    private int port;
    private String serviceKind;
    public DWCException(int errCode, String desc){
        this(errCode, desc, hostname, hostPort, applicationName);
    }
    public DWCException(int errCode, String desc, String ip, int port, String serviceKind){
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
        retMap.put("level", getLevel().getLevel());
        retMap.put("errCode", getErrCode());
        retMap.put("desc", getDesc());
        retMap.put("ip", getIp());
        retMap.put("port", getPort());
        retMap.put("serviceKind", getServiceKind());
        return retMap;
    }

   /* public static Exception getDWCException(Map<String, Object>  errorMap) {

    }*/

    abstract ExceptionLevel getLevel();
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
