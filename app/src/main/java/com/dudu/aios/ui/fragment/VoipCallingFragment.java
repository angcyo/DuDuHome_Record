package com.dudu.aios.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dudu.aios.ui.activity.MainRecordActivity;
import com.dudu.aios.ui.fragment.base.RBaseFragment;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.android.launcher.utils.BtPhoneUtils;
import com.dudu.android.launcher.utils.DeviceIDUtil;
import com.dudu.android.launcher.utils.NetworkUtils;
import com.dudu.android.launcher.utils.SharedPreferencesUtils;
import com.dudu.rest.common.IpUtils;
import com.dudu.rest.common.RetrofitServiceFactory;
import com.dudu.rest.model.GetCallSubAccountResponse;
import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.service.AppService;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.TTSType;
import com.dudu.voip.VoipSDKCoreHelper;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECVoIPCallManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * chad 2016/5/4
 */
public class VoipCallingFragment extends RBaseFragment implements View.OnClickListener,View.OnTouchListener {
    private Button mTerminateButton;

    private LinearLayout mLinearLayoutDialKeyboard;
    private LinearLayout mLinearLayoutContacts;
    private ImageButton mBackButton;

    private ImageButton mDialKeyboardButton;
    private ImageButton mContactsButton;

    private TextView mNumberText;
    private TextView mVoipStateText;

    private TextView mTimeText;

    private Handler timerHandler = new TimerHandler();
    private Handler toMainFragmentHandler = new ToMainFragmentHandler();

    private long startTime = 0;
    private boolean stopTimer = false;

    private GetCallSubAccountResponse mSubAccount;//VOIP 用户信息
    private String mPhoneNumber = "";
    private long duration = 0;
    private long hourTime = 3600 * 1000;//一个小时的微秒值
    private TimerThread timerThread = null;
    private String mCurrentCallId;//Voip callId表示当前这路通话的ID
    private Logger logger = LoggerFactory.getLogger("voip.CallingFragment");
    public static final String VOIP_CALL_BUNDLE_KEY = "call";
    private MediaPlayer mediaPlayer;
    @Override
    protected int getContentView() {
        return R.layout.fragment_voip_calling;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void initView(View rootView) {
        super.initView(rootView);
        mLinearLayoutDialKeyboard = (LinearLayout) mViewHolder.v(R.id.linearLayout_dial_keyboard);
        mLinearLayoutContacts = (LinearLayout) mViewHolder.v(R.id.linearLayout_contacts);
        mTerminateButton = (Button) mViewHolder.v(R.id.calling_terminate_button);
        mBackButton = (ImageButton) mViewHolder.v(R.id.button_back);
        mVoipStateText = (TextView) mViewHolder.v(R.id.tv_voip_state);
        mNumberText = (TextView) mViewHolder.v(R.id.caller_name);
        mTimeText = (TextView) mViewHolder.v(R.id.call_time);
        mContactsButton = (ImageButton) mViewHolder.v(R.id.button_contacts);
        mDialKeyboardButton = (ImageButton) mViewHolder.v(R.id.button_dial_keyboard);

        rootView.setOnTouchListener(this);//设置touch事件避免点击穿透到上一个fragment
        initFilter();
    }

    @Override
    protected void initViewData() {
        mLinearLayoutDialKeyboard.setOnClickListener(this);
        mLinearLayoutContacts.setOnClickListener(this);
        mTerminateButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        mContactsButton.setOnClickListener(this);
        mDialKeyboardButton.setOnClickListener(this);

        mediaPlayer = MediaPlayer.create(mBaseActivity, R.raw.outgoing);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.calling_terminate_button:
                stopPlayOutCallRing();
                String callId = VoipSDKCoreHelper.getInstance().getCurrentCallId();
                if (!TextUtils.isEmpty(callId)) {
                    if(null!=ECDevice.getECVoIPCallManager()){

                        ECDevice.getECVoIPCallManager().releaseCall(callId);
                    }
                }
                mBaseActivity.showMain();
                break;
            case R.id.button_back:
                mBaseActivity.showMain();
                break;
            case R.id.button_dial_keyboard:
            case R.id.linearLayout_dial_keyboard:
                break;
            case R.id.button_contacts:
            case R.id.linearLayout_contacts:
//                replaceFragment(FragmentConstants.BT_CONTACTS);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        logger.debug("onResume");
        startVoip();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimer = false;
        timerThread = null;
        Bundle bundle = FragmentConstants.TEMP_ARGS;
        if(null!=bundle){

            bundle.putBoolean(VOIP_CALL_BUNDLE_KEY,false);
            FragmentConstants.TEMP_ARGS = bundle;
        }
    }

    @Override
    public void onShow() {
        super.onShow();
        logger.debug("onShow");
        startVoip();
    }

    @Override
    public void onHide() {
        super.onHide();
        logger.debug("onHide");
        stopTimer = false;
        timerThread = null;
        Bundle bundle = FragmentConstants.TEMP_ARGS;
        if(null!=bundle){

            bundle.putBoolean(VOIP_CALL_BUNDLE_KEY,false);
            FragmentConstants.TEMP_ARGS = bundle;
        }

        //设置静音
        /*if (null != ECDevice.getECVoIPSetupManager()) {
            ECDevice.getECVoIPSetupManager().setMute(true);
        }*/

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTimer = false;
        logger.debug("onDestroyView");
        mBaseActivity.unregisterReceiver(voipReveiver);
    }

    public void onDialButtonClick(View view) {
        logger.trace("keyboard", "--" + view.getTag());
        String number = (String) view.getTag();
        if (TextUtils.isEmpty(number)) {
            return;
        }
        String callId = VoipSDKCoreHelper.getInstance().getCurrentCallId();
        if(null!=ECDevice.getECVoIPCallManager() && !TextUtils.isEmpty(callId)){

            char dtmf = number.charAt(0);
            ECDevice.getECVoIPCallManager().sendDTMF(callId, dtmf);
        }

    }

    private void playOutCallRing(){
        if(mediaPlayer==null){
            mediaPlayer = MediaPlayer.create(mBaseActivity, R.raw.outgoing);
        }
        if(null!=mediaPlayer){
            logger.debug("playOutCallRing() mediaPlayer.start(); ");
            mediaPlayer.start();
            mediaPlayer.setLooping(true);
        }
    }
    private void stopPlayOutCallRing(){
        if(null!=mediaPlayer){
            logger.debug("stopPlayOutCallRing() mediaPlayer.stop();");
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    private void startVoip(){
        logger.debug("startVoip() VoipSDKCoreHelper.getInstance().eccall_state:"+VoipSDKCoreHelper.getInstance().eccall_state+
        ",callId:"+VoipSDKCoreHelper.getInstance().getCurrentCallId());
        if(VoipSDKCoreHelper.getInstance().eccall_state!=VoipSDKCoreHelper.ERROR_ECCALL_ANSWERED){
            String callId = VoipSDKCoreHelper.getInstance().getCurrentCallId();
            if(!TextUtils.isEmpty(callId)){

                if(null!=ECDevice.getECVoIPCallManager()){

                    ECDevice.getECVoIPCallManager().releaseCall(callId);
                }
                VoipSDKCoreHelper.getInstance().setCurrentCallId("");
            }

            //重置通话时间为零
            if(null!=mTimeText){
                mTimeText.setText("00:00");
            }
            if (null != mVoipStateText) {
                mVoipStateText.setText(R.string.voip_connecting);
            }
        }else if(VoipSDKCoreHelper.getInstance().eccall_state==VoipSDKCoreHelper.ERROR_ECCALL_ANSWERED){

            setPhoneNumberAndTimer();
            return;
        }

        if(!TextUtils.isEmpty(VoipSDKCoreHelper.getInstance().getCurrentCallId())||
                VoipSDKCoreHelper.getInstance().eccall_state == VoipSDKCoreHelper.ERROR_ECCALL_PROCEEDING||
                VoipSDKCoreHelper.getInstance().eccall_state == VoipSDKCoreHelper.ERROR_ECCALL_ALERTING){
            return;
        }
        if (null != FragmentConstants.TEMP_ARGS && this.isVisible()) {
            Bundle bundle = FragmentConstants.TEMP_ARGS;
            if (bundle.getBoolean(VOIP_CALL_BUNDLE_KEY)) {

                //判断网络
                if (!NetworkUtils.isNetworkConnected(mBaseActivity)) {
                    VoiceManagerProxy.getInstance().startSpeaking(
                            getString(R.string.voip_network_error), TTSType.TTS_DO_NOTHING, false);
                    VoipSDKCoreHelper.getInstance().eccall_state = VoipSDKCoreHelper.ERROR_ECCALL_DEFAULT;
                    logger.debug("network invalid");
                    toMainFragmentHandler.sendMessageDelayed(new Message(),1000);
                    return;
                }

                //判断蓝牙电话是否正在通话中
                if (BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_ACTIVE) {
                    VoiceManagerProxy.getInstance().startSpeaking(
                            getString(R.string.bt_noti_btphone_calling), TTSType.TTS_DO_NOTHING, false);
                    replaceFragment(FragmentConstants.FRAGMENT_MAIN_PAGE);
                    VoipSDKCoreHelper.getInstance().eccall_state = VoipSDKCoreHelper.ERROR_ECCALL_DEFAULT;
                    return;
                }

                if (VoipSDKCoreHelper.getInstance().eccall_state==VoipSDKCoreHelper.ERROR_ECCALL_RELEASED||
                        VoipSDKCoreHelper.getInstance().eccall_state==VoipSDKCoreHelper.ERROR_ECCALL_FAILED||
                        VoipSDKCoreHelper.getInstance().eccall_state==VoipSDKCoreHelper.ERROR_ECCALL_DEFAULT||
                        VoipSDKCoreHelper.getInstance().eccall_state==VoipSDKCoreHelper.ERROR_ECCALL_ALERTING) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            //提示正在连接VIP服务
                            VoiceManagerProxy.getInstance().startSpeaking(
                                    getString(R.string.voip_get_vip_services), TTSType.TTS_DO_NOTHING, false);
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            playOutCallRing();
                            if (null == mSubAccount || TextUtils.isEmpty(mSubAccount.result.subAccountSid)) {
                                logger.debug("voip帐号信息 mSubAccount -> null");
                                getSubVoipAccount(mBaseActivity);
                                return;
                            } else {
                                logger.debug("voip帐号信息：" + mSubAccount.toString());
                                //停止语音助手
                                VoipSDKCoreHelper.getInstance().destroySpeech();
                                //初始化Voip 并登录
                                VoipSDKCoreHelper.init(mBaseActivity);
                            }

                        }
                    }).start();


                }
            }
        }

    }

    /**
     * 连接后台服务器获取voip子帐号
     */
    private void getSubVoipAccount(Context context) {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(AppService.GET_CALL_SUB_ACCOUNT, null, DeviceIDUtil.getIMEI(context));
        RetrofitServiceFactory.getAppService().
                getCallSubAccount(DeviceIDUtil.getIMEI(context), requestBody)
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<GetCallSubAccountResponse>() {
                    @Override
                    public void call(GetCallSubAccountResponse getCallSubAccountResponse) {
                        mSubAccount = getCallSubAccountResponse;
                        VoipSDKCoreHelper.getInstance().setmSubAccount(mSubAccount);
                        logger.debug("voip帐号信息：" + mSubAccount.toString());

                        //停止语音助手
                        VoipSDKCoreHelper.getInstance().destroySpeech();
                        //初始化Voip 并登录
                        VoipSDKCoreHelper.init(mBaseActivity);
                    }
                }, throwable -> {
                    stopPlayOutCallRing();
                    VoipSDKCoreHelper.getInstance().eccall_state = VoipSDKCoreHelper.ERROR_ECCALL_DEFAULT;
                    logger.error("getSubVoipAccount", throwable);
                    showGetSubAccountFailInfo();
                });
    }

    private void showGetSubAccountFailInfo() {
        VoiceManagerProxy.getInstance().startSpeaking(
                getString(R.string.voip_get_subaccount_error), TTSType.TTS_DO_NOTHING, false);
        replaceFragment(FragmentConstants.FRAGMENT_MAIN_PAGE);
    }

    /**
     * 设置界面的通话号码和通话时间
     */
    private void setPhoneNumberAndTimer() {

        if(VoipSDKCoreHelper.getInstance().eccall_state==VoipSDKCoreHelper.ERROR_ECCALL_ANSWERED){
            if (null == timerThread) {
                timerThread = new TimerThread();
                timerThread.start();
            } else {
                setStartTime();
            }
        }else{
            stopTimer = false;
            timerThread = null;
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        logger.debug("event.getAction():"+event.getAction());
        if(null!=event && ((event.getAction()==MotionEvent.ACTION_MOVE)||
                (event.getAction()==MotionEvent.ACTION_SCROLL)) ){
            return false;
        }
        return false;
    }

    private class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            mTimeText.setText(getDateFormatTime(duration));
        }
    }

    private class ToMainFragmentHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            stopPlayOutCallRing();
//            finish();
            toMainActivity(LauncherApplication.getContext());
            if (null != MainRecordActivity.appActivity) {

                MainRecordActivity.appActivity.showMain();
            }

        }
    }

    private synchronized String getDateFormatTime(long duration) {
        SimpleDateFormat sdf;
        if (duration < hourTime) {
            sdf = new SimpleDateFormat("mm:ss");
        } else {
            //如果通话时间长于一个小时
            sdf = new SimpleDateFormat("HH:mm:ss");
        }
        Date date = new Date(duration);
        logger.trace("duration:" + duration);
        return sdf.format(date);
    }

    private void setStartTime(){

        startTime = SharedPreferencesUtils.getLongValue(mBaseActivity, VoipSDKCoreHelper.CALLING_START_TIME_KEY, System.currentTimeMillis());

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
        logger.debug("startTime:" + getDateFormatTime(startTime));
    }
    private class TimerThread extends Thread {

        @Override
        public void run() {
            setStartTime();
            stopTimer = true;
            while (stopTimer) {
                try {
                    duration = System.currentTimeMillis() - startTime;
                    logger.debug("timer:" + getDateFormatTime(duration) + ",startTime:"+startTime);
                    timerHandler.sendEmptyMessage(0);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void toMainActivity(Context context) {
        if (!ActivitiesManager.getInstance().isForegroundActivity(context, MainRecordActivity.class.getName())) {

            Intent intent = new Intent();
            intent.setClass(context, MainRecordActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    private void initFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(VoipSDKCoreHelper.ACTION_SDK_CONNECT);
        intentFilter.addAction(VoipSDKCoreHelper.ACTION_ECCALL_STATE);
        mBaseActivity.registerReceiver(voipReveiver, intentFilter);
    }

    BroadcastReceiver voipReveiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (VoipSDKCoreHelper.ACTION_ECCALL_STATE.equals(action)) {
                int error = intent.getIntExtra("error", 0);
                switch (error) {
                    case VoipSDKCoreHelper.ERROR_ECCALL_ANSWERED:
                        stopPlayOutCallRing();
                        setPhoneNumberAndTimer();
                        if (null != mVoipStateText) {
                            mVoipStateText.setText(R.string.calling);
                        }
                        return;
                    case VoipSDKCoreHelper.ERROR_ECCALL_FAILED:
                        stopTimer = false;
                        timerThread = null;
                        release();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                VoiceManagerProxy.getInstance().startSpeaking(getString(R.string.voip_eccall_failed), TTSType.TTS_DO_NOTHING, false);
                                toMainFragmentHandler.sendMessageDelayed(new Message(), 1000);
                            }
                        }).start();

                        return;
                    case VoipSDKCoreHelper.ERROR_ECCALL_RELEASED:
                        VoipSDKCoreHelper.getInstance().eccall_state = VoipSDKCoreHelper.ERROR_ECCALL_DEFAULT;
                        stopTimer = false;
                        timerThread = null;
                        toMainFragmentHandler.sendMessageDelayed(new Message(), 100);
                        return;
                }

            } else if (VoipSDKCoreHelper.ACTION_SDK_CONNECT.equals(action)) {
                //说明：mCurrentCallId如果返回空则代表呼叫失败，可能是参数错误引起。否则返回是一串数字，是当前通话的标识。
                int error = intent.getIntExtra("error", 0);
                switch (error) {
                    case -1:
                        logger.debug("参数有误");
                        VoiceManagerProxy.getInstance().startSpeaking(getString(R.string.voip_eccall_failed), TTSType.TTS_DO_NOTHING, false);
                        toMainFragmentHandler.sendMessageDelayed(new Message(), 1000);
                        release();
                        return;
                    case -2:
                        logger.debug("用户为空");
                        VoiceManagerProxy.getInstance().startSpeaking(getString(R.string.voip_eccall_failed), TTSType.TTS_DO_NOTHING, false);
                        toMainFragmentHandler.sendMessageDelayed(new Message(), 1000);
                        release();
                        return;
                }

                if (null != FragmentConstants.TEMP_ARGS) {
                    Bundle bundle = FragmentConstants.TEMP_ARGS;
                    if (bundle.getBoolean(VOIP_CALL_BUNDLE_KEY)) {
                        if (null!=VoipSDKCoreHelper.getInstance() &&
                                VoipSDKCoreHelper.getInstance().eccall_state==VoipSDKCoreHelper.ERROR_ECCALL_DEFAULT||
                                VoipSDKCoreHelper.getInstance().eccall_state==VoipSDKCoreHelper.ERROR_ECCALL_FAILED||
                                VoipSDKCoreHelper.getInstance().eccall_state==VoipSDKCoreHelper.ERROR_ECCALL_RELEASED) {
                            if (null != mVoipStateText) {
                                mVoipStateText.setText(R.string.voip_connecting);
                            }

                            if(null!=ECDevice.getECVoIPCallManager()){

                                mCurrentCallId = ECDevice.getECVoIPCallManager().makeCall(
                                        ECVoIPCallManager.CallType.DIRECT, "01083035573");//4001228034/01083035573
                                if(!TextUtils.isEmpty(mCurrentCallId)){
                                    VoipSDKCoreHelper.getInstance().setCurrentCallId(mCurrentCallId);
                                }
                            }else{
                                release();
                            }
                            logger.debug("voip:VoipSDKCoreHelper.getInstance().eccall_state->" + VoipSDKCoreHelper.getInstance().eccall_state);
                            logger.debug("voip:mCurrentCallId->" + mCurrentCallId);
                        }
                    }
                }else{
                    release();
                }
            }
        }
    };

    public void release() {
        stopPlayOutCallRing();
        ECDevice.unInitial();
        logger.debug("release()");
        VoipSDKCoreHelper.getInstance().eccall_state = VoipSDKCoreHelper.ERROR_ECCALL_DEFAULT;
    }

}
