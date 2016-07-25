package com.dudu.service;

import android.content.Context;

import com.dudu.android.launcher.utils.CarStatusUtils;
import com.dudu.android.launcher.utils.StatusBarManager;
import com.dudu.commonlib.CommonLib;
import com.dudu.monitor.Monitor;
import com.dudu.network.NetworkManage;
import com.dudu.service.storage.StorageSpaceService;
import com.dudu.weather.WeatherStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Subscription;

/**
 * Created by dengjun on 2016/2/29.
 * Description :
 */
public class ServiceManage {
    private static ServiceManage instance = null;

    private Logger log;
    private Context context;

    private StorageSpaceService storageSpaceService = new StorageSpaceService();

    Subscription flamoutSubscription;

    private boolean blueToothOpenFlag = false;

    public static ServiceManage getInstance() {
        if (instance == null) {
            synchronized (ServiceManage.class) {
                if (instance == null) {
                    instance = new ServiceManage();
                }
            }
        }
        return instance;
    }

    private ServiceManage() {
        log = LoggerFactory.getLogger("init.service");
        context = CommonLib.getInstance().getContext();
    }

    public void init() {
        new Thread() {
            @Override
            public void run() {
                try {
                    initModules();
                } catch (Exception e) {
                    log.error("异常", e);
                }
            }
        }.start();
    }

    public void release() {

        new Thread() {
            @Override
            public void run() {
                try {
                    releaseModules();
                } catch (Exception e) {
                    log.error("异常", e);
                }
            }
        }.start();
    }

    private void initModules() {
        log.debug("初始化各个模块服务------");

        CarStatusUtils.saveCarStatus(true);
//        WifiApAdmin.startWifiAp(context);
//        if (flamoutSubscription != null) {
//            flamoutSubscription.unsubscribe();
//        }

        NetworkManage.getInstance().init();

        StatusBarManager.getInstance().initBarStatus();


        WeatherStream.getInstance().startService();

        storageSpaceService.start();
    }


    private void releaseModules() {
        log.debug("结束各个模块服务-------");

        Monitor.getInstance().stopWork();
        storageSpaceService.stop();
//        ObdInit.getInstance().release();//蓝牙模块保持开着，接收obd数据，唤醒主服务

        NetworkManage.getInstance().release();

        CarStatusUtils.saveCarStatus(false);
    }

}
