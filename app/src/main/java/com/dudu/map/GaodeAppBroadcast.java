package com.dudu.map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.ui.activity.LocationMapActivity;
import com.dudu.android.launcher.utils.BtPhoneUtils;
import com.dudu.android.launcher.utils.TimeUtils;
import com.dudu.navi.NavigationManager;
import com.dudu.navi.vauleObject.NavigationType;
import com.dudu.voice.FloatWindowUtils;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.TTSType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by lxh on 2016/3/1.
 */
public class GaodeAppBroadcast extends BroadcastReceiver {


    private final static String SEND_ACTION = "com.autonavi.minimap.carmode.send";
    private final static String SEND_BUSINESS_ACTION = "send_business_action";
    private final static String SEND_BUSINESS_DATA = "send_business_data";
    public final static String SEND_LAUNCH_APP = "LAUNCH_APP"; // 程序启动
    public final static String SEND_EXIT_APP = "EXIT_APP"; // 程序退出
    public final static String SEND_OPEN_NAVI = "OPEN_NAVI"; //打开导航
    public final static String SEND_CLOSE_NAVI = "CLOSE_NAVI"; // 关闭导航
    public final static String SEND_NAVI_END = "NAVI_END"; //导航到终点正常结束
    public final static String SEND_PATH_FAIL = "PATH_FAIL"; // 路径规划失败
    public final static String SEND_APP_FOREGROUND = "APP_FOREGROUND"; // 程序切到前台
    public final static String SEND_APP_BACKGROUND = "APP_BACKGROUND"; // 程序隐藏到后台
    public final static String SEND_NAVI_INFO = "NAVI_INFO"; // 导航信息
    // 显示路口放大图
    public final static String SEND_DISP_ROAD_ENLARGE_PIC = "DIS_ROAD_ENLARGE_PIC";
    // 隐藏路口放大图
    public final static String SEND_HIDE_ROAD_ENLARGE_PIC = "HIDE_ROAD_ENLARGE_PIC";
    public final static String SEND_NAVI_STR = "NAVI_STR"; // 导航文字信息

    private boolean isForeground = false;
    private Logger logger = LoggerFactory.getLogger("naviInfo");

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (SEND_ACTION.equals(action)) {
            String businessAct = intent.getStringExtra(SEND_BUSINESS_ACTION);
            if (SEND_LAUNCH_APP.equals(businessAct)) {
                logger.debug("onReceive  程序启动");
                NavigationManager.getInstance(context).setIsNavigatining(true);
                LauncherApplication.getContext().setReceivingOrder(true);

            } else if (SEND_EXIT_APP.equals(businessAct)) {
                logger.debug("onReceive  程序退出");

                isForeground = true;
                VoiceManagerProxy.getInstance().startSpeaking(context.getResources().getString(R.string.navigation_end), TTSType.TTS_DO_NOTHING, false);
                NavigationManager.getInstance(context).setIsNavigatining(false);
                LauncherApplication.getContext().setReceivingOrder(false);
                NavigationProxy.getInstance().setStartNewNavi(false);
            } else if (SEND_OPEN_NAVI.equals(businessAct)) {
                logger.debug("onReceive  打开导航");

                GaodeMapAppUtil.closeNaviVoice();
                int time = Integer.parseInt(TimeUtils.format(TimeUtils.format6));

                if (time > 18 || time < 5) {
                    GaodeMapAppUtil.startNaviNightMode();
                } else {
                    GaodeMapAppUtil.startNaviDayMode();
                }
            } else if (SEND_CLOSE_NAVI.equals(businessAct)) {
                logger.debug("onReceive  退出导航");
                if (!NavigationProxy.getInstance().isStartNewNavi())
                    GaodeMapAppUtil.exitGapdeApp();
                NavigationProxy.getInstance().setStartNewNavi(false);

            } else if (SEND_PATH_FAIL.equals(businessAct)) {

                EventBus.getDefault().post(NavigationType.CALCULATEERROR);

            } else if (SEND_APP_FOREGROUND.equals(businessAct)) {

                logger.debug("onReceive  程序切换到前台");
                NavigationManager.getInstance(context).setIsNavigatining(true);
                LauncherApplication.getContext().setReceivingOrder(true);

                isForeground = true;

            } else if (SEND_APP_BACKGROUND.equals(businessAct)) {

                logger.debug("onReceive  程序切换到后台");
                isForeground = false;
                LauncherApplication.getContext().setReceivingOrder(false);
            } else if (SEND_NAVI_END.equals(businessAct)) {

                logger.debug("onReceive  导航结束");
                Observable.timer(4, TimeUnit.SECONDS)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {
                            if (isForeground) {

                                GaodeMapAppUtil.exitGapdeApp();
                                Intent i = new Intent(context, LocationMapActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(i);
                            }
                        }, throwable -> logger.error("SEND_NAVI_END", throwable));


            } else if (SEND_NAVI_STR.equals(businessAct)) {
                String strInfo = intent.getStringExtra(SEND_BUSINESS_DATA);
                try {
                    logger.debug("onReceive  导航播报文字 {}", strInfo);

                    if (!TextUtils.isEmpty(strInfo) && !FloatWindowUtils.isShowWindow()
                            && BtPhoneUtils.btCallState != BtPhoneUtils.CALL_STATE_ACTIVE) {
                        if (strInfo.contains("(")) {
                            strInfo.replace("(", "");
                        }
                        if (strInfo.contains(")")) {
                            strInfo.replace(")", "");
                        }
                        VoiceManagerProxy.getInstance().stopSpeaking();
                        VoiceManagerProxy.getInstance().clearMisUnderstandCount();
                        VoiceManagerProxy.getInstance().startSpeaking(strInfo, TTSType.TTS_DO_NOTHING, false);
                    }
                } catch (Exception e) {

                }

            }
        }
    }
}
