package com.dudu.workflow.push.receiver;

import com.dudu.workflow.push.websocket.WebSocketCallBack;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * Created by Administrator on 2016/3/5.
 */
public class SocketServiceImpl extends WebSocketClient implements SocketService {
    private Logger log= LoggerFactory.getLogger("workFlow.webSocket");

    private WebSocketCallBack webSocketCallBack = null;


    public SocketServiceImpl(URI serverURI) {
        super(serverURI, new Draft_10());
    }


    @Override
    public boolean isOpen() {
        return getConnection().isOpen();
    }

    @Override
    public void openSocketService(){
        log.info("连接 websocket");
        connect();
    }

    @Override
    public void closeSocketService() {
        log.info("关闭 websocket");
        try {
                close();
        } catch (Exception e) {
            log.error("异常", e);
        }
    }

    @Override
    public void sendMessage(String jsonString) {
        if (getConnection().isOpen()){
            log.info("发送消息：{}", jsonString);
            send(jsonString);
        }
    }

    @Override
    public void setWebSocketCallBack(WebSocketCallBack webSocketCallBack) {
        this.webSocketCallBack = webSocketCallBack;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("onOpen----");
        if (webSocketCallBack != null){
            webSocketCallBack.onOpen(handshakedata);
        }
    }

    @Override
    public void onMessage(String message) {
        if (webSocketCallBack != null){
            webSocketCallBack.onMessage(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("onClose code：{}， resason：{}， remote：{}", code, reason, remote);
        if (webSocketCallBack != null){
            webSocketCallBack.onClose(code, reason, remote);
        }
    }

    @Override
    public void onError(Exception ex) {
        if (webSocketCallBack != null){
            webSocketCallBack.onError(ex);
        }
    }
}
