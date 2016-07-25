package com.dudu.network.msghandler;

import com.dudu.network.message.GeneralResponseMessage;
import com.dudu.network.service.NetworkServiceNew;
import com.dudu.obd.common.BaseMsg;
import com.dudu.obd.common.MsgType;
import com.dudu.obd.common.ReplyClientBody;
import com.dudu.obd.common.ReplyClientLoginBody;
import com.dudu.obd.common.ReplyMsg;
import com.dudu.obd.common.ReturnCode;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;

/**
 * Created by dengjun on 2016/3/4.
 * Description :
 */
public class MessageHandler {
    private NetworkServiceNew networkServiceNew;

    private Logger log;

    public MessageHandler(NetworkServiceNew networkServiceNew) {
        this.networkServiceNew = networkServiceNew;
        log = LoggerFactory.getLogger("network");
    }

    //处理收到的消息
    public void processReceivedMessage(BaseMsg messageReceived) {
        try {
            MsgType msgType = messageReceived.getType();
//            log.debug("收到消息 {}", msgType.toString());

            switch (msgType) {
                case PING:
                    break;
                case ASK:
                    break;
                case REPLY:
                    ReplyMsg replyMsg = (ReplyMsg) messageReceived;
                    Object replyObj = (Object) replyMsg.getBody();
                    if (replyObj instanceof ReplyClientBody) {
                        proGeneralResponse(replyMsg);
                    } else if (replyObj instanceof ReplyClientLoginBody) {
                        proLoginMessage(replyObj);
                    }
                    break;
            }
        } catch (Exception e) {
            log.error("异常:", e);
        }
    }

    private void proLoginMessage(Object messagePackage) {
        ReplyClientLoginBody clientLoginBody = (ReplyClientLoginBody) messagePackage;
        String authToken = clientLoginBody.getAuthToken();
        ReturnCode returnCode = clientLoginBody.getReturnCode();
        log.debug("authToken：{},  returnCode：{}", authToken, returnCode.getCode());
        if (returnCode.getCode().equals(ReturnCode.LOGIN_SUCCESS.getCode())) {
            log.info("登录成功----");
            networkServiceNew.setAuthToken(clientLoginBody.getAuthToken());
            networkServiceNew.setIsLogined(true);
            networkServiceNew.notifySendThread();
        } else {
            Observable.timer(30, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
                @Override
                public void call(Long aLong) {
                    log.info("再次发送登录消息----");
                    networkServiceNew.sendLoginMessage();
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    log.error("proLoginMessage", throwable);
                }
            });
        }
    }

    private void proGeneralResponse(Object messagePackage) {
        log.info("network-处理proGeneralResponse事件");
        GeneralResponseMessage generalResponseMessage = new GeneralResponseMessage();
        generalResponseMessage.setMessageEntity((ReplyMsg) messagePackage);

        log.debug("收到响应  = {}", new Gson().toJson(generalResponseMessage));

//        log.debug("收到响应消息：{);

        if (ReturnCode.RECIEVE_SUCCESS.getCode().equals(generalResponseMessage.getMessageEntity().getBody().getReturnCode().getCode())) {
            networkServiceNew.removeHeadOfMessageQueue();
        }
        networkServiceNew.nodifyReceiveResponse();
    }
}
