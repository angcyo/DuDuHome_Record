package com.dudu.workflow.push.receiver;

import com.dudu.workflow.push.websocket.WebSocketCallBack;

/**
 * Created by Administrator on 2016/3/3.
 */
public interface SocketService {
    public boolean isOpen();

    public void openSocketService();

    public void closeSocketService();

    public void sendMessage(String jsonString);

    public void setWebSocketCallBack(WebSocketCallBack webSocketCallBack);
}
