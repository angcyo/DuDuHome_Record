package com.dudu.android.launcher;

import android.app.Application;
import android.support.multidex.MultiDex;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.process.ProcessUtils;
import com.dudu.drivevideo.frontcamera.FrontCameraManage;
import com.dudu.drivevideo.rearcamera.RearCameraManage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LauncherApplication extends Application {

    public static LauncherApplication mApplication;

    private static Logger logger;
    private boolean  initedFlag = false;

    public static boolean startRecord = false;

    private boolean mReceivingOrder = false;

    private boolean needSaveVoice = false;

    public boolean isNeedSaveVoice() {
        return needSaveVoice;
    }

    public void setNeedSaveVoice(boolean needSaveVoice) {

        this.needSaveVoice = needSaveVoice;
    }

    public static LauncherApplication getContext() {
        return mApplication;
    }

    @Override
    public void onCreate() {
        MultiDex.install(this);

        super.onCreate();
        mApplication = this;
        if (initedFlag == false){
            CommonLib.getInstance().init(this);//这个不能放到initManager里面，context需要为application
            initedFlag = true;
        }


        logger = LoggerFactory.getLogger("init.application");
        logger.debug("正在初始化application");
        if ("com.dudu.android.launcher".equals(ProcessUtils.getCurProcessName(CommonLib.getInstance().getContext()))){
            FrontCameraManage.getInstance().init();//录像的邓军方案
            // 打开后拉摄像
            RearCameraManage.getInstance().init();
        }
        logger.info("当前进程：{}", ProcessUtils.getCurProcessName(CommonLib.getInstance().getContext()));
    }


    public boolean isReceivingOrder() {
        return mReceivingOrder;
    }

    public void setReceivingOrder(boolean receivingOrder) {
        mReceivingOrder = receivingOrder;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }
}

