package com.dudu.monitor.active;

import android.content.Context;
import android.content.SharedPreferences;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.DataJsonTranslation;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;
import com.dudu.rest.model.active.ActiveRequestResponse;
import com.dudu.workflow.common.RequestFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by dengjun on 2015/12/2.
 * Description :
 */
public class ActiveDeviceManage {
    private static ActiveDeviceManage instance = null;

    public static int ACTIVE_OK = 1;
    public static int UNACTIVE = 0;

    private Context mContext;
    private SharedPreferences sp;
    private Logger log;
    /* 激活状态*/
    private int activeState = 0;

    public static ActiveDeviceManage getInstance() {
        if (instance == null) {
            synchronized (ActiveDeviceManage.class) {
                if (instance == null) {
                    instance = new ActiveDeviceManage();
                }
            }
        }
        return instance;
    }

    private ActiveDeviceManage() {
        mContext = CommonLib.getInstance().getContext();
        sp = mContext.getSharedPreferences("ActiveDevice", Context.MODE_PRIVATE);

        log = LoggerFactory.getLogger("monitor.ActiveDevice");

        activeState = getActiveFlag();
    }

    public void init(){
        release();
        checkActive();
    }

    public void release(){
        if (checkActiveSubscription != null){
            checkActiveSubscription.unsubscribe();
        }
        if (activeDeviceSubscription != null){
            activeDeviceSubscription.unsubscribe();
        }
    }

    public void setActiveFlag(int flag) {
        if (sp != null) {
            sp.edit().putInt("activeFlag", flag).commit();
        }

    }

    public int getActiveFlag() {
        if (sp != null) {
            return sp.getInt("activeFlag", 0);
        }
        return 0;
    }


    private Subscription checkActiveSubscription;

    public void checkActive() {
        log.info("检查激活信息：{}", DataJsonTranslation.objectToJson(new com.dudu.rest.model.active.CheckDeviceActive()));
        checkActiveSubscription = RequestFactory
                .getActiveRequest()
                .checkDeviceActive(new com.dudu.rest.model.active.CheckDeviceActive())
                .subscribeOn(Schedulers.io())
                .subscribe(requestResponse -> {
                    log.debug("post 收到检查激活响应：{}", DataJsonTranslation.objectToJson(requestResponse));
                    if (requestResponse != null){
                        proCheckActiveRes(requestResponse);
                    }else {
                        checkActiveAgain();
                    }
                },throwable -> {
                    log.error("异常", throwable);
                    checkActiveAgain();
                });
    }

    private void proCheckActiveRes(ActiveRequestResponse requestResponse) {
        if (requestResponse.resultCode == 40019) {
            activeState = 1;
            setActiveFlag(ACTIVE_OK);
            log.info("设备已经激活了");

            if (requestResponse.result != null){
                savePayPictureUri(requestResponse.result);
            }
        } else if (requestResponse.resultCode == 40020) {
            activeDevice();
        } else {
            checkActiveAgain();
        }
    }

    private void checkActiveAgain() {
        Observable
                .timer(30, TimeUnit.SECONDS, Schedulers.io())
                .subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                log.info("再次检查设备激活----");
                checkActive();
            }
        }, throwable -> log.error("onEventBackgroundThread", throwable));
    }


    private Subscription activeDeviceSubscription;
    private void activeDevice() {
        log.debug("激活信息：{}", DataJsonTranslation.objectToJson(new com.dudu.rest.model.active.ActiveDevice()));
        activeDeviceSubscription = RequestFactory
                .getActiveRequest()
                .acticeDevice(new com.dudu.rest.model.active.ActiveDevice())
                .subscribeOn(Schedulers.io())
                .subscribe(activeDeviceResponse -> {
                    log.info("收到激活响应：{}", DataJsonTranslation.objectToJson(activeDeviceResponse));
                    if (activeDeviceResponse != null) {
                        proActiveDeviceRes(activeDeviceResponse);
                    }else {
                        activeDevcieAgain();
                    }
                }, throwable -> {
                    log.error("异常",throwable);
                    activeDevcieAgain();
                });
    }

    private void proActiveDeviceRes(ActiveRequestResponse activeRequestResponse) {
        if (activeRequestResponse.resultCode == 40023) {
            activeState = 1;
            setActiveFlag(ACTIVE_OK);
            log.info("激活成功");

            if (activeRequestResponse.result != null){
                savePayPictureUri(activeRequestResponse.result);
            }
        } else if (activeRequestResponse.resultCode == 40022) {
            activeDevcieAgain();
        } else if (activeRequestResponse.resultCode == 40021) {
            activeDevcieAgain();
        } else if (activeRequestResponse.resultCode == 40017) {
            activeDevcieAgain();
        }
    }

    private void activeDevcieAgain() {
        Observable
                .timer(30, TimeUnit.SECONDS, Schedulers.io())
                .subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                log.info("再次尝试激活设备----");
                activeDevice();
            }
        }, throwable -> log.error("onEventBackgroundThread", throwable));
    }


    private void reboot() {
        com.dudu.android.hideapi.SystemPropertiesProxy.getInstance().set(mContext, "persist.sys.boot", "reboot");
    }

    public int getActiveState() {
        return activeState;
    }

    private void savePayPictureUri(ActiveRequestResponse.ActiveDeviceResInfo activeDeviceResInfo){
        try {
            log.info("保存支付二维码照片的下载地址：{}", DataJsonTranslation.objectToJson(activeDeviceResInfo));
            SharedPreferencesUtil.putStringValue(CommonLib.getInstance().getContext(), ActiveContants.TENCENT_PAY_KEY, activeDeviceResInfo.tencentPayUri);
            SharedPreferencesUtil.putStringValue(CommonLib.getInstance().getContext(), ActiveContants.ALIPAY_PAY_KEY, activeDeviceResInfo.aliPayUri);
        }catch (Exception e){
            log.error("异常", e);
        }
    }


    /* 设备是否激活了*/
    public boolean isDeviceActived() {
        if (getActiveState() == ActiveDeviceManage.ACTIVE_OK) {
            return true;
        } else {
            return false;
        }
    }
}
