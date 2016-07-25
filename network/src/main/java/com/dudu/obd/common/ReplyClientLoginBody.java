package com.dudu.obd.common;

/**
 * 回复客服端登录消息的消息体
 * @author    Bob
 * @date      2015年12月5日 上午11:45:10
 */
public class ReplyClientLoginBody extends ReplyBody {

    private static final long serialVersionUID = 1420499849396586532L;

    //鉴权token值
    private String            authToken;

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

}
