package com.dudu.obd.common;

/**
 * 服务器回复客户端消息体，此消息体一般有服务端创建
 * @author    Bob
 * @date      2015年12月3日 上午11:58:41
 */
public class ReplyClientBody extends ReplyBody {

    private static final long serialVersionUID = -5860018507393423081L;
    private String            replyClientInfo;

    public ReplyClientBody(String replyClientInfo) {
        this.replyClientInfo = replyClientInfo;
    }

    public String getReplyClientInfo() {
        return replyClientInfo;
    }

    public void setReplyClientInfo(String replyClientInfo) {
        this.replyClientInfo = replyClientInfo;
    }

}
