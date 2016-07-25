package com.dudu.network.message;

import com.dudu.obd.common.BaseMsg;
import com.dudu.obd.common.ReplyMsg;

/**
 * Created by dengjun on 2016/3/8.
 * Description :
 */
public class GeneralResponseMessage extends MessagePackage<ReplyMsg> {
    private  ReplyMsg replyMsg;

    @Override
    public void setMessageId(String messageId) {

    }

    @Override
    public String getMessageId() {
        return replyMsg.getMessageId();
    }

    @Override
    public boolean isNeedWaitResponse() {
        return false;
    }

    @Override
    public boolean isNeedEncrypt() {
        return false;
    }

    @Override
    public void createFromJsonString(String messageJsonString) {

    }

    @Override
    public String toJsonString() {
        return null;
    }

    @Override
    public boolean isNeedCache() {
        return false;
    }

    @Override
    public ReplyMsg getMessageEntity() {
        return replyMsg;
    }

    @Override
    public void setMessageEntity(ReplyMsg messageEntity) {
        this.replyMsg = messageEntity;
    }



    @Override
    public String getBusinessCode() {
        return null;
    }

    @Override
    public void setBusinessCode(String businessCode) {

    }
}
