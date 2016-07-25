package com.dudu.obd.common;

public enum BusinessCode {
    GPS_UPLOAD("1001"), //GPS数据上传服务
    OBD_UPLOAD("1002"), //OBD综合数据上传服务
    DRIVE_HABIT_UPLOAD("1003"), //车辆驾驶习惯数据上传服务
    DATA_UPLOAD("1004"), //使用流量上报服务
    TIRE_PRESSURE_UPLOAD("1005"), //胎压数据上报服务

    DATA_SWITCH_PUSH("2001"), //流量开关推送指令服务
    DATA_ALARM_PUSH("2002"), //流量超限预警推送指令服务
    DATA_EXCEPTION_PUSH("2003"), //流量异常预警推送指令服务
    UPDATE_PORTAL_PUSH("2004"), //更新Portal推送指令服务
    GET_LOG_PUSH("2005"); //获取设备日志推送指令服务

    private String code;

    BusinessCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
