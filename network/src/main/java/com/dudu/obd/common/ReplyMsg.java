package com.dudu.obd.common;

/**
 * ReplyMsg
 * 回复请求消息类型
 * @author    Bob
 * @date      2015年12月3日 上午11:54:43
 */
public class ReplyMsg extends BaseMsg {

    private static final long serialVersionUID = 3478306460781089421L;

    public ReplyMsg() {
        super();
        setType(MsgType.REPLY);
    }

    private ReplyBody body;

    public ReplyBody getBody() {
        return body;
    }

    public void setBody(ReplyBody body) {
        this.body = body;
    }
}
