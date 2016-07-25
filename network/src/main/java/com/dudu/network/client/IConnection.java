package com.dudu.network.client;

import com.dudu.network.valueobject.ConnectionParam;

/**
 * Created by dengjun on 2016/3/4.
 * Description :
 */
public interface IConnection<T> {
    public boolean isConnected();

    public void connect(ConnectionParam connectionParam);

    public void disConnect();

    public void setConnectCallBack(IConnectCallBack iConnectCallBack);

    public void sendMessage(T sendMessage);
}
