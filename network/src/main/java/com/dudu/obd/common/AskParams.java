package com.dudu.obd.common;

import java.io.Serializable;

/**
 * AskParams
 * 消息请求，发送参数
 * @author    Bob
 * @date      2015年12月3日 上午11:49:23
 */
public class AskParams implements Serializable {

    private static final long serialVersionUID = 1L;
    private String            auth;
    private String            context;

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }


    public void setContext(String context) {
        this.context = context;
    }
}
