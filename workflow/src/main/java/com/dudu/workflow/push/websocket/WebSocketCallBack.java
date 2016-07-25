package com.dudu.workflow.push.websocket;

import org.java_websocket.handshake.ServerHandshake;

/**
 * Created by dengjun on 2016/5/10.
 * Description :
 */
public interface WebSocketCallBack {
    public void onOpen(ServerHandshake handshakedata);

    public void onMessage(String message);

    public void onError(Exception ex);

    public void onClose(int code, String reason, boolean remote);
}
