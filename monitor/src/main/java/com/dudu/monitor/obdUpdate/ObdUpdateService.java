package com.dudu.monitor.obdUpdate;

import com.dudu.commonlib.event.Events;
import com.dudu.monitor.obd.ObdManage;
import com.dudu.monitor.obdUpdate.config.ObdUpdateConstants;
import com.dudu.workflow.obd.OBDStream;
import com.dudu.workflow.obd.ObdUpdateFlow;
import com.dudu.workflow.obd.RobberyFlow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by dengjun on 2016/5/12.
 * Description :
 */
public class ObdUpdateService implements ObdUpdateUtils.ObdUpdateCallback {
    private static ObdUpdateService instance = null;

    private String curObdVersion = "";

    private boolean updateFlag = false;
    private ObdUpdateUtils obdUpdateUtils;
    private ObdBinFetchUtils obdBinFetchUtils;

    public Logger log = LoggerFactory.getLogger("car.obdUpdate");

    private Subscription readVersionSubscription;
    private Subscription delaySubscription;

    private ObdUpdateService() {
        obdUpdateUtils = new ObdUpdateUtils();
        obdUpdateUtils.setObdUpdateCallback(this);
        obdBinFetchUtils = new ObdBinFetchUtils();
    }

    public void init() {
        ObdBinFetchUtils.copyObdBin();
        delayQueryObdVersion(0);
        delayQueryServerVersion(60);
    }

    public void release() {
        cancerQueryVersion();
    }

    public void updateObdBin() {
        if (updateFlag == false) {
            updateFlag = true;
            cancerQueryVersion();
            obdUpdateUtils.updateObdBin(curObdVersion);
        } else {
            log.info("obd正在在升级----------------------");
        }
    }

    /* 强制升级接口，用于调试*/
    public void hardUpdateObdbin() {
        if (updateFlag == false) {
            updateFlag = true;
            cancerQueryVersion();
            obdUpdateUtils.updateObdBin(ObdUpdateConstants.hardUpdateObdBinVersion);
        } else {
            log.info("obd正在在升级----------------------");
        }
    }

    public boolean isUpdateIng() {
        return updateFlag;
    }

    public String getObdVersion() {
        return curObdVersion;
    }

    public Subscription delayQueryServerVersion(int seconds) {
        log.info("延时：{}秒查询服务器版本信息", seconds);
        return Observable
                .timer(seconds, TimeUnit.SECONDS, Schedulers.io())
                .subscribe(l -> {
                    queryVersion();
                }, throwable -> {
                    log.error("异常", throwable);
                });
    }

    private void queryVersion() {
        if (updateFlag == false) {
            log.info("obd没有在升级，查询服务器配置的版本信息");
            obdBinFetchUtils.queryObdBinVersion(curObdVersion);
        } else {
            log.info("obd正在在升级，暂时不查询服务器配置的版本信息");
        }
    }

    @Override
    public void onResult(boolean updateResult, String obdBinVersionUpdated) {
        log.info("升级结果：{}， 升级后的版本号：{}", updateResult, obdBinVersionUpdated);
        updateFlag = false;
        if (updateResult == true) {
            if (!"".equals(obdBinVersionUpdated)) {
                curObdVersion = obdBinVersionUpdated;
            }
            EventBus.getDefault().post(new Events.DeviceEvent(Events.REBOOT));
        }
    }


    private void delayQueryObdVersion(int seconds) {
        log.info("延时：{}秒读取版本号", seconds);
        delaySubscription = Observable
                .timer(seconds, TimeUnit.SECONDS, Schedulers.computation())
                .subscribe(l -> {
                    queryCurObdVersion();
                }, throwable -> {
                    log.error("异常", throwable);
                });
    }

    private void queryCurObdVersion() {
        try {
            readVersionSubscription = ObdUpdateFlow.getObdVersion()
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(obdVerion -> {
                        log.info("读取到的当前版本号：{}", obdVerion);
                        proQueryVersionResult(obdVerion);
                    }, throwable -> {
                        log.error("异常", throwable);
//                        log.info("查询版本异常，强制升级obdBin");
//                        updateObdBin();
                    });
            log.info("查询obdBin版本--");
            ObdUpdateFlow.checkVersion();
        } catch (IOException e) {
            log.error("异常", e);
        }
    }

    private void cancerQueryVersion() {
        if (delaySubscription != null) {
            delaySubscription.unsubscribe();
        }
        if (readVersionSubscription != null) {
            readVersionSubscription.unsubscribe();
        }
    }

    private void proQueryVersionResult(String obdVersion) {
        curObdVersion = obdVersion;
        cancerQueryVersion();
        sendStartDataStreamCmd();
        ObdManage.getInstance().init();
        RobberyFlow.checkGunSwitch();
    }


    public void sendStartDataStreamCmd() {
        try {
            log.info("发送开启数据流命令");
            OBDStream.getInstance().exec("ATRON");
        } catch (IOException e) {
            log.error("异常：", e);
        }
    }

    public static ObdUpdateService getInstance() {
        if (instance == null) {
            synchronized (ObdUpdateService.class) {
                if (instance == null) {
                    instance = new ObdUpdateService();
                }
            }
        }
        return instance;
    }
}
