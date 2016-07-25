package com.dudu.aios.ui.activity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import com.dudu.aios.ui.base.BaseFragmentManagerActivity;
import com.dudu.aios.ui.base.ObservableFactory;
import com.dudu.aios.ui.fragment.AccelerationTestFragment;
import com.dudu.aios.ui.fragment.BtCallingFragment;
import com.dudu.aios.ui.fragment.BtContactsFragment;
import com.dudu.aios.ui.fragment.BtDialFragment;
import com.dudu.aios.ui.fragment.BtDialSelectNumberFragment;
import com.dudu.aios.ui.fragment.BtInCallFragment;
import com.dudu.aios.ui.fragment.BtOutCallFragment;
import com.dudu.aios.ui.fragment.CarCheckingFragment;
import com.dudu.aios.ui.fragment.DeviceBindFragment;
import com.dudu.aios.ui.fragment.FlowFragment;
import com.dudu.aios.ui.fragment.LicenseUploadFragment;
import com.dudu.aios.ui.fragment.MainFragment;
import com.dudu.aios.ui.fragment.PhotoFragment;
import com.dudu.aios.ui.fragment.PhotoListFragment2;
import com.dudu.aios.ui.fragment.PhotoShowFragment;
import com.dudu.aios.ui.fragment.RepairFaultCodeFragment;
import com.dudu.aios.ui.fragment.RequestNetworkFragment;
import com.dudu.aios.ui.fragment.SafetyMainFragment;
import com.dudu.aios.ui.fragment.VehicleAnimationFragment;
import com.dudu.aios.ui.fragment.VideoFragment;
import com.dudu.aios.ui.fragment.VideoListFragment;
import com.dudu.aios.ui.fragment.VideoPlayFragment;
import com.dudu.aios.ui.fragment.VoipCallingFragment;
import com.dudu.aios.ui.fragment.base.BaseManagerFragment;
import com.dudu.aios.ui.fragment.video.DrivingRecordFragment;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.aios.ui.voice.VoiceEvent;
import com.dudu.aios.ui.voice.VoiceFragment;
import com.dudu.android.hideapi.SystemPropertiesProxy;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.broadcast.ScreenReceiver;
import com.dudu.android.launcher.broadcast.TFlashCardReceiver;
import com.dudu.android.launcher.broadcast.WeatherAlarmReceiver;
import com.dudu.android.launcher.utils.AdminReceiver;
import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.event.Events;
import com.dudu.drivevideo.frontcamera.FrontCameraManage;
import com.dudu.drivevideo.frontcamera.event.StreamEvent;
import com.dudu.drivevideo.spaceguard.event.VideoSpaceEvent;
import com.dudu.event.DeviceEvent;
import com.dudu.init.CarFireManager;
import com.dudu.monitor.obdUpdate.ObdUpdateService;
import com.dudu.navi.event.NaviEvent;
import com.dudu.voice.FloatWindowUtils;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.TTSType;
import com.dudu.voip.VoipSDKCoreHelper;
import com.dudu.workflow.HandlerPushData;
import com.dudu.workflow.common.DataFlowFactory;
import com.dudu.workflow.common.RequestFactory;
import com.dudu.workflow.obd.CarLock;
import com.dudu.workflow.obd.ObdFlow;
import com.dudu.workflow.obd.RobberyFlow;
import com.dudu.workflow.push.model.PushParams;
import com.dudu.workflow.push.model.ReceiverPushData;
import com.dudu.workflow.robbery.RobberyStateModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainRecordActivity extends BaseFragmentManagerActivity {
    public static final String MAIN_FRAGMENT = "mainfragment";
    public static final String DRIVINGRECORD_FRAGMENT = "drivingRecordFragment";
    private static final int SET_PREVIEW = 0;
    private static final int INIT_FRAGMENTS = 1;
    private static final int MY_REQUEST_CODE = 9999;
    private static final String SAFETY_FRAGMENT = "safetyFragment";
    private static final String PHOTO_FRAGMENT = "photoFragment";
    private static final String PHOTOLIST_FRAGMENT = "photoListFragment";
    private static final String VIDEO_FRAGMENT = "videoFragment";
    private static final String FLOW_FRAGMENT = "flowFragment";
    private static final String VOICE_FRAGMENT = "voiceFragment";
    private static final String DEVICEBIND_FRAGMENT = "DeviceBindFragment";
    private static final String VIDEOLIST_FRAGMENT = "videoListFragment";
    private static final String ACCELERATION_FRAGMENT = "AccelerationFragment";
    public static MainRecordActivity appActivity;
    private AlarmManager mAlarmManager;
    private TFlashCardReceiver mTFlashCardReceiver;
    private ScreenReceiver mScreenReceiver;
    private Logger log_init;
    private Logger log_web;
    private DevicePolicyManager mPolicyManager;
    private ComponentName componentName;
    private Subscription robberySubscription;

    private Logger log = LoggerFactory.getLogger("ui.MainRecordActivity");

    /**
     * 该页面显示透明还是黑色背景<br/>
     * true:透明<br/>
     * false:黑色
     */
    protected boolean titleColorIsTransparent = true;

    @Override
    public int fragmentViewId() {
//        return R.id.main_container;
        return R.id.container;
    }

    public Fragment getFragment(String key) {
        Fragment fragment = null;
        if (fragmentMap != null) {
            List<BaseManagerFragment> fragmentList = fragmentMap.get(key);
            if (fragmentList != null && fragmentList.size() > 0) {
                fragment = fragmentList.get(0);
            }
        }
        return fragment;
    }

    @Override
    public Map<String, Class<? extends BaseManagerFragment>> baseFragmentWithTag() {
        Map<String, Class<? extends BaseManagerFragment>> fragmentMap = new HashMap<>();
        fragmentMap.put(MAIN_FRAGMENT, MainFragment.class);
        fragmentMap.put(SAFETY_FRAGMENT, SafetyMainFragment.class);
        fragmentMap.put(DRIVINGRECORD_FRAGMENT, DrivingRecordFragment.class);//行车记录
        fragmentMap.put(PHOTO_FRAGMENT, PhotoFragment.class);
        fragmentMap.put(PHOTOLIST_FRAGMENT, PhotoListFragment2.class);//图片列表
        fragmentMap.put(VIDEO_FRAGMENT, VideoFragment.class);
        fragmentMap.put(FLOW_FRAGMENT, FlowFragment.class);//移动热点
        fragmentMap.put(VOICE_FRAGMENT, VoiceFragment.class);
        fragmentMap.put(VIDEOLIST_FRAGMENT, VideoListFragment.class);
        fragmentMap.put(DEVICEBIND_FRAGMENT, DeviceBindFragment.class);//展示绑定设备的二维码
        fragmentMap.put(FragmentConstants.CAR_CHECKING, CarCheckingFragment.class);//行车自检
        fragmentMap.put(FragmentConstants.BT_DIAL, BtDialFragment.class);//蓝牙电话
        fragmentMap.put(FragmentConstants.BT_CONTACTS, BtContactsFragment.class);//联系人界面
        fragmentMap.put(FragmentConstants.BT_IN_CALL, BtInCallFragment.class);//来电界面
        fragmentMap.put(FragmentConstants.BT_OUT_CALL, BtOutCallFragment.class);//去电界面
        fragmentMap.put(FragmentConstants.BT_CALLING, BtCallingFragment.class);//通话中
        fragmentMap.put(FragmentConstants.BT_DIAL_SELECT_NUMBER, BtDialSelectNumberFragment.class);//多个电话号码选择
        fragmentMap.put(FragmentConstants.VEHICLE_ANIMATION_FRAGMENT, VehicleAnimationFragment.class);//故障清除
        fragmentMap.put(FragmentConstants.REPAIR_FAULT_CODE_FRAGMENT, RepairFaultCodeFragment.class);//汽车修理
        fragmentMap.put(FragmentConstants.PHOTO_SHOW_FRAGMENT, PhotoShowFragment.class);//图片显示
        fragmentMap.put(FragmentConstants.VIDEO_PLAY_FRAGMENT, VideoPlayFragment.class);//视频播放
        fragmentMap.put(FragmentConstants.ACCELERATION_TEST_FRAGMENT, AccelerationTestFragment.class);//加速测试
        fragmentMap.put(FragmentConstants.LICENSE_UPLOAD_UPLOAD_FRAGMENT, LicenseUploadFragment.class);//证件上传
        fragmentMap.put(FragmentConstants.REQUEST_NETWORK_FRAGMENT, RequestNetworkFragment.class);//等待服务器请求
        fragmentMap.put(FragmentConstants.VOIP_CALLING_FRAGMENT, VoipCallingFragment.class);//VOIP呼叫 VIP Service

        return fragmentMap;
    }

    @Override
    public void showDefaultFragment() {
        replaceFragment(FragmentConstants.FRAGMENT_MAIN_PAGE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appActivity = this;
        super.onCreate(null);

        EventBus.getDefault().unregister(this);

        EventBus.getDefault().register(this);

//        initPreview();

        initFragment(savedInstanceState);

        initData();
        FrontCameraManage.getInstance().setBlurGLSurfaceView(baseBinding.frontCameraPreview);
        replaceFragment(FragmentConstants.FRAGMENT_MAIN_PAGE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
//        initFrontPreview();
//        baseBinding.preview.addView(new CameraPreview(this));
//        CameraPreview cameraPreview = new CameraPreview(this);
//        cameraPreview.getHolder().setFixedSize(FrontVideoConfigParam.DEFAULT_WIDTH, FrontVideoConfigParam.DEFAULT_HEIGHT);
//        cameraPreview.setLayoutParams(new FrameLayout.LayoutParams(FrontVideoConfigParam.DEFAULT_WIDTH, FrontVideoConfigParam.DEFAULT_HEIGHT));
//
//        baseBinding.preview.addView(cameraPreview);
//        FrontCameraManage.getInstance().startForegroundRecord();
    }

    public void showMain() {
        if (!FloatWindowUtils.isShowWindow()) {
            replaceFragment(FragmentConstants.FRAGMENT_MAIN_PAGE);
        }
    }

    @Override
    protected View getChildView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_record, null);
    }

    @Override
    public void showFragment(String tagKey, boolean append) {
        super.showFragment(tagKey, append);
    }

    private void initData() {
        log_init = LoggerFactory.getLogger("init.start");
        log_web = LoggerFactory.getLogger("workFlow.webSocket");

        log_init.debug("MainActivity 调用onCreate方法初始化...");

        setWeatherAlarm();

        registerTFlashCardReceiver();

        registerScreenReceiver();

        // 获取设备管理服务
        mPolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        // 自己的AdminReceiver 继承自 DeviceAdminReceiver
        componentName = new ComponentName(this, AdminReceiver.class);

    }

    private void initFragment(Bundle savedInstanceState) {
//        switchToStackByTag(MAIN_FRAGMENT);
    }

    public void replaceFragment(String name, Bundle arg) {
        fragmentArg = arg;
        replaceFragment(name);
    }

    public void showTitleColorTransparent() {
        if (titleColorIsTransparent) {
            if (baseBinding.commonTitleLayout.getBackground() instanceof ColorDrawable) {
                if (((ColorDrawable) baseBinding.commonTitleLayout.getBackground()).getColor() == getResources().getColor(R.color.transparent1)) {
                    return;
                }
            }
            baseBinding.commonTitleLayout.setBackgroundResource(R.color.transparent1);
        } else {
            if (baseBinding.commonTitleLayout.getBackground() instanceof ColorDrawable) {
                if (((ColorDrawable) baseBinding.commonTitleLayout.getBackground()).getColor() == getResources().getColor(R.color.black)) {
                    return;
                }
            }
            baseBinding.commonTitleLayout.setBackgroundResource(R.color.black);
        }
    }

    public void showTitle(boolean isShow) {
        ObservableFactory.getInstance().getCommonObservable().hasTitle.set(isShow);
        ObservableFactory.getInstance().getCommonObservable().hasBackground.set(false);
    }

    @DebugLog
    public void replaceFragment(String name) {
        switch (name) {

            //视频播放
            case FragmentConstants.VIDEO_PLAY_FRAGMENT:
                switchToStackByTag(FragmentConstants.VIDEO_PLAY_FRAGMENT);
                break;

            //图片显示
            case FragmentConstants.PHOTO_SHOW_FRAGMENT:
                switchToStackByTag(FragmentConstants.PHOTO_SHOW_FRAGMENT);
                break;

            //汽车修理
            case FragmentConstants.REPAIR_FAULT_CODE_FRAGMENT:
                switchToStackByTag(FragmentConstants.REPAIR_FAULT_CODE_FRAGMENT);
                break;

            //故障清除
            case FragmentConstants.VEHICLE_ANIMATION_FRAGMENT:
                switchToStackByTag(FragmentConstants.VEHICLE_ANIMATION_FRAGMENT);
                break;

            //蓝牙电话打进界面
            case FragmentConstants.BT_IN_CALL:
                switchToStackByTag(FragmentConstants.BT_IN_CALL);
                break;
            //蓝牙电话拨出界面
            case FragmentConstants.BT_OUT_CALL:
                switchToStackByTag(FragmentConstants.BT_OUT_CALL);
                break;

            //蓝牙电话联系人界面
            case FragmentConstants.BT_CONTACTS:
                switchToStackByTag(FragmentConstants.BT_CONTACTS);
                break;

            //蓝牙电话拨号界面
            case FragmentConstants.BT_DIAL:
                switchToStackByTag(FragmentConstants.BT_DIAL);
                break;
            //蓝牙电话通话中界面
            case FragmentConstants.BT_CALLING:
                switchToStackByTag(FragmentConstants.BT_CALLING);
                break;
            //多个号码选择界面
            case FragmentConstants.BT_DIAL_SELECT_NUMBER:
                switchToStackByTag(FragmentConstants.BT_DIAL_SELECT_NUMBER);
                break;

            //行车自检
            case FragmentConstants.CAR_CHECKING:
                switchToStackByTag(FragmentConstants.CAR_CHECKING);
                break;

            case FragmentConstants.FRAGMENT_MAIN_PAGE:
                switchToStackByTag(MAIN_FRAGMENT);
                break;

            case FragmentConstants.FRAGMENT_SAFETY_CENTER:
                switchToStackByTag(SAFETY_FRAGMENT);
                break;

            case FragmentConstants.FRAGMENT_DRIVING_RECORD:
//                行车记录界面,取消模糊
//                setBlur(false);
                switchToStackByTag(DRIVINGRECORD_FRAGMENT);
                break;

            case FragmentConstants.FRAGMENT_VIDEO_LIST:
                switchToStackByTag(VIDEOLIST_FRAGMENT);
                break;

            case FragmentConstants.FRAGMENT_VIDEO:
                switchToStackByTag(VIDEO_FRAGMENT);
                break;

            case FragmentConstants.FRAGMENT_PHOTO_LIST:
                switchToStackByTag(PHOTOLIST_FRAGMENT);
                break;

            case FragmentConstants.FRAGMENT_PHOTO:
                switchToStackByTag(PHOTO_FRAGMENT);
                break;

            case FragmentConstants.FRAGMENT_FLOW:
                switchToStackByTag(FLOW_FRAGMENT);
                break;
            case FragmentConstants.VOICE_FRAGMENT:
                switchToStackByTag(VOICE_FRAGMENT);
                break;
            case FragmentConstants.FRAGMENT_DEVICE_BINDING:
                switchToStackByTag(DEVICEBIND_FRAGMENT);
                break;
            case FragmentConstants.ACCELERATION_TEST_FRAGMENT:
                switchToStackByTag(FragmentConstants.ACCELERATION_TEST_FRAGMENT);
                break;
            case FragmentConstants.LICENSE_UPLOAD_UPLOAD_FRAGMENT:
                switchToStackByTag(FragmentConstants.LICENSE_UPLOAD_UPLOAD_FRAGMENT);
                break;
            case FragmentConstants.VOIP_CALLING_FRAGMENT:
                switchToStackByTag(FragmentConstants.VOIP_CALLING_FRAGMENT);
                break;
            case FragmentConstants.REQUEST_NETWORK_FRAGMENT:
                switchToStackByTag(FragmentConstants.REQUEST_NETWORK_FRAGMENT);
                break;
            default:
                switchToStackByTag(MAIN_FRAGMENT);
                break;
        }

        if (!name.equals(FragmentConstants.VOICE_FRAGMENT)) {
            lastThirdFragment = lastSecondFragment;
            lastSecondFragment = lastFragment;
            lastFragment = name;
        }
    }

    @Override
    public void switchToStackByTag(String tag){
        changeTitleColor(tag);
        super.switchToStackByTag(tag);
    }

    private void changeTitleColor(String tag){
        titleColorIsTransparent = true;
        switch (tag){
            case FragmentConstants.CAR_CHECKING:
            case FragmentConstants.VEHICLE_ANIMATION_FRAGMENT:
            case FragmentConstants.REPAIR_FAULT_CODE_FRAGMENT:
                titleColorIsTransparent = false;
                break;
        }
    }

    private void setWeatherAlarm() {
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, WeatherAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 20);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 30 * 60 * 1000, pi);
    }

    private void registerTFlashCardReceiver() {
        mTFlashCardReceiver = new TFlashCardReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addDataScheme("file");
        registerReceiver(mTFlashCardReceiver, intentFilter);
    }

    private void registerScreenReceiver() {
        mScreenReceiver = new ScreenReceiver();

        IntentFilter filter = new IntentFilter();
        // 屏幕灭屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        // 屏幕亮屏广播
        filter.addAction(Intent.ACTION_SCREEN_ON);
        // 屏幕解锁广播
        filter.addAction(Intent.ACTION_USER_PRESENT);
        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
        // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
        // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        registerReceiver(mScreenReceiver, filter);
    }

    private void cancelWeatherAlarm() {
        Intent intent = new Intent(this, WeatherAlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        mAlarmManager.cancel(pi);
    }

    public void onEventMainThread(DeviceEvent.Screen event) {
        log_init.debug("DeviceEvent.Screen {}", event.getState());
        if (event.getState() == DeviceEvent.OFF) {
            if (mPolicyManager.isAdminActive(componentName)) {
                mPolicyManager.lockNow();// 锁屏
            } else {
                activeManage(); //获取权限
            }
        } else {
            wakeScreen();
        }
    }

    private void wakeScreen() {
        CarFireManager.getInstance().acquireLock();
        CarFireManager.getInstance().releaseWakeLockIfNotFired();
    }

    private void activeManage() {
        // 启动设备管理(隐式Intent) - 在AndroidManifest.xml中设定相应过滤器
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);

        // 权限列表
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);

        // 描述(additional explanation) 在申请权限时出现的提示语句
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "激活后就能一键锁屏了");

        startActivityForResult(intent, MY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 获取权限成功，立即锁屏并finish自己，否则继续获取权限
        if (requestCode == MY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mPolicyManager.lockNow();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onResume() {
        super.onResume();
        log.debug("MainRecordActivity onResume");

        observableFactory.getCommonObservable(baseBinding).hasTitle.set(true);
        observableFactory.getCommonObservable().hasBackground.set(false);

//        if (cameraView != null) {
//            cameraView.onResume();
//        }
//        cameraView.resumePreview();

        LauncherApplication.startRecord = false;
        EventBus.getDefault().post(NaviEvent.FloatButtonEvent.HIDE);
        if (!isSaveInstanceState) {
            //chad add 加上该判定是为了避免activity进入onSaveInstanceState方法后再执行transaction.commit();时候报错

//            if (LauncherApplication.startRecord) {
//                replaceFragment(FragmentConstants.FRAGMENT_DRIVING_RECORD);
//            } else {
//                showMain();
//            }
        }

        isSaveInstanceState = false;
        LoggerFactory.getLogger("video.frontdrivevideo").debug("MainRecordActivity onResume");
        FrontCameraManage.getInstance().startForegroundRecord();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    public void onDialButtonClick(View view) {
        if (TextUtils.equals(FragmentConstants.BT_DIAL, currentStackTag)) {
            List<BaseManagerFragment> fragmentList = fragmentMap.get(FragmentConstants.BT_DIAL);
            ((BtDialFragment) fragmentList.get(fragmentList.size() - 1)).onDialButtonClick(view);
        } else if (TextUtils.equals(FragmentConstants.BT_CALLING, currentStackTag)) {
            List<BaseManagerFragment> fragmentList = fragmentMap.get(FragmentConstants.BT_CALLING);
            ((BtCallingFragment) fragmentList.get(fragmentList.size() - 1)).onDialButtonClick(view);
        } else if (TextUtils.equals(FragmentConstants.VOIP_CALLING_FRAGMENT, currentStackTag)) {
            List<BaseManagerFragment> fragmentList = fragmentMap.get(FragmentConstants.VOIP_CALLING_FRAGMENT);
            ((VoipCallingFragment) fragmentList.get(fragmentList.size() - 1)).onDialButtonClick(view);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        log.debug("MainRecordActivity onPause");
//        CameraInstance.getInstance().stopCamera();
//        if (cameraView != null) {
//            cameraView.release(null);
//            cameraView.onPause();
//        }

//        cameraView.stopPreview();

        LoggerFactory.getLogger("video.frontdrivevideo").debug("MainRecordActivity onPause");
//        FrontCameraManage.getInstance().startBackgroundRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        CameraInstance.getInstance().stopCamera();

        log_init.debug("MainRecordActivity 调用onDestroy释放资源...");

//        InitManager.getInstance().unInit();

        cancelWeatherAlarm();

        unregisterReceiver(mTFlashCardReceiver);
        unregisterReceiver(mScreenReceiver);

        //释放Voip资源
        VoipSDKCoreHelper.getInstance().release();

        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LoggerFactory.getLogger("ui.MainRecordActivity").debug("onSaveInstanceState method called!");


    }

    public void onEventBackgroundThread(ReceiverPushData data) {
        log_web.debug("收到推送数据");
        HandlerPushData.getInstance().handlerData(data);
        setCarTypeIfGetted(data);

        if (data != null && data.resultCode == 0 && data.result != null) {
            if (data.result.method != null && PushParams.LAUNCHER_UPGRADE.equals(data.result.method)) {
                LoggerFactory.getLogger("car.obdUpdate").info("收到推送升级消息----");
                ObdUpdateService.getInstance().delayQueryServerVersion(0);
            }
        }
    }

    public void onEventBackgroundThread(VideoSpaceEvent videoSpaceEvent) {
        VoiceManagerProxy.getInstance().startSpeaking(videoSpaceEvent.getMesageToSpeak(), TTSType.TTS_DO_NOTHING, false);
    }

    public void setCarTypeIfGetted(ReceiverPushData data) {
        Observable.just(data)
                .filter(data1 -> data1 != null)
                .filter(data2 -> data2.resultCode == 0)
                .filter(data3 -> data3.result != null)
                .map(data4 -> data4.result)
                .filter(result -> result.method != null)
                .map(result1 -> result1.method)
                .filter(method -> method == PushParams.THEFT_APPROVAL)
                .subscribe(method1 -> ObdFlow.setCarType(data.result.obd_car_no)
                        , throwable -> log_init.error("setCarTypeIfGetted", throwable));


    }


    public void onEventMainThread(Events.DeviceEvent event) {
        if (event.getEvent() == Events.REBOOT) {
            VoiceManagerProxy.getInstance().startSpeaking(
                    getString(R.string.obd_update_success_reboot), TTSType.TTS_DO_NOTHING, false);
            Observable.timer(10, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> SystemPropertiesProxy.getInstance().set(this, "persist.sys.boot", "reboot"), throwable -> log_init.error("onEventMainThread", throwable));
        }
    }

    public void onEventMainThread(Events.TestSpeedEvent data) {
        int testSpeedStatus = data.getEvent();
        switch (testSpeedStatus) {
            case Events.TEST_SPEED_START:
                replaceFragment(FragmentConstants.ACCELERATION_TEST_FRAGMENT);
                break;
            case Events.TEST_SPEED_ZERO:
                log_init.debug("请停止车速后再开始测速。。");
                VoiceManagerProxy.getInstance().startSpeaking(
                        CommonLib.getInstance().getContext().getString(R.string.test_speed_after_stop), TTSType.TTS_DO_NOTHING, false);
                break;
        }
    }

    public void onEventMainThread(Events.RobberyEvent data) {
        try {
            if (robberySubscription != null) {
                robberySubscription.unsubscribe();
            }
            LoggerFactory.getLogger("workFlow.Robbery").info("收到RobberyEvent，开启防劫（踩油门检测）");
            robberySubscription = RobberyFlow.checkGunSwitch(data.getRevolutions(), data.getNumberOfOperations(), data.getCompleteTime());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onEventMainThread(VoiceEvent event) {
        switch (event) {
            case SHOW_ANIM:
                log_init.debug("voice onEvent show anim");
                replaceFragment(FragmentConstants.VOICE_FRAGMENT);
                break;
            case DISMISS_WINDOW:
                replaceFragment(lastFragment);
                break;
        }

    }

    public void onEventMainThread(StreamEvent event) {
        if (event.getState() == StreamEvent.START) {
            mLogoImageView.startAnimation(getRotateAnimation());
        } else {
            mLogoImageView.clearAnimation();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (TextUtils.equals(FragmentConstants.BT_IN_CALL, currentStackTag)) {
            List<BaseManagerFragment> fragmentList = fragmentMap.get(FragmentConstants.BT_IN_CALL);
            ((BtInCallFragment) fragmentList.get(fragmentList.size() - 1)).dispatchKeyEvent(event);
        } else if (TextUtils.equals(FragmentConstants.BT_CALLING, currentStackTag)) {
            List<BaseManagerFragment> fragmentList = fragmentMap.get(FragmentConstants.BT_CALLING);
            ((BtCallingFragment) fragmentList.get(fragmentList.size() - 1)).dispatchKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }

    public void onEventMainThread(RobberyStateModel event) {
        log_init.debug("收到防劫模式触发事件:" + event.getRobberyState());
        DataFlowFactory.getRobberyMessageFlow().obtainRobberyMessage()
                .map(robberyMessage -> robberyMessage.isRobberySwitch())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(switchIsOn -> {
                    log_init.debug("查询本地是否打开了防3次踩油门:" + switchIsOn);
                    if (switchIsOn) {
                        DataFlowFactory.getSwitchDataFlow().saveGuardSwitch(true);
                        EventBus.getDefault().post(new Events.GuardSwitchState(true));
                        checkCarlock(event.getRobberyState());
                        requestCheckSwitch();
                    }
                }, (error) -> {
                    log_init.error("收到防劫模式触发事件:" + event.getRobberyState(), error);
                });
    }

    public void requestCheckSwitch() {
        RequestFactory.getGuardRequest().checkLockCar(true)
                .subscribe(requestResponse -> {
                    if (requestResponse.resultCode == 0) {
                        log_init.debug("requestCheckSwitch:成功");
                    } else {
                        log_init.debug("requestCheckSwitch:" + requestResponse.resultMsg);
                    }

                }, throwable -> log_init.error("requestCheckSwitch", throwable));
    }

    public void checkCarlock(boolean lock) {
        if (lock) {
            CarLock.lockCar();
        } else {
            CarLock.unlockCar();
        }
    }
}
