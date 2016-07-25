package com.dudu.network;

import com.dudu.network.d01code.message.MessagePackage;
import com.dudu.network.service.NetworkServiceNew;
import com.dudu.network.utils.IPConfig;
import com.dudu.network.valueobject.ConnectionParam;

/**
 * Created by dengjun on 2015/11/27.
 * Description :
 */
public class NetworkManage {
    private static NetworkManage instance = null;

    private NetworkServiceNew networkServiceNew;

    public static  NetworkManage getInstance(){
        if (instance == null){
            synchronized (NetworkManage.class){
                if (instance == null){
                    instance = new NetworkManage();
                }
            }
        }
        return instance;
    }

    private NetworkManage(){
        networkServiceNew = new NetworkServiceNew();
    }

    public void init(){
        IPConfig.getInstance().init();
        initParam();
        networkServiceNew.init();
    }

    public void release(){
        networkServiceNew.release();
    }

    public void reConnect(){
        networkServiceNew.disConnect();
    }

    public ConnectionParam getConnectionParam(){
        return networkServiceNew.getConnectionParam();
    }

    //此发送方法把数据丢到阻塞队列里面，最好不要放在UI线程中运行
    public void sendMessage(MessagePackage messagePackage){

    }

    public void sendMessage(com.dudu.network.message.MessagePackage messagePackage){
        networkServiceNew.sendMessage(messagePackage);
    }

    private void initParam() {
        if (IPConfig.getInstance().isTest_Server()) {
            networkServiceNew.getConnectionParam().setHost(IPConfig.getInstance().getTestServerIP());
            networkServiceNew.getConnectionParam().setPort(IPConfig.getInstance().getTestServerPort());
        } else {;
            networkServiceNew.getConnectionParam().setHost(IPConfig.getInstance().getServerIP());
            networkServiceNew.getConnectionParam().setPort(IPConfig.getInstance().getServerPort());
        }
    }
}
