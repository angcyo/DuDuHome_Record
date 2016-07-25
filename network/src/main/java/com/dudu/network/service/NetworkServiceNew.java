package com.dudu.network.service;


import com.dudu.commonlib.utils.NetworkUtils;
import com.dudu.network.client.IConnectCallBack;
import com.dudu.network.client.IConnection;
import com.dudu.network.client.netty.NettyConnection;
import com.dudu.network.message.LoginMessage;
import com.dudu.network.message.MessagePackage;
import com.dudu.network.msghandler.MessageHandler;
import com.dudu.network.msghandler.MessagePackagesQueue;
import com.dudu.network.storage.Storage;
import com.dudu.network.utils.Encrypt;
import com.dudu.network.valueobject.ConnectionParam;
import com.dudu.network.valueobject.ConnectionState;
import com.dudu.obd.common.AskMsg;
import com.dudu.obd.common.BaseMsg;
import com.dudu.obd.common.MsgType;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by dengjun on 2016/3/4.
 * Description :
 */
public class NetworkServiceNew implements IConnectCallBack<BaseMsg> {
    //阻塞队列，用于存放要发送的消息
    private MessagePackagesQueue messagePackagesQueue;

    private ConnectionParam connectionParam;
    private IConnection iConnection;

    private String authToken = "";
    //当前发送的消息包
    private MessagePackage curSendMessagePackage;
    //发送数据后，上锁， 发送数据成功后释放锁
    private String sendMessageLock = "SendMessageLock";
    /* 消息处理*/
    private MessageHandler messageHandler;

    private StorageMessageHandler storageMessageHandler;

    private ScheduledExecutorService sendThreadPool = null;
    //发送数据线程运行标记
    private boolean sendThreadRunFlag = false;
    private String sendThreadLock = "sendThreadLock";

    /* 登录状态，设备发送其他消息前，需要先登录*/
    private boolean isLogined = false;
    private Subscription logingSubscription;

    private Subscription guardSubscription;

    private Logger log;

    public NetworkServiceNew() {
        log = LoggerFactory.getLogger("network");
        connectionParam = new ConnectionParam();

        messagePackagesQueue = new MessagePackagesQueue<MessagePackage>(600);
        messageHandler = new MessageHandler(this);
        storageMessageHandler = new StorageMessageHandler(this);
    }


    private void startSendThread(){
        new Thread(()->{
            while (sendThreadRunFlag) {
                try {
                    if (isLogined()) {
                        sendMessage();
                    } else {
                        sendThreadWait();
                    }
                    checkAndStorageMessage();
                } catch (Exception e) {
                    log.error("异常:", e);
                }
            }
        }).start();
    }

    private void sendMessage() {
        MessagePackage messagePackage = (MessagePackage) messagePackagesQueue.getQueueHeadMessage();
        if (iConnection.isConnected()){
            sendMessageReal(messagePackage);
            if (messagePackage.isNeedWaitResponse())
                waitResponse();//等待响应
        }
    }

    private void waitResponse() {
        try {//是否需要等待，后续待定
            synchronized (sendMessageLock) {
                log.debug("----发送消息后--等待响应---");
                sendMessageLock.wait(30 * 1000);//后续做时间控制,
            }
        } catch (InterruptedException e) {
            log.error("异常:", e);
        }
    }

    /* 通知发送线程可以发送消息了*/
    public void notifySendThread() {
        synchronized (sendThreadLock) {
            log.debug("通知发送线程，可以发送消息了--------");
            sendThreadLock.notify();
        }
    }

    /* 发送线程等待*/
    private void sendThreadWait() throws InterruptedException {
        synchronized (sendThreadLock) {
            log.debug("----发送线程等待---：");
            sendThreadLock.wait(30 * 1000);
        }
    }

    //通知可以发送下一条
    public void nodifyReceiveResponse() {
        synchronized (sendMessageLock) {
            log.debug("发送数据收到响应，通知可以发送下一条--------");
            sendMessageLock.notifyAll();//通知可以发送下一条数据了
        }
    }


    public void init() {
        cancerGuard();
        sendThreadRunFlag = true;
        startGuard();
        startSendThread();
    }

    public void release() {
        sendThreadRunFlag = false;
        if (logingSubscription != null) {
            logingSubscription.unsubscribe();
            logingSubscription = null;
        }
        cancerGuard();
        disConnect();
    }


    private void sendMessageReal(MessagePackage messagePackage) {
        curSendMessagePackage = messagePackage;
        String sendMessage = messagePackage.toJsonString();
        log.info("发送消息内容：{}", new Gson().toJson(messagePackage.getMessageEntity()));
        try {
            if (((BaseMsg) messagePackage.getMessageEntity()).getType().toString().equals(MsgType.ASK.toString())) {
                ((AskMsg) messagePackage.getMessageEntity()).getParams().setAuth(authToken);
            }
            iConnection.sendMessage(messagePackage.getMessageEntity());
        } catch (Exception e) {
            log.error("异常:", e);
        }
    }

    public void removeHeadOfMessageQueue() {
        if (messagePackagesQueue.size() >= 1) {
            MessagePackage messagePackage = (MessagePackage) messagePackagesQueue.peek();
            if (messagePackage.equals(curSendMessagePackage)) {
                log.debug("删除队列头----------");
                messagePackagesQueue.remove();
            }
        }
    }

    /*发送登录消息 */
    public void sendLoginMessage() {
        curSendMessagePackage = new LoginMessage(Encrypt.MD5Encode("dudu"), Encrypt.MD5Encode("dudu@123456"));
        sendMessageReal(curSendMessagePackage);

        logingSubscription = Observable.timer(60, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                if (!isLogined()) {
                    log.info("再次发送登录消息----");
                    sendLoginMessage();
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                log.error("sendLoginMessage", throwable);
            }
        });
    }

    public void sendMessage(MessagePackage messagePackage) {
//        log.debug("消息队列加入消息：messageId = {}, {}", messagePackage.getMessageId(), messagePackage.toJsonString());
        messagePackagesQueue.put(messagePackage);
    }

    public void disConnect(){
        log.info("断开网络连接----");
        setIsLogined(false);
        if (iConnection != null){
            iConnection.disConnect();
        }
    }


    private void startGuard(){
        log.info("interval.io.create startGuard");
        guardSubscription = Observable
                .interval(0, 30,TimeUnit.SECONDS, Schedulers.io())
                .subscribe(l->{
                    guardAction();
                },throwable -> {
                    log.error("interval.io 异常:", throwable);
                });
    }

    private void cancerGuard(){
        if(guardSubscription != null){
            guardSubscription.unsubscribe();
        }
    }

    private void guardAction(){
        try {
            if (NetworkUtils.isNetworkConnected()){
                if (iConnection == null) {
                    initConnection();
                }
                log.debug("interval.io 运行keepAliveThread  网络状态：{}", iConnection.isConnected());
                if (iConnection.isConnected() == false) {
                    reConnect();
                }
                if (isLogined()){
                    storageMessageHandler.proStorageMessage();
                }
            }
        } catch (Exception e) {
            log.error("interval.io 异常:", e);
        }
    }

    public void initConnection() {
        log.info("初始化网络连接");
        iConnection = new NettyConnection();
        iConnection.setConnectCallBack(NetworkServiceNew.this);
    }

    public void reConnect() {
        setIsLogined(false);
        iConnection.disConnect();
        log.info("守护线程重连网络---");
        iConnection.connect(connectionParam);
    }

    @Override
    public void onConnectionState(ConnectionState connectionState) {
        log.info("网络状态：" + connectionState.connectionState);
        switch (connectionState.connectionState) {
            case ConnectionState.CONNECTION_CREATE:
                break;
            case ConnectionState.CONNECTION_FAIL://当连接被关闭的时候，此方法被调用。
            case ConnectionState.CONNECTION_IDLE://默认情况不会有限制状态
                break;
            case ConnectionState.CONNECTION_SUCCESS:
                sendLoginMessage();
                break;
            default:
                break;
        }
    }

    @Override
    public void onReceive(BaseMsg messageReceived) {
        messageHandler.processReceivedMessage(messageReceived);
    }

    //检查是否需要对消息队列的数据进行持久化处理，如果大小大于200条就进行持久化
    private void checkAndStorageMessage() {
        if (messagePackagesQueue.size() > 30){
            for (int i = 0; i < messagePackagesQueue.size(); i++) {
                MessagePackage messagePackage = (MessagePackage) messagePackagesQueue.remove();
                if (messagePackage.isNeedCache()){
                    String messageToStorage = messagePackage.toJsonString();
//                    log.debug("持久化消息：{}", messagePackage.getBusinessCode() + "@" + messageToStorage);
                    Storage.getInstance().saveData(messagePackage.getBusinessCode() + "@" + messageToStorage);
                }
                Storage.getInstance().flush();
                log.info("消息持久化----完成");
            }
        }
    }



    public synchronized boolean isLogined() {
        return isLogined;
    }

    public synchronized void setIsLogined(boolean isLogined) {
        this.isLogined = isLogined;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public ConnectionParam getConnectionParam() {
        return connectionParam;
    }

}
