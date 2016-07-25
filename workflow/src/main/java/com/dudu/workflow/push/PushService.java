package com.dudu.workflow.push;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.DataJsonTranslation;
import com.dudu.commonlib.utils.NetworkUtils;
import com.dudu.rest.common.IpConfig;
import com.dudu.workflow.push.model.ReceiverPushData;
import com.dudu.workflow.push.model.UserInfo;
import com.dudu.workflow.push.receiver.SocketService;
import com.dudu.workflow.push.receiver.SocketServiceImpl;
import com.dudu.workflow.push.websocket.WebSocketCallBack;

import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by dengjun on 2016/5/10.
 * Description :
 */
public class PushService implements WebSocketCallBack {
    private SocketService socketService = null;
    private URI uri;

    private Subscription loginSubscription;
    private Subscription reConnectSubscription;


    private Logger log = LoggerFactory.getLogger("workFlow.webSocket");

    public PushService() {

    }

    public void init() {
        try {
            String addr = "ws://" + IpConfig.getSocketAddress() + ":" + IpConfig.getSocketPort() + "";
            log.info("初始化 webSocket地址：{}", addr);
            uri = new URI(addr);
            reConnectWebSocket(0);
        } catch (URISyntaxException e) {
            log.error("异常", e);
        }
    }

    public void release() {
        log.info("WebSocket 释放");
        cancerCreateConnection();
        cancerReConnect();
        disConnect();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        cancerReConnect();
        loginUntillSuccess();
    }

    @Override
    public void onMessage(String message) {
        log.info("收到推送消息：{}", message);
        ReceiverPushData receiverPushData = (ReceiverPushData) DataJsonTranslation.jsonToObject(message, ReceiverPushData.class);
        if (receiverPushData.result != null) {
            EventBus.getDefault().post(receiverPushData);
        } else {
            if (receiverPushData.resultCode == 0) {
                log.info("登录成功---");
//                cancerCreateConnection();
            }
        }
    }

    @Override
    public void onError(Exception ex) {
        log.error("异常：", ex);
        reConnectWebSocket(5);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (remote == true) {
            log.info("连接由于网络断开或者服务器关闭连接，需要重连");
            reConnectWebSocket(5);
        }
    }

    public void reConnectWebSocket(int seconds) {
        reConnectSubscription = Observable
                .timer(seconds, TimeUnit.SECONDS, Schedulers.newThread())
                .subscribe(l -> {
                    if (NetworkUtils.isNetworkConnected()) {
                        log.info("建立webSocket连接");
                        disConnect();
                        connect();
                    } else {
                        log.info("网络不通，10秒后重连");
                        reConnectWebSocket(10);
                    }
                }, throwable -> {
                    log.error("异常：", throwable);
                });
    }

    private void cancerReConnect() {
        if (reConnectSubscription != null) {
            reConnectSubscription.unsubscribe();
        }
    }

    public void connect() {
        if (socketService == null) {
            socketService = new SocketServiceImpl(uri);
        }
        socketService.setWebSocketCallBack(this);
        socketService.openSocketService();
    }

    public void disConnect() {
        if (socketService != null) {
            socketService.closeSocketService();
            socketService = null;
        }
    }

    private void loginUntillSuccess() {
        log.info("interval.io.create loginUntillSuccess");
        loginSubscription = Observable
                .interval(0, 240, TimeUnit.SECONDS, Schedulers.io())
                .subscribe(l -> {
                    login();
                }, throwable -> {
                    log.error("interval.io loginUntillSuccess 异常：", throwable);
                });
    }

    private void cancerCreateConnection() {
        if (loginSubscription != null) {
            loginSubscription.unsubscribe();
        }
    }

    private void login() {
        if (socketService != null && socketService.isOpen()) {
            log.info("interval.io dudu websocket 登录");
            socketService.sendMessage(DataJsonTranslation.objectToJson(new UserInfo("dudu_websocket", "dudu20150806", CommonLib.getInstance().getObeId())));
        } else {
            reConnectWebSocket(5);
        }
    }


}
