package com.dudu.android.launcher.broadcast;

import android.app.Activity;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;

import com.dudu.aios.ui.activity.MainRecordActivity;
import com.dudu.aios.ui.base.BaseActivity;
import com.dudu.aios.ui.map.GaodeMapActivity;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.android.launcher.utils.BtPhoneUtils;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.init.CarFireManager;
import com.dudu.map.NavigationProxy;
import com.dudu.navi.NavigationManager;
import com.dudu.voice.FloatWindowUtils;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voip.VoipSDKCoreHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by 赵圣琪 on 2016/1/18.
 */
public class BtCallReceiver extends BroadcastReceiver {

    public static final int CALL_STATE_TERMINATED = 7;
    public static final String EXTRA_RESULT_CODE = "android.bluetooth.handsfreeclient.extra.RESULT_CODE";
    public static final String EXTRA_CME_CODE = "android.bluetooth.handsfreeclient.extra.CME_CODE";
    private static final String ACTIVITY_NAME_AUTONAVI = "com.autonavi.auto.MainMapActivity";
    private Logger logger = LoggerFactory.getLogger("phone.BtCallReceiver");
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private String mScreenState = Intent.ACTION_SCREEN_OFF;
    private int MSG_BT_IN_CALL = 1;
    private int MSG_BT_OUT_CALL = 2;
    private int MSG_BT_CALLING = 3;
    public static HashMap<String,Long> phoneStartTimeList = new HashMap<>(); //通话开始时间
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        logger.debug("接收到蓝牙电话广播: " + action);
        if (action.equals(Constants.ACTION_BLUETOOTH_PHONE_INCALL)) {
            //设置音量
            int curVolume = BtPhoneUtils.getBtPhoneCurrentVolume(context);
            logger.debug("in call current Volume:"+curVolume);
            BtPhoneUtils.setBtPhoneVolume(context, Constants.STREAM_BLUETOOTH_SCO, curVolume==0?5:curVolume);

            BtPhoneUtils.btCallState = BtPhoneUtils.CALL_STATE_INCOMING;

            //点亮屏幕
            CarFireManager.getInstance().acquireLock();

            // 获取电话号码
            String phoneNumber = intent.getStringExtra("HFP_NUMBER");
            logger.debug("incoming number:"+phoneNumber);
            Bundle arguments = new Bundle();
            if(!TextUtils.isEmpty(phoneNumber)){
                arguments.putString(Constants.EXTRA_PHONE_NUMBER,phoneNumber);
                FragmentConstants.TEMP_ARGS = arguments;
            }

            if(VoipSDKCoreHelper.getInstance().eccall_state != VoipSDKCoreHelper.ERROR_ECCALL_ANSWERED){

                //暂停语音
                VoiceManagerProxy.getInstance().onStop();
                //隐藏浮窗动画
                if(FloatWindowUtils.isShowWindow()){
                    FloatWindowUtils.removeFloatWindow();
                }
            }

            toMainActivity(LauncherApplication.getContext());

            Message msg = new Message();
            msg.what = MSG_BT_IN_CALL;
            myHandler.sendMessageDelayed(msg, 1000);


        } else if (action.equals(Constants.ACTION_BLUETOOTH_PHONE_OUTCALL)) {
            //设置音量
            //设置音量
            int curVolume = BtPhoneUtils.getBtPhoneCurrentVolume(context);
            logger.debug("out call current Volume:"+curVolume);
            BtPhoneUtils.setBtPhoneVolume(context, Constants.STREAM_BLUETOOTH_SCO, curVolume==0?5:curVolume);

            BtPhoneUtils.btCallState = BtPhoneUtils.CALL_STATE_DIALING;

            //点亮屏幕
            CarFireManager.getInstance().acquireLock();

            //暂停语音
            VoiceManagerProxy.getInstance().stopWakeup();
            //隐藏浮窗动画
            if(FloatWindowUtils.isShowWindow()){
                FloatWindowUtils.removeFloatWindow();
            }

            // 获取电话号码
            String phoneNumber = intent.getStringExtra("HFP_NUMBER");
            logger.debug("outcall number:"+phoneNumber);

            Bundle arguments = new Bundle();
            if(!TextUtils.isEmpty(phoneNumber)){
                arguments.putString(Constants.EXTRA_PHONE_NUMBER,phoneNumber);
                FragmentConstants.TEMP_ARGS = arguments;
            }

            toMainActivity(LauncherApplication.getContext());
            if(BtPhoneUtils.btCallOutSource == BtPhoneUtils.BTCALL_OUT_SOURCE_DEFAULT ){
                BtPhoneUtils.btCallOutSource = BtPhoneUtils.BTCALL_OUT_SOURCE_MOBILE;
                Message msg = new Message();
                msg.what = MSG_BT_OUT_CALL;
                myHandler.sendMessageDelayed(msg, 1000);
            }

        } else if (action.equals(Constants.ACTION_BLUETOOTH_PHONE_CONNECT)) {

            BtPhoneUtils.btCallState = BtPhoneUtils.CALL_STATE_ACTIVE;
            logger.debug("BtPhoneUtils.btCallState:"+BtPhoneUtils.btCallState);
            BtPhoneUtils.initAudio(context);

            //设置音量
            int curVolume = BtPhoneUtils.getBtPhoneCurrentVolume(context);
            logger.debug("connect current Volume:"+curVolume);
            BtPhoneUtils.setBtPhoneVolume(context, Constants.STREAM_BLUETOOTH_SCO,  curVolume==0?5:curVolume);
//            BtPhoneUtils.setBtPhoneVolume(context, AudioManager.STREAM_VOICE_CALL,
//                    BtPhoneUtils.getBtPhoneMaxVolume(context, AudioManager.STREAM_VOICE_CALL));
//            BtPhoneUtils.setBtPhoneVolume(context, AudioManager.STREAM_MUSIC,
//                    BtPhoneUtils.getBtPhoneMaxVolume(context, AudioManager.STREAM_MUSIC));

            //获取电话号码
            String phoneNumber = intent.getStringExtra("HFP_NUMBER");
            Bundle arguments = new Bundle();
            if(!TextUtils.isEmpty(phoneNumber)){
                arguments.putString(Constants.EXTRA_PHONE_NUMBER,phoneNumber);
                FragmentConstants.TEMP_ARGS = arguments;
                //缓存通话号码的起始时间
                phoneStartTimeList.remove(phoneNumber);
                phoneStartTimeList.put(phoneNumber,System.currentTimeMillis());
            }

            //如果网络电话正在通话中
            if(VoipSDKCoreHelper.getInstance().eccall_state == VoipSDKCoreHelper.ERROR_ECCALL_ANSWERED||
                    VoipSDKCoreHelper.getInstance().eccall_state == VoipSDKCoreHelper.ERROR_ECCALL_PROCEEDING||
                    VoipSDKCoreHelper.getInstance().eccall_state == VoipSDKCoreHelper.ERROR_ECCALL_ALERTING){

                toMainActivity(LauncherApplication.getContext());
                if(null!=MainRecordActivity.appActivity){
                    logger.debug("MainRecordActivity.appActivity " + MainRecordActivity.appActivity);
                    MainRecordActivity.appActivity.replaceFragment(FragmentConstants.VOIP_CALLING_FRAGMENT);
                }
            }else if(NavigationManager.getInstance(LauncherApplication.getContext()).isNavigatining()&&
                    ActivitiesManager.getInstance().isForegroundActivity(LauncherApplication.getContext(), ACTIVITY_NAME_AUTONAVI)){
                logger.debug("NavigationManager navigatining");
                //导航
                NavigationProxy.getInstance().openNavi(NavigationProxy.OPEN_MANUAL);

            }else {

                toMainActivity(LauncherApplication.getContext());
                if(null!=MainRecordActivity.appActivity){
                    logger.debug("MainRecordActivity.appActivity " + MainRecordActivity.appActivity);
                    MainRecordActivity.appActivity.replaceFragment(FragmentConstants.BT_CALLING);
                }
            }

        } else if (action.equals(Constants.ACTION_BLUETOOTH_PHONE_END)) {
            BtPhoneUtils.btCallState = BtPhoneUtils.CALL_STATE_TERMINATED;
            BtPhoneUtils.btCallOutSource = BtPhoneUtils.BTCALL_OUT_SOURCE_DEFAULT;

            //如果网络电话正在通话中
            if(VoipSDKCoreHelper.getInstance().eccall_state != VoipSDKCoreHelper.ERROR_ECCALL_ANSWERED&&
                    VoipSDKCoreHelper.getInstance().eccall_state != VoipSDKCoreHelper.ERROR_ECCALL_PROCEEDING&&
                    VoipSDKCoreHelper.getInstance().eccall_state != VoipSDKCoreHelper.ERROR_ECCALL_ALERTING){

                //恢复唤醒语音
                VoiceManagerProxy.getInstance().startWakeup();
            }

            //获取电话号码
            String phoneNumber = intent.getStringExtra("HFP_NUMBER");
            Bundle arguments = new Bundle();
            if(!TextUtils.isEmpty(phoneNumber)){
                arguments.putString(Constants.EXTRA_PHONE_NUMBER,phoneNumber);
                FragmentConstants.TEMP_ARGS = arguments;
                //清除通话号码起始时间
                phoneStartTimeList.remove(phoneNumber);
            }else{
                phoneStartTimeList.clear();
            }

            replaceFragment();
        }else if(action.equals(Constants.ACTION_AUDIO_STATE_CHANGED)){
            int prevState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, 0);
            int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 0);
            logger.debug("ACTION_AUDIO_STATE_CHANGED prevState: " + prevState + " state: " + state);

            if(prevState==1 && state==2){
//                BtPhoneUtils.btCallState = BtPhoneUtils.CALL_STATE_ACTIVE;
                toMainActivity(LauncherApplication.getContext());
                Message msg = new Message();
                if(BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_ACTIVE){

                    msg.what = MSG_BT_CALLING;
                }else if (BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_INCOMING){
                    msg.what = MSG_BT_IN_CALL;
                }else if (BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_DIALING){
                    msg.what = MSG_BT_OUT_CALL;
                }else {
                    msg.what = MSG_BT_CALLING;
                }
                myHandler.sendMessageDelayed(msg, 1000);
            }
        }else if(action.equals(Constants.ACTION_AG_CALL_CHANGED)){
            logger.debug("BtPhoneUtils.btCallState:"+BtPhoneUtils.btCallState);
            //来电振铃
            if(BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_INCOMING){
                toMainActivity(LauncherApplication.getContext());

                Message msg = new Message();
                msg.what = 1;
                myHandler.sendMessageDelayed(msg, 1000);
            }
        }else if(action.equals(Constants.ACTION_HFP_RESULT)){
            int result = intent.getIntExtra( EXTRA_RESULT_CODE, -1);
            int cme = intent.getIntExtra( EXTRA_CME_CODE, -1);
            if(result==1 && (cme==-1 || cme==27)){
                BtPhoneUtils.btCallState = BtPhoneUtils.CALL_STATE_TERMINATED;
                BtPhoneUtils.btCallOutSource = BtPhoneUtils.BTCALL_OUT_SOURCE_DEFAULT;
                replaceFragment();
            }
        }else if(Intent.ACTION_SCREEN_OFF.equals(action)){
            mScreenState = Intent.ACTION_SCREEN_OFF;
        }else if(Intent.ACTION_SCREEN_ON.equals(action)){
            mScreenState = Intent.ACTION_SCREEN_ON;
        }
    }

    /**
     * 处理停止通话后的界面
     */
    private void replaceFragment(){

        logger.debug("BaseActivity.lastFragment:"+ BaseActivity.lastFragment
                +",BaseActivity.lastSecondFragment:"+BaseActivity.lastSecondFragment
                +",BaseActivity.lastThirdFragment:"+BaseActivity.lastThirdFragment);
        if (!ActivitiesManager.getInstance().isForegroundActivity(LauncherApplication.getContext(), ACTIVITY_NAME_AUTONAVI)) {
            toMainActivity(LauncherApplication.getContext());

                if (null != MainRecordActivity.appActivity) {
                    if (null != phoneStartTimeList && phoneStartTimeList.size() == 1) {
                        Iterator iter = phoneStartTimeList.entrySet().iterator();
                        if (iter.hasNext()) {

                            Map.Entry entry = (Map.Entry) iter.next();

                            Bundle arguments = new Bundle();
                            arguments.putString(Constants.EXTRA_PHONE_NUMBER, (String) entry.getKey());
                            FragmentConstants.TEMP_ARGS = arguments;
                            MainRecordActivity.appActivity.replaceFragment(FragmentConstants.BT_CALLING);

                            //返回，不释放wakelock
                            return;
                        } else if(!FragmentConstants.BT_OUT_CALL.equals(BaseActivity.lastThirdFragment)&&
                                !FragmentConstants.BT_IN_CALL.equals(BaseActivity.lastThirdFragment)&&
                                !FragmentConstants.BT_CALLING.equals(BaseActivity.lastThirdFragment)&&
                                !FragmentConstants.VOIP_CALLING_FRAGMENT.equals(BaseActivity.lastThirdFragment)){

                            MainRecordActivity.appActivity.replaceFragment(BaseActivity.lastThirdFragment);
                        }else{
                            MainRecordActivity.appActivity.replaceFragment(FragmentConstants.BT_DIAL);
                        }
                    } else if(!FragmentConstants.BT_OUT_CALL.equals(BaseActivity.lastThirdFragment)&&
                            !FragmentConstants.BT_IN_CALL.equals(BaseActivity.lastThirdFragment)&&
                            !FragmentConstants.BT_CALLING.equals(BaseActivity.lastThirdFragment)&&
                            !FragmentConstants.VOIP_CALLING_FRAGMENT.equals(BaseActivity.lastThirdFragment)){

                        MainRecordActivity.appActivity.replaceFragment(BaseActivity.lastThirdFragment);
                    }else{
                        MainRecordActivity.appActivity.replaceFragment(FragmentConstants.BT_DIAL);
                    }

                    //释放wakelock
                    CarFireManager.getInstance().releaseWakeLockIfNotFired();
                }
        }else{
            if(NavigationManager.getInstance(LauncherApplication.getContext()).isNavigatining()){
                logger.debug("NavigationManager navigatining");
                //导航
                NavigationProxy.getInstance().openNavi(NavigationProxy.OPEN_MANUAL);
            }else{
                //释放wakelock
                CarFireManager.getInstance().releaseWakeLockIfNotFired();
            }
        }
    }
    
    private void startBtPhoneActivity(Context context,
                                      Class<? extends Activity> clzz, Intent intent) {
        Intent i = new Intent(context, clzz);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(Constants.EXTRA_PHONE_NUMBER, intent.getStringExtra("HFP_NUMBER"));
        i.putExtra(Constants.EXTRA_CONTACT_NAME, intent.getStringExtra("HFP_NAME"));
        context.startActivity(i);
    }

    //设置音量
    private void setVolume(Context context, int value){
        //设置音量
        //STREAM_BLUETOOTH_SCO -- 蓝牙通话
        AudioManager audiomanager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if(null!=audiomanager){
            int currentVolume = audiomanager.getStreamVolume(Constants.STREAM_BLUETOOTH_SCO/*AudioManager.STREAM_BLUETOOTH_SCO*/); // 获取当前值
            audiomanager.setStreamVolume(Constants.STREAM_BLUETOOTH_SCO/*AudioManager.STREAM_BLUETOOTH_SCO*/, value, 0);
            logger.debug("currentVolume:"+currentVolume);
        }
    }
    private void toMainActivity(Context context) {

        if (!ActivitiesManager.getInstance().isForegroundActivity(context, MainRecordActivity.class.getName())) {

            Intent intent = new Intent();
            intent.setClass(context, MainRecordActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            ActivitiesManager.getInstance().setTopActivity(MainRecordActivity.appActivity);
        }
    }

    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==MSG_BT_IN_CALL){
                if(null!=MainRecordActivity.appActivity && BtPhoneUtils.btCallState==BtPhoneUtils.CALL_STATE_INCOMING){
                    MainRecordActivity.appActivity.replaceFragment(FragmentConstants.BT_IN_CALL);
                }
            }else if(msg.what==MSG_BT_OUT_CALL){
                if(null!=MainRecordActivity.appActivity && BtPhoneUtils.btCallState==BtPhoneUtils.CALL_STATE_DIALING){
                    MainRecordActivity.appActivity.replaceFragment(FragmentConstants.BT_OUT_CALL);
                }
            } else if(msg.what==MSG_BT_CALLING){
                if(null!=MainRecordActivity.appActivity && BtPhoneUtils.btCallState==BtPhoneUtils.CALL_STATE_ACTIVE){
                    MainRecordActivity.appActivity.replaceFragment(FragmentConstants.BT_CALLING);
                }
            }
        }
    };
}
