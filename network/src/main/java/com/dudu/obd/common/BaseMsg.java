package com.dudu.obd.common;

import java.io.Serializable;

/**
 * 消息基础类，必须实现序列
 * @author    Bob
 * @date      2015年12月3日 上午11:45:37
 */
public abstract class BaseMsg implements Serializable {

    private static final long serialVersionUID = 1L;
    //消息类型
    private MsgType           type;
    //clientId 客户端连接的时候创建的，必须唯一，否者会出现channel调用混乱
    private String            clientId;
    //业务代码
    private BusinessCode      code;
    //消息ID
    private String            messageId;

    //初始化客户端id
    public BaseMsg() {
        this.clientId = Constants.getClientId();
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public MsgType getType() {
        return type;
    }

    public void setType(MsgType type) {
        this.type = type;
    }

    public BusinessCode getCode() {
        return code;
    }

    public void setCode(BusinessCode code) {
        this.code = code;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

}
