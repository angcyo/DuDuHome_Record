package com.dudu.obd.common;

public enum ReturnCode {
    LOGIN_SUCCESS("1000"), //登录成功
    RECIEVE_SUCCESS("1001"), //接收成功
    OTHER("9999"); //其它错误

    private String code;

    ReturnCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
