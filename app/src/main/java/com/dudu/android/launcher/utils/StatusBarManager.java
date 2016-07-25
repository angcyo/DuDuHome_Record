package com.dudu.android.launcher.utils;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.dudu.android.launcher.LauncherApplication;
import com.dudu.commonlib.event.Events;
import com.dudu.event.DeviceEvent;
import com.dudu.workflow.common.DataFlowFactory;
import com.dudu.workflow.push.ReceiverDataFlow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;

/**
 * Created by 赵圣琪 on 2015/12/17.
 */
public class StatusBarManager {

    private static StatusBarManager mInstance;

    private Context mContext;

    private int mSignalLevel = 0;

    private String mSignalType = "";

    private PhoneStateListener mPhoneStateListener;

    private TelephonyManager mPhoneManager;

    private int isRecording = 0;

    private int bleConnState = 0;

    private Logger logger = LoggerFactory.getLogger("init.StatusBarManager");

    public int getBleConnState() {
        return bleConnState;
    }

    public void setBleConnState(int bleConnState) {
        this.bleConnState = bleConnState;
    }

    public int isRecording() {
        return isRecording;
    }

    public void setRecording(int isRecording) {
        this.isRecording = isRecording;
    }

    public int getSignalLevel() {
        return mSignalLevel;
    }

    public static StatusBarManager getInstance() {
        if (mInstance == null) {
            mInstance = new StatusBarManager();
        }

        return mInstance;
    }

    private StatusBarManager() {
        mContext = LauncherApplication.getContext();

        mPhoneManager = (TelephonyManager) mContext.getSystemService(
                Context.TELEPHONY_SERVICE);
        mPhoneStateListener = new PhoneStateListener() {

            @Override
            public void onDataConnectionStateChanged(int state, int networkType) {
                super.onDataConnectionStateChanged(state, networkType);
                String type = NetworkUtils.getCurrentNetworkType(mContext);
                logger.trace("sim state :{}, networkType:{}, type{}", state, networkType, type);
                if (state == 2) {
                    ReceiverDataFlow.getInstance().reConnect();
                }

                if (type.equals("2G") || type.equals("3G") || type.equals("4G")) {
                    setSimType(type);
                }
            }

            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                logger.trace("signalStrength:{}", signalStrength);
                try {
                    int level = (int) signalStrength.getClass().getMethod("getLevel").
                            invoke(signalStrength);
                    logger.trace("signal level:{}", level);
                    setSimLevel(level);
                } catch (Exception e) {
                    // 忽略
                }
            }
        };
    }

    public void initBarStatus() {
        mPhoneManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
                PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        obtainGuardState();
    }

    private void obtainGuardState() {
        DataFlowFactory.getSwitchDataFlow().getGuardSwitch().subscribe(aBoolean -> {
            logger.debug("开机获取本地防盗的开关的状态: {}", (aBoolean ? "开启" : "关闭"));
            EventBus.getDefault().post(new Events.GuardSwitchState(aBoolean));
        }, throwable -> {
            logger.debug("subscribe error: {}", throwable.toString());
        });
    }

    private boolean isCanUseSim() {
        return mPhoneManager.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

    private void setSimType(String type) {


        if (!isCanUseSim()) {
            return;
        }

        if (!mSignalType.equals(type)) {
            mSignalType = type;
            logger.trace("SimType Type:{}", type);
            EventBus.getDefault().post(new DeviceEvent.SimType(type));
        }
    }

    private void setSimLevel(int level) {
        if (!isCanUseSim()) {
            return;
        }

        if (mSignalLevel != level) {
            mSignalLevel = level;
            EventBus.getDefault().post(new DeviceEvent.SimLevel(level));
        }
    }

}
