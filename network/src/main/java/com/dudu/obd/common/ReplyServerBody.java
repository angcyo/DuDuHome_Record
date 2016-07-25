package com.dudu.obd.common;

/**
 * ReplyServerBody
 * 客户端回复服务器消息体，此消息体一般有客户端创建
 * @author    Bob
 * @date      2015年12月3日 上午11:58:10
 */
public class ReplyServerBody extends ReplyBody {

    private static final long serialVersionUID = 1L;
    private String            replyServerInfo;

    public ReplyServerBody(String replyServerInfo) {
        this.replyServerInfo = replyServerInfo;
    }

    public String getReplyServerInfo() {
        return replyServerInfo;
    }

    public void setReplyServerInfo(String replyServerInfo) {
        this.replyServerInfo = replyServerInfo;
    }

}
