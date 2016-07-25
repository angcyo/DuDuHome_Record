package com.dudu.network.message;

import com.dudu.commonlib.CommonLib;
import com.dudu.network.message.id.Bicker;
import com.dudu.network.message.id.BusinessMessageEnum;
import com.dudu.obd.common.LoginMsg;

/**
 * Created by dengjun on 2016/3/4.
 * Description :
 */
public class LoginMessage extends MessagePackage<LoginMsg> {
    private LoginMsg loginMsg;

    public LoginMessage(String userName, String password) {
        loginMsg = new LoginMsg();
        loginMsg.setUserName(userName);
        loginMsg.setPassword(password);

        loginMsg.setMessageId(Bicker.getBusinessCode(BusinessMessageEnum.LOGIN_DATA.getCode()));
        loginMsg.setObeId(CommonLib.getInstance().getObeId());
    }

    @Override
    public void setMessageId(String messageId) {

    }

    @Override
    public String getMessageId() {
        return loginMsg.getMessageId();
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

        return loginMsg.getUserName()+loginMsg.getPassword()+loginMsg.getObeId();
    }

    @Override
    public boolean isNeedCache() {
        return false;
    }

    @Override
    public LoginMsg getMessageEntity() {
        return loginMsg;
    }

    @Override
    public void setMessageEntity(LoginMsg messageEntity) {

    }

    @Override
    public String getBusinessCode() {
        return null;
    }

    @Override
    public void setBusinessCode(String businessCode) {

    }

}
