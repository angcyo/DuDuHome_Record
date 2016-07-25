package com.dudu.voice.semantic.chain;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;

import com.dudu.aios.ui.activity.MainRecordActivity;
import com.dudu.aios.ui.map.AddressSearchActivity;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.android.launcher.utils.BtPhoneUtils;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.android.launcher.utils.Contacts;
import com.dudu.android.launcher.utils.WifiApAdmin;
import com.dudu.commonlib.event.Events;
import com.dudu.commonlib.utils.ModelUtil;
import com.dudu.drivevideo.frontcamera.FrontCameraManage;
import com.dudu.drivevideo.rearcamera.RearCameraManage;
import com.dudu.map.NavigationProxy;
import com.dudu.voice.FloatWindowUtils;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.bean.CmdBean;
import com.dudu.voice.semantic.bean.SemanticBean;
import com.dudu.voice.semantic.constant.SemanticConstant;
import com.dudu.voice.semantic.constant.TTSType;
import com.dudu.voice.semantic.engine.SemanticEngine;
import com.dudu.workflow.obd.VehicleConstants;

import de.greenrobot.event.EventBus;

/**
 * Created by 赵圣琪 on 2015/10/29.
 */
public class CmdChain extends SemanticChain {

    @Override
    public boolean matchSemantic(String service) {
        return SemanticConstant.SERVICE_CMD.equals(service);
    }

    @Override
    public boolean doSemantic(SemanticBean semantic) {
        return handleCmd((CmdBean) semantic);
    }

    private boolean handleCmd(CmdBean bean) {

        String action = bean.getAction();
        String target = bean.getTarget();

        if (target == null) {

        } else {
            if (target.contains(Constants.NAVIGATION)) {
                return handleNavigationCmd(action);
            } else if (target.contains(SemanticConstant.RECORD_CN)) {
                return handleVideoCmd(action);
            } else if (target.equals(Constants.WIFI) || target.equals(Constants.WIFI_CN)) {
                handleWifi(action);
                return true;
            } else if (target.contains(Constants.SPEECH)) {
                handleExitCmd();
                return true;
            } else if (target.contains(Constants.EXIT)) {
                handleExitCmd();
                return true;
            } else if (target.contains(Constants.BACK)) {
                handleBackCmd();
                return true;
            } else if (target.contains(Constants.MAP)) {
                return handleMapCmd(action);
            } else if (target.contains(Constants.SELF_CHECKING)) {
                return handleSelfChecking(action);
            } else if (target.contains(Constants.ROBBERY)) {
                handleRobbery();
                return true;
            } else if (target.contains(Constants.GUARD)) {
                handleGuard();
                return true;
            } else if (target.contains(Constants.FLOWPAY)) {
                handleFlowPay();
                return true;
            } else if (target.contains(Constants.GUARDUNLOCK)) {
                handleUnlockGuard();
                return true;
            } else if (target.contains(Constants.OPEN_ROBBERY)) {
                handleOpenRobbery();
                return true;
            } else if (target.contains(Constants.CONTACT)) {
                openContact();
                return true;
            } else if (target.equals(Constants.VIPSERVICE)) {
                return handleVipService();
            } else if (target.equals(Constants.BT_CALL)) {
                openBtCall();
                return true;
            }
        }

        return false;
    }

    private boolean handleNavigationCmd(String option) {
        switch (option) {
            case Constants.OPEN:
            case Constants.START:
                return NavigationProxy.getInstance().openNavi(NavigationProxy.OPEN_VOICE);
            case Constants.CLOSE:
            case Constants.EXIT:
                FloatWindowUtils.removeFloatWindow();
                NavigationProxy.getInstance().existNavi();
                toMainActivity();
                MainRecordActivity.appActivity.replaceFragment(FragmentConstants.FRAGMENT_MAIN_PAGE);
                break;
        }
        return true;
    }

    private boolean handleVideoCmd(String option) {
        switch (option) {
            case Constants.OPEN:
            case Constants.QIDONG:
            case Constants.KAIQI:
                FloatWindowUtils.removeFloatWindow();
                toMainActivity();
                LauncherApplication.startRecord = true;
                MainRecordActivity.appActivity.replaceFragment(FragmentConstants.FRAGMENT_DRIVING_RECORD);
                return true;
            case Constants.CLOSE:
            case Constants.EXIT:
            case Constants.GUANDIAO:
                FloatWindowUtils.removeFloatWindow();

                try {
                    MainRecordActivity.appActivity.replaceFragment(FragmentConstants.FRAGMENT_MAIN_PAGE);
//                    MainRecordActivity.appActivity.setBlur();
                    FrontCameraManage.getInstance().setPreviewBlur(true);
                } catch (Exception e) {

                }
                RearCameraManage.getInstance().stopPreview();
                mVoiceManager.startSpeaking("录像预览已关闭", TTSType.TTS_DO_NOTHING, false);
                return true;
            case Constants.PLAY:
                FloatWindowUtils.removeFloatWindow();
                toMainActivity();
                MainRecordActivity.appActivity.replaceFragment(FragmentConstants.FRAGMENT_VIDEO_LIST);
                return true;
            default:
                return false;
        }
    }


    private void handleBackCmd() {

        FloatWindowUtils.removeFloatWindow();
        Activity topActivity = ActivitiesManager.getInstance().getTopActivity();

        if (topActivity instanceof AddressSearchActivity) {
            ActivitiesManager.getInstance().closeTargetActivity(topActivity.getClass());
        } else {
            toMainActivity();
            MainRecordActivity.appActivity.replaceFragment(FragmentConstants.FRAGMENT_MAIN_PAGE);
        }
    }

    private void handleExitCmd() {
        FloatWindowUtils.removeFloatWindow();
        SemanticEngine.getProcessor().clearSemanticStack();
        ActivitiesManager.getInstance().closeTargetActivity(AddressSearchActivity.class);
        toMainActivity();
        MainRecordActivity.appActivity.replaceFragment(FragmentConstants.FRAGMENT_MAIN_PAGE);
    }

    private boolean handleMapCmd(String option) {
        FloatWindowUtils.removeFloatWindow();
        switch (option) {
            case Constants.OPEN:
            case Constants.START:
            case Constants.KAIQI:
                if (NavigationProxy.getInstance().openNavi(NavigationProxy.OPEN_MAP)) {
                    FloatWindowUtils.removeFloatWindow();
                    return true;
                }
                return false;
            case Constants.CLOSE:
            case Constants.EXIT:
                NavigationProxy.getInstance().closeMap();
                break;
        }
        return true;
    }

    private boolean handleSelfChecking(String action) {
        switch (action) {
            case Constants.OPEN:
            case Constants.QIDONG:
            case Constants.KAIQI:
            case Constants.START:
                FloatWindowUtils.removeFloatWindow();
                toMainActivity();
                Bundle bundle = new Bundle();
                bundle.putBoolean(VehicleConstants.START_CHECKING, true);
                FragmentConstants.TEMP_ARGS = bundle;
                EventBus.getDefault().post(new Events.OpenSafeCenterEvent(Events.OPEN_VEHICLE_INSPECTION));
                break;
            case Constants.CLOSE:
            case Constants.EXIT:
            case Constants.GUANDIAO:
                FloatWindowUtils.removeFloatWindow();

                MainRecordActivity.appActivity.replaceFragment(FragmentConstants.FRAGMENT_MAIN_PAGE);
                break;
            default:
                return false;
        }

        return true;
    }

    private void handleWifi(String option) {
        switch (option) {
            case Constants.OPEN:
                WifiApAdmin.startWifiAp(mContext);
                mVoiceManager.startSpeaking("Wifi热点已打开", TTSType.TTS_START_UNDERSTANDING, true);
                break;
            case Constants.CLOSE:
                WifiApAdmin.closeWifiAp(mContext);
                mVoiceManager.startSpeaking("Wifi热点已关闭", TTSType.TTS_START_UNDERSTANDING, true);
                break;
        }
    }


    private void handleRobbery() {
        FloatWindowUtils.removeFloatWindow();
        toMainActivity();
        Bundle bundle = new Bundle();
        bundle.putInt(VehicleConstants.SHOW_GUARD_OR_ROBBERY, Contacts.SHOW_ROBBERY_FRAGMENT);
        FragmentConstants.TEMP_ARGS = bundle;
        EventBus.getDefault().post(new Events.OpenSafeCenterEvent(Events.OPEN_SAFETY_CENTER));
    }

    private void handleGuard() {
        FloatWindowUtils.removeFloatWindow();
        toMainActivity();
        Bundle bundle = new Bundle();
        bundle.putInt(VehicleConstants.SHOW_GUARD_OR_ROBBERY, Contacts.SHOW_GUARD_FRAGMENT);
        FragmentConstants.TEMP_ARGS = bundle;
        EventBus.getDefault().post(new Events.OpenSafeCenterEvent(Events.OPEN_SAFETY_CENTER));
    }

    private void toMainActivity() {

        if (!(ActivitiesManager.getInstance().getTopActivity() instanceof MainRecordActivity)
                || LauncherApplication.getContext().isReceivingOrder()
                || !ActivitiesManager.getInstance().isTopActivity(mContext, "com.dudu.android.launcher")) {
            LauncherApplication.getContext().setReceivingOrder(false);
            Intent intent = new Intent();
            intent.setClass(mContext, MainRecordActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    private void handleFlowPay() {
        FloatWindowUtils.removeFloatWindow();
        toMainActivity();
        MainRecordActivity.appActivity.replaceFragment(FragmentConstants.FRAGMENT_FLOW);

    }

    private void handleUnlockGuard() {
        FloatWindowUtils.removeFloatWindow();
        toMainActivity();
        Bundle bundle = new Bundle();
        bundle.putInt(VehicleConstants.SHOW_GUARD_OR_ROBBERY, Contacts.SHOW_GUARD_FRAGMENT);
        bundle.putBoolean(VehicleConstants.UNLOCK_GUARD, true);
        FragmentConstants.TEMP_ARGS = bundle;
        EventBus.getDefault().post(new Events.OpenSafeCenterEvent(Events.OPEN_SAFETY_CENTER));
    }

    private void handleOpenRobbery() {
        FloatWindowUtils.removeFloatWindow();
        toMainActivity();
        Bundle bundle = new Bundle();
        bundle.putInt(VehicleConstants.SHOW_GUARD_OR_ROBBERY, Contacts.SHOW_ROBBERY_FRAGMENT);
        bundle.putBoolean(VehicleConstants.OPEN_ROBBERY, true);
        FragmentConstants.TEMP_ARGS = bundle;
        EventBus.getDefault().post(new Events.OpenSafeCenterEvent(Events.OPEN_SAFETY_CENTER));
    }

    private void openContact() {
        FloatWindowUtils.removeFloatWindow();
        toMainActivity();
        MainRecordActivity.appActivity.replaceFragment(FragmentConstants.BT_CONTACTS);

    }

    private boolean handleVipService() {
        if (ModelUtil.needVip()) {
            FloatWindowUtils.removeFloatWindow();
            toMainActivity();
            Bundle voipBundle = new Bundle();
            voipBundle.putBoolean("call", true);
            FragmentConstants.TEMP_ARGS = voipBundle;
            MainRecordActivity.appActivity.replaceFragment(FragmentConstants.VOIP_CALLING_FRAGMENT);

            return true;
        }
        return false;
    }

    private void openBtCall() {
        toMainActivity();
        //拨号前先判断蓝牙是否处于连接状态
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if (null == adapter) {
            VoiceManagerProxy.getInstance().startSpeaking(
                    mContext.getString(R.string.bt_noti_disenable), TTSType.TTS_START_UNDERSTANDING, true);
            return;
        }
        if (!adapter.isEnabled() || BtPhoneUtils.connectionState != BtPhoneUtils.STATE_CONNECTED) {
            adapter.enable();
            VoiceManagerProxy.getInstance().startSpeaking(
                    mContext.getString(R.string.bt_noti_connect_waiting), TTSType.TTS_START_UNDERSTANDING, true);
            return;
        }

        FloatWindowUtils.removeFloatWindow();
        MainRecordActivity.appActivity.replaceFragment(FragmentConstants.BT_DIAL);

    }
}
