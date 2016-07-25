package com.dudu.android.launcher.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2016/4/23.
 */
public class NetWorkReceiver extends BroadcastReceiver {
    private Logger log;

    public NetWorkReceiver() {
        log = LoggerFactory.getLogger("init.receiver.net");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        if (wifiInfo != null) {
            if (activeInfo != null) {
                log.debug("mobile:{}, wifi:{}, active:{}", mobileInfo.isConnected(), wifiInfo.isConnected(), activeInfo.isConnected());
                if (mobileInfo.isConnected() || wifiInfo.isConnected()) {

                }
            }
        }
    }  //如果无网络连接activeInfo为null
}
