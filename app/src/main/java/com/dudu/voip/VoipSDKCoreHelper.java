package com.dudu.voip;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.dudu.aios.ui.activity.MainRecordActivity;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.android.launcher.utils.BtPhoneUtils;
import com.dudu.android.launcher.utils.DeviceIDUtil;
import com.dudu.android.launcher.utils.SharedPreferencesUtils;
import com.dudu.commonlib.utils.ToastUtils;
import com.dudu.rest.common.IpUtils;
import com.dudu.rest.common.RetrofitServiceFactory;
import com.dudu.rest.model.GetCallSubAccountResponse;
import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.service.AppService;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.TTSType;
import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECInitParams;
import com.yuntongxun.ecsdk.ECNotifyOptions;
import com.yuntongxun.ecsdk.ECVoIPCallManager;
import com.yuntongxun.ecsdk.ECVoIPSetupManager;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.VideoRatio;
import com.yuntongxun.ecsdk.VoIPCallUserInfo;
import com.yuntongxun.ecsdk.meeting.video.ECVideoMeetingMsg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.functions.Action1;
import rx.observables.BlockingObservable;
import rx.schedulers.Schedulers;

/**
 * Created by Chad on 2016/5/3.
 */
public class VoipSDKCoreHelper implements ECDevice.InitListener, ECDevice.OnECDeviceConnectListener, ECDevice.OnLogoutListener {

    public static final String TAG = "VoipSDKCoreHelper";
    public static final String ACTION_LOGOUT = "com.yuntongxun.ECDemo_logout";
    public static final String ACTION_SDK_CONNECT = "com.yuntongxun.Intent_Action_SDK_CONNECT";
    public static final String ACTION_KICK_OFF = "com.yuntongxun.Intent_ACTION_KICK_OFF";

    public static final String ACTION_ECCALL_STATE = "ACTION_ECCALL_STATE";

    /**
     * 网络电话状态
     */
    public int eccall_state = -1;
    public static final int ERROR_ECCALL_DEFAULT = -1;
    public static final int ERROR_ECCALL_PROCEEDING = 0;
    public static final int ERROR_ECCALL_ALERTING = 1;
    public static final int ERROR_ECCALL_ANSWERED = 2;
    public static final int ERROR_ECCALL_PAUSED = 3;
    public static final int ERROR_ECCALL_PAUSED_BY_REMOTE = 4;
    public static final int ERROR_ECCALL_RELEASED = 5;
    public static final int ERROR_ECCALL_FAILED = 6;

    private static VoipSDKCoreHelper sInstance;
    private Context mContext;
    private ECDevice.ECConnectState mConnect = ECDevice.ECConnectState.CONNECT_FAILED;
    private ECInitParams mInitParams;
    private ECInitParams.LoginMode mMode = ECInitParams.LoginMode.FORCE_LOGIN;
    /**
     * 初始化错误
     */
    public static final int ERROR_CODE_INIT = -3;

    public static final int WHAT_SHOW_PROGRESS = 0x101A;
    public static final int WHAT_CLOSE_PROGRESS = 0x101B;
    private boolean mKickOff = false;
    private ECNotifyOptions mOptions;
    public static SoftUpdate mSoftUpdate;
    private AudioManager mAudioManager;
    private GetCallSubAccountResponse mSubAccount;
    // 获得SDKVoIP呼叫接口
    // 注册VoIP呼叫事件回调监听
    private ECVoIPCallManager callInterface = null;
    private String mCallId;//当前版本支持同时一次通话，暂不支持同时多次通话
    public static final String CALLING_START_TIME_KEY = "calling_start_time_key";
    private static Logger logger = LoggerFactory.getLogger("voip.SDKCoreHelper");
    private static int log_index = 0;
    private Handler handler;

    private VoipSDKCoreHelper() {
        initNotifyOptions();
    }

    public static VoipSDKCoreHelper getInstance() {
        if (sInstance == null) {
            sInstance = new VoipSDKCoreHelper();
        }
        return sInstance;
    }

    public GetCallSubAccountResponse getmSubAccount() {
        return mSubAccount;
    }
    public void setmSubAccount(GetCallSubAccountResponse account){
        this.mSubAccount = account;
    }

    public synchronized void setHandler(final Handler handler) {
        this.handler = handler;
    }

    public static boolean isKickOff() {
        return getInstance().mKickOff;
    }

    public static void init(Context ctx) {
        init(ctx, ECInitParams.LoginMode.AUTO);
    }

    public static void init(Context ctx, ECInitParams.LoginMode mode) {
        getInstance().mKickOff = false;
        log_index = 0;
        logger.debug("[{}] start regist..",log_index++);

        getInstance().mMode = mode;
        getInstance().mContext = ctx;

        //停止语音助手
        VoiceManagerProxy.getInstance().onDestroy();

        // 判断SDK是否已经初始化，没有初始化则先初始化SDK
        if (!ECDevice.isInitialized()) {
            logger.debug("[{}]SDK has ont inited ..",log_index++);

            getInstance().mConnect = ECDevice.ECConnectState.CONNECTING;
            // ECSDK.setNotifyOptions(getInstance().mOptions);
            ECDevice.initial(ctx, getInstance());

            postConnectNotify();
            return;
        }
        logger.debug("[{}]SDK has inited , then regist..",log_index++);
        // 已经初始化成功，直接进行注册
        getInstance().onInitialized();
    }

    public static void setSoftUpdate(String version, String desc, boolean mode) {
        mSoftUpdate = new SoftUpdate(version, desc, mode);
    }

    private void initNotifyOptions() {
        if (mOptions == null) {
            mOptions = new ECNotifyOptions();
        }
        // 设置新消息是否提醒
        mOptions.setNewMsgNotify(true);
        // 设置状态栏通知图标
        mOptions.setIcon(R.drawable.ic_launcher);
        // 设置是否启用勿扰模式（不会声音/震动提醒）
        mOptions.setSilenceEnable(false);
        // 设置勿扰模式时间段（开始小时/开始分钟-结束小时/结束分钟）
        // 小时采用24小时制
        // 如果设置勿扰模式不启用，则设置勿扰时间段无效
        // 当前设置晚上11点到第二天早上8点之间不提醒
        mOptions.setSilenceTime(23, 0, 8, 0);
        // 设置是否震动提醒(如果处于免打扰模式则设置无效，没有震动)
        mOptions.enableShake(true);
        // 设置是否声音提醒(如果处于免打扰模式则设置无效，没有声音)
        mOptions.enableSound(true);
    }

    @Override
    public void onInitialized() {
        logger.debug("[{}]ECSDK is ready Viop初始化成功",log_index++);

        if(null==mSubAccount){
            logger.debug("[{}]验证:子帐号为空",log_index++);
            ToastUtils.showToast(R.string.voip_regist_params_error);
            Intent failIntent = new Intent(ACTION_SDK_CONNECT);
            failIntent.putExtra("error", -2);
            mContext.sendBroadcast(failIntent);
            return;
        }

        //获取音频服务
        mAudioManager = (AudioManager) LauncherApplication.getContext().getSystemService(Context.AUDIO_SERVICE);
        // 设置消息提醒
        ECDevice.setNotifyOptions(mOptions);
        // 设置接收VoIP来电事件通知Intent
        // 呼入界面activity、开发者需修改该类
        /*暂时注释Intent intent = new Intent(getInstance().mContext, VoIPCallActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity( getInstance().mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ECDevice.setPendingIntent(pendingIntent);*/

        // 设置SDK注册结果回调通知，当第一次初始化注册成功或者失败会通过该引用回调
        // 通知应用SDK注册状态
        // 当网络断开导致SDK断开连接或者重连成功也会通过该设置回调
        // 暂时注释 ECDevice.setOnChatReceiveListener(IMChattingHelper.getInstance());
        ECDevice.setOnDeviceConnectListener(this);

        // 设置VOIP 自定义铃声路径
        // 获取一个VoIP设置接口对象
        ECVoIPSetupManager setupManager = ECDevice.getECVoIPSetupManager();
        if (setupManager != null) {
            // 目前支持下面三种路径查找方式
            // 1、如果是assets目录则设置为前缀[assets://]
            setupManager.setInComingRingUrl(true, "assets://voip//phonering.mp3");
            setupManager.setOutGoingRingUrl(true, "assets://voip//phonering.mp3");
            setupManager.setBusyRingTone(true, "assets://voip//playend.mp3");
            // 2、如果是raw目录则设置为前缀[raw://]
            // 3、如果是SDCard目录则设置为前缀[file://]

            // 比如设置开启回音消除模式
            setupManager.setAudioConfigEnabled(ECVoIPSetupManager.AudioType.AUDIO_EC ,
                    true , ECVoIPSetupManager.AudioMode.EC_Conference);

            // 创建一个个人信息参数对象
            VoIPCallUserInfo mUserInfo = new VoIPCallUserInfo();
            mUserInfo.setNickName(mSubAccount.result.voipAccount);
            mUserInfo.setPhoneNumber(DeviceIDUtil.getIMEI(mContext));
            setupManager.setVoIPCallUserInfo(mUserInfo);
        }

        if (ECDevice.getECMeetingManager() != null) {
            // 暂时注释 ECDevice.getECMeetingManager().setOnMeetingListener(MeetingMsgReceiver.getInstance());
        }

        //暂时注释        ClientUser clientUser = CCPAppManager.getClientUser();
        if (mInitParams == null) {
            mInitParams = ECInitParams.createParams();
        }
        mInitParams.reset();
        // 如：VoIP账号/手机号码/..
        mInitParams.setUserid(mSubAccount.result.voipAccount);
        // appkey
        mInitParams.setAppKey("8a48b55153eae511015417fbbe814b89");
        // mInitParams.setAppKey(/*clientUser.getAppKey()*/"ff8080813d823ee6013d856001000029");
        // appToken
        mInitParams.setToken("0b80e8d957d42eaa64ebaa4a368c7c03");
        // mInitParams.setToken(/*clientUser.getAppToken()*/"d459711cd14b443487c03b8cc072966e");
        // ECInitParams.LoginMode.FORCE_LOGIN
        mInitParams.setMode(getInstance().mMode);

        // 如果有密码（VoIP密码，对应的登陆验证模式是）
        // ECInitParams.LoginAuthType.PASSWORD_AUTH
        /*暂时注释 if(!TextUtils.isEmpty(clientUser.getPassword())) {
            mInitParams.setPwd(clientUser.getPassword());
        }*/
        mInitParams.setPwd(mSubAccount.result.voipPwd);
        // 设置登陆验证模式（是否验证密码/如VoIP方式登陆）
        /*暂时注释 if(clientUser.getLoginAuthType() != null) {
            mInitParams.setAuthType(clientUser.getLoginAuthType());
        }*/
        // 设置登陆验证模式（是否验证密码）NORMAL_AUTH-自定义方式
        mInitParams.setAuthType(ECInitParams.LoginAuthType.NORMAL_AUTH);
        // 1代表用户名+密码登陆（可以强制上线，踢掉已经在线的设备）
        // 2代表自动重连注册（如果账号已经在其他设备登录则会提示异地登陆）
        // 3 LoginMode（强制上线：FORCE_LOGIN  默认登录：AUTO）
        mInitParams.setMode(ECInitParams.LoginMode.FORCE_LOGIN);

        //验证参数是否有效
        if (!mInitParams.validate()) {
            logger.debug("[{}]验证:参数无效",log_index++);
            ToastUtils.showToast(R.string.voip_regist_params_error);
            Intent failIntent = new Intent(ACTION_SDK_CONNECT);
            failIntent.putExtra("error", -1);
            mContext.sendBroadcast(failIntent);
            return;
        }

        callInterface = ECDevice.getECVoIPCallManager();
        if (null != callInterface) {
            callInterface.setOnVoIPCallListener(voIPListener);
        } else {
            logger.debug("[{}]callInterface->null",log_index++);
        }
        //登录
        ECDevice.login(mInitParams);

    }

    @Override
    public void onConnect() {
        // Deprecated
        logger.debug("[{}]onConnect",log_index++);
    }

    @Override
    public void onDisconnect(ECError error) {
        // SDK与云通讯平台断开连接
        // Deprecated
        try{
            getInstance().eccall_state = ERROR_ECCALL_DEFAULT;
            if(null!=error){
                logger.debug("[{}]onDisconnect error:" + error.errorMsg,log_index++);
            }else {
                logger.debug("[{}]onDisconnect",log_index++);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectState(ECDevice.ECConnectState state, ECError error) {
        logger.debug("[{}]onConnectState error:" + error.errorMsg,log_index++);
        if (state == ECDevice.ECConnectState.CONNECT_FAILED && error.errorCode == SdkErrorCode.SDK_KICKED_OFF) {
//            try {
//                ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_REGIST_AUTO, "", true);
//                ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_FULLY_EXIT, true, true);
//                ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_FULLY_EXIT, true, true);
//            } catch (InvalidClassException e) {
//                e.printStackTrace();
//            }
            mKickOff = true;
            // 失败，账号异地登陆
            /* 暂时注释 Intent intent = new Intent(ACTION_KICK_OFF);
            intent.putExtra("kickoffText" , error.errorMsg);
            mContext.sendBroadcast(intent);
            LauncherActivity.mLauncherUI.handlerKickOff(error.errorMsg);
            ECNotificationManager.getInstance().showKickoffNotification(mContext ,error.errorMsg);*/
            //恢复语音助手
            initSpeech();
            getInstance().eccall_state = ERROR_ECCALL_DEFAULT;
        }
        if (state == ECDevice.ECConnectState.CONNECT_FAILED) {
            if (error.errorCode == SdkErrorCode.SDK_KICKED_OFF) {
                //账号异地登陆
                logger.debug("[{}]账号异地登陆",log_index++);
                SharedPreferencesUtils.putLongValue(mContext,CALLING_START_TIME_KEY, 0);
                toMainActivity(LauncherApplication.getContext());
                if(null!=MainRecordActivity.appActivity){
                    MainRecordActivity.appActivity.showMain();
                }

                //恢复语音助手
                initSpeech();
                getInstance().eccall_state = ERROR_ECCALL_DEFAULT;
            } else {
                //连接状态失败
                logger.debug("[{}]连接状态失败",log_index++);

                SharedPreferencesUtils.putLongValue(mContext,CALLING_START_TIME_KEY, 0);
                toMainActivity(LauncherApplication.getContext());
                if(null!=MainRecordActivity.appActivity){
                    MainRecordActivity.appActivity.showMain();
                }
                //恢复语音助手
                initSpeech();
                getInstance().eccall_state = ERROR_ECCALL_DEFAULT;
            }
            return;
        } else if (state == ECDevice.ECConnectState.CONNECT_SUCCESS && error.errorCode==SdkErrorCode.REQUEST_SUCCESS) {
            // 登陆成功
            logger.debug("[{}]登录成功",log_index++);
        }else{
            // 登陆失败
            logger.debug("[{}]登录失败",log_index++);
            getInstance().eccall_state = ERROR_ECCALL_DEFAULT;
        }

        getInstance().mConnect = state;
        Intent intent = new Intent(ACTION_SDK_CONNECT);
        intent.putExtra("error", error.errorCode);
        mContext.sendBroadcast(intent);
        postConnectNotify();
    }

    /**
     * 当前SDK注册状态
     *
     * @return
     */
    public static ECDevice.ECConnectState getConnectState() {
        return getInstance().mConnect;
    }

    @Override
    public void onLogout() {
        getInstance().mConnect = ECDevice.ECConnectState.CONNECT_FAILED;
        if (mInitParams != null && mInitParams.getInitParams() != null) {
            mInitParams.getInitParams().clear();
        }
        mInitParams = null;
        mContext.sendBroadcast(new Intent(ACTION_LOGOUT));
    }

    // SDK 初始化失败,可能有如下原因造成
    // 1、可能SDK已经处于初始化状态
    // 2、SDK所声明必要的权限未在清单文件（AndroidManifest.xml）里配置、
    //    或者未配置服务属性android:exported="false";
    // 3、当前手机设备系统版本低于ECSDK所支持的最低版本（当前ECSDK支持
    //    Android Build.VERSION.SDK_INT 以及以上版本）

    @Override
    public void onError(Exception exception) {
        logger.debug("[{}]ECSDK couldn't start: " + exception.getLocalizedMessage(),log_index++);
        Intent intent = new Intent(ACTION_SDK_CONNECT);
        intent.putExtra("error", ERROR_CODE_INIT);
        mContext.sendBroadcast(intent);
        ECDevice.unInitial();
        //恢复语音助手
        initSpeech();
        getInstance().eccall_state = ERROR_ECCALL_DEFAULT;
    }

    /**
     * 状态通知
     */
    private static void postConnectNotify() {
        if (getInstance().mContext instanceof MainRecordActivity) {
            // 暂时注释 ((MainRecordActivity) getInstance().mContext).onNetWorkNotify(getConnectState());
            logger.debug("[{}]postConnectNotify",log_index++);
        }
    }

    public void logout(boolean isNotice) {
        ECDevice.NotifyMode notifyMode = (isNotice) ? ECDevice.NotifyMode.IN_NOTIFY : ECDevice.NotifyMode.NOT_NOTIFY;
        ECDevice.logout(notifyMode, getInstance());
        //恢复语音助手
        if(BtPhoneUtils.btCallState==BtPhoneUtils.CALL_STATE_TERMINATED){

            initSpeech();
        }
        release();
    }

    public void release() {
        logger.debug("[{}]网络电话释放资源ECDevice.unInitial();",log_index++);
        getInstance().mKickOff = false;
        getInstance().eccall_state = ERROR_ECCALL_DEFAULT;

        setCurrentCallId("");
        ECDevice.unInitial();
        initSpeech();
        /*// 暂时注释 IMChattingHelper.getInstance().destroy();
        ContactSqlManager.reset();
        ConversationSqlManager.reset();
        GroupMemberSqlManager.reset();
        GroupNoticeSqlManager.reset();
        GroupSqlManager.reset();
        IMessageSqlManager.reset();
        ImgInfoSqlManager.reset();*/
    }

    /**
     * IM聊天功能接口
     *
     * @return
     */
    public static ECChatManager getECChatManager() {
        ECChatManager ecChatManager = ECDevice.getECChatManager();
        logger.debug("[{}]ecChatManager :" + ecChatManager,log_index++);
        return ecChatManager;
    }

    /**
     * 群组聊天接口
     * @return
     */
//    public static ECGroupManager getECGroupManager() {
//        return ECDevice.getECGroupManager();
//    }

//    public static ECDeskManager getECDeskManager() {
//        return ECDevice.getECDeskManager();
//    }

    /**
     * VoIP呼叫接口
     *
     * @return
     */
    public static ECVoIPCallManager getVoIPCallManager() {
        return ECDevice.getECVoIPCallManager();
    }

    public static ECVoIPSetupManager getVoIPSetManager() {
        return ECDevice.getECVoIPSetupManager();
    }


    public static class SoftUpdate {
        public String version;
        public String desc;
        public boolean force;

        public SoftUpdate(String version, String desc, boolean force) {
            this.version = version;
            this.force = force;
            this.desc = desc;
        }
    }

    /**
     * @return返回底层so库 是否支持voip及会议功能
     * true 表示支持 false表示不支持
     * 请在sdk初始化完成之后调用
     */
    public boolean isSupportMedia() {

        return ECDevice.isSupportMedia();
    }

    public static boolean hasFullSize(String inStr) {
        if (inStr.getBytes().length != inStr.length()) {
            return true;
        }
        return false;
    }

    public void onReceiveVideoMeetingMsg(ECVideoMeetingMsg msg) {

        logger.error("[onReceivevideomsg ] Receive video phone message  , id :"
                + msg.getMeetingNo() + ",type="
                + msg.getMsgType());
        Bundle b = new Bundle();
        b.putParcelable("VideoConferenceMsg", msg);

        // 暂时注释 sendTarget(VideoconferenceBaseActivity.KEY_VIDEO_RECEIVE_MESSAGE, b);
    }

    long t = 0;

    public void sendTarget(int what, Object obj) {
        t = System.currentTimeMillis();
        while (handler == null && (System.currentTimeMillis() - t < 3500)) {

            try {
                Thread.sleep(80L);
            } catch (InterruptedException e) {
            }
        }

        if (handler == null) {
            logger.warn("[RLVoiceHelper] handler is null, activity maybe destory, wait...");
            return;
        }

        Message msg = Message.obtain(handler);
        msg.what = what;
        msg.obj = obj;
        msg.sendToTarget();
    }

    /**
     * 判断服务是否自动重启
     *
     * @return 是否自动重启
     */
    public static boolean isUIShowing() {
        return ECDevice.isInitialized();
    }


    private ECVoIPCallManager.OnVoIPListener voIPListener = new ECVoIPCallManager.OnVoIPListener() {
        @Override
        public void onVideoRatioChanged(VideoRatio videoRatio) {

        }

        @Override
        public void onSwitchCallMediaTypeRequest(String s, ECVoIPCallManager.CallType callType) {

        }

        @Override
        public void onSwitchCallMediaTypeResponse(String s, ECVoIPCallManager.CallType callType) {

        }

        @Override
        public void onDtmfReceived(String s, char c) {

        }

        @Override
        public void onCallEvents(ECVoIPCallManager.VoIPCall voipCall) {
            // 处理呼叫事件回调
            if (voipCall == null) {
                initSpeech();
                logger.error("[{}]SDKCoreHelper", "handle call event error , voipCall null",log_index++);
                return;
            }
            // 根据不同的事件通知类型来处理不同的业务
            ECVoIPCallManager.ECCallState callState = voipCall.callState;
            switch (callState) {
                case ECCALL_PROCEEDING:
                    // 正在连接服务器处理呼叫请求
                    logger.debug("[{}]正在连接服务器处理呼叫请求",log_index++);
                    eccall_state = ERROR_ECCALL_PROCEEDING;
                    Intent intent0 = new Intent(ACTION_ECCALL_STATE);
                    intent0.putExtra("error", ERROR_ECCALL_PROCEEDING);
                    mContext.sendBroadcast(intent0);
                    break;
                case ECCALL_ALERTING:
                    // 呼叫到达对方客户端，对方正在振铃
                    logger.debug("[{}]呼叫到达对方客户端，对方正在振铃",log_index++);
                    eccall_state = ERROR_ECCALL_ALERTING;
                            Intent intent1 = new Intent(ACTION_ECCALL_STATE);
                    intent1.putExtra("error", ERROR_ECCALL_ALERTING);
                    mContext.sendBroadcast(intent1);
                    break;
                case ECCALL_ANSWERED:
                    // 对方接听本次呼叫
                    logger.debug("[{}]对方接听本次呼叫",log_index++);
                    eccall_state = ERROR_ECCALL_ANSWERED;
                    Intent intent2 = new Intent(ACTION_ECCALL_STATE);
                    intent2.putExtra("error", ERROR_ECCALL_ANSWERED);
                    mContext.sendBroadcast(intent2);

                    //设置音量
                    AudioManager audiomanager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                    if(null!=audiomanager){
                        audiomanager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 10, 0);
                    }

                    SharedPreferencesUtils.putLongValue(mContext,CALLING_START_TIME_KEY, System.currentTimeMillis());

                    logger.debug("[{}]静音状态return:"+ECDevice.getECVoIPSetupManager().getMuteStatus() + "，" +
                            "返回true则是静音状态，false则不是静音状态",log_index++);
                    if(ECDevice.getECVoIPSetupManager().getMuteStatus()){
                        //传入true则对方听不到说话，false则对方可以听到说话
                        ECDevice. getECVoIPSetupManager().setMute(false);
                    }
                    //设置非免提
                    ECDevice.getECVoIPSetupManager().enableLoudSpeaker(false);

                    if(null!=mAudioManager){
                        //设置声音模式
                        mAudioManager.setMode(AudioManager.MODE_NORMAL);
                        //打开麦克风
                        mAudioManager.setMicrophoneMute(false);
                        //打开扬声器
                        mAudioManager.setSpeakerphoneOn(true);
                        logger.debug("[{}]MicrophoneMute:"+mAudioManager.isMicrophoneMute(),log_index++);
                    }

                    break;
                case ECCALL_FAILED:
                    // 本次呼叫失败，根据失败原因播放提示音
                    eccall_state = ERROR_ECCALL_FAILED;
                    Intent intent3 = new Intent(ACTION_ECCALL_STATE);
                    intent3.putExtra("error", ERROR_ECCALL_FAILED);
                    mContext.sendBroadcast(intent3);

                    SharedPreferencesUtils.putLongValue(mContext,CALLING_START_TIME_KEY, 0);
                    ECDevice.getECVoIPCallManager().releaseCall(voipCall.callId);
                    logger.error("[{}]本次呼叫失败，根据失败原因播放提示音 callId:" +
                            voipCall.callId + " reason:"+voipCall.reason,log_index++);
                    //释放SDK资源
                    logout(false);
                    break;
                case ECCALL_RELEASED:
                    // 通话释放[完成一次呼叫]
                    eccall_state = ERROR_ECCALL_RELEASED;
                    Intent intent4 = new Intent(ACTION_ECCALL_STATE);
                    intent4.putExtra("error", ERROR_ECCALL_RELEASED);
                    mContext.sendBroadcast(intent4);

                    ECDevice.getECVoIPCallManager().releaseCall(voipCall.callId);
                    SharedPreferencesUtils.putLongValue(mContext,CALLING_START_TIME_KEY, 0);
                    //释放SDK资源
                    logout(false);

                    logger.debug("[{}]通话释放[完成一次呼叫]",log_index++);
                    break;
                default:
                    logger.error("[{}]SDKCoreHelper", "handle call event error , callState " + callState,log_index++);
                    break;
            }
        }
    };

    public void destroySpeech(){
        logger.debug("[{}]销毁思必驰语音 destroySpeech()",log_index++);
        VoiceManagerProxy.getInstance().onDestroy();
    }

    public void initSpeech(){
        logger.debug("[{}]初始化思必驰语音 initSpeech()",log_index++);
        VoiceManagerProxy.getInstance().onInit();
    }

    public void setCurrentCallId(String callId){
        mCallId = callId;
    }
    public String getCurrentCallId(){
        return mCallId;
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
}
