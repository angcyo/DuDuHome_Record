package com.dudu.network.client;

import com.dudu.network.valueobject.ConnectionState;

/**
 * Created by dengjun on 2015/11/27.
 * Description :
 */
public interface IConnectCallBack<T> {
    //连接状态回调
    public void onConnectionState(ConnectionState connectionState);

    //接收数据回调
    public void onReceive(T messageReceived);

//    //发送消息成功回调
//    public void onMessageSent(String messageSent);
}
