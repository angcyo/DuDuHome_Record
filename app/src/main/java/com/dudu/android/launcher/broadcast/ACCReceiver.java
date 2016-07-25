package com.dudu.android.launcher.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dudu.drivevideo.rearcamera.RearCameraManage;
import com.dudu.drivevideo.utils.UsbControl;
import com.dudu.init.CarFireManager;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.TTSType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

public class ACCReceiver extends BroadcastReceiver {
    private final static String ACTION_ACC_BL_CHANGED = "android.intent.action.ACC_BL";
    private final static String ACTION_ACC_ON_CHANGED = "android.intent.action.ACC_ON";

    /* 标记是否正在倒车*/
    public static boolean isBackCarIng = false;
    public static boolean isFactroyIng = false;

    private Logger log;

    public ACCReceiver() {
        log = LoggerFactory.getLogger("video.reardrivevideo");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isFactroyIng) return;

        if (intent.getAction().equals(ACTION_ACC_BL_CHANGED)) {
            boolean backed = intent.getBooleanExtra("backed", false);
            log.debug("收到倒车广播 acc_bl:{}", backed);
            isBackCarIng = backed;
            if (backed == true) {
                VoiceManagerProxy.getInstance().startSpeaking("正在倒车", TTSType.TTS_DO_NOTHING, false);
                if (UsbControl.isUsbHostState() == false) {
                    log.info("正在倒车 UsbControl.setToHost()");
                    UsbControl.setToHost();
                }
                RearCameraManage.getInstance().startDrivingPreview();
                delayStartRearCameraRecord(5);
            } else {
                VoiceManagerProxy.getInstance().startSpeaking("停止倒车", TTSType.TTS_DO_NOTHING, false);
                RearCameraManage.getInstance().stopPreview();
            }
        } else if (intent.getAction().equals(ACTION_ACC_ON_CHANGED)) {
            boolean fired = intent.getBooleanExtra("fired", false);
            log.debug("ACCReceiver acc_on:{}", fired);
            if (fired) {
                log.debug("[init][{}] 接收到点火通知");
                VoiceManagerProxy.getInstance().startSpeaking(
                        "车辆点火", TTSType.TTS_DO_NOTHING, false);
//                CommonLib.getInstance().getContext().startService(new Intent(context, MainService.class));
                CarFireManager.getInstance().fireControl();
            } else {
                log.debug("[init][{}] 收到熄火广播");
                VoiceManagerProxy.getInstance().startSpeaking(
                        "车辆熄火", TTSType.TTS_DO_NOTHING, false);
//                CommonLib.getInstance().getContext().stopService(new Intent(context, MainService.class));
                CarFireManager.getInstance().flamoutControl();
            }
        }
    }

    private  void delayStartRearCameraRecord(int seconds){
        Observable
                .timer(seconds, TimeUnit.SECONDS, Schedulers.newThread())
                .subscribe(l->{
                    log.info("延时{}秒开启后置录像", seconds);
                    RearCameraManage.getInstance().startRecord();
                },throwable -> {
                    log.error("异常",throwable);
                });
    }
}
