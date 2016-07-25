package com.dudu.aios.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dudu.aios.ui.activity.MainRecordActivity;
import com.dudu.aios.ui.base.ObservableFactory;
import com.dudu.aios.ui.fragment.base.BaseFragment;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.BtPhoneUtils;
import com.dudu.android.launcher.utils.Contacts;
import com.dudu.android.launcher.utils.LogUtils;
import com.dudu.android.launcher.utils.ViewAnimation;
import com.dudu.android.launcher.utils.WeatherUtils;
import com.dudu.commonlib.event.Events;
import com.dudu.commonlib.utils.ModelUtil;
import com.dudu.commonlib.utils.TextVerify;
import com.dudu.drivevideo.frontcamera.FrontCameraManage;
import com.dudu.map.NavigationProxy;
import com.dudu.monitor.utils.SharedPreferencesUtil;
import com.dudu.persistence.RobberyMessage.RobberyMessage;
import com.dudu.rest.model.GetGuardStatusResponse;
import com.dudu.rest.model.GetRobberyStatusResponse;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voip.VoipSDKCoreHelper;
import com.dudu.weather.WeatherFlow;
import com.dudu.weather.WeatherInfo;
import com.dudu.weather.WeatherStream;
import com.dudu.workflow.common.DataFlowFactory;
import com.dudu.workflow.common.RequestFactory;
import com.dudu.workflow.obd.VehicleConstants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

//import rx.android.schedulers.AndroidSchedulers;


public class MainFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "ui.MainFragment";

    private static final int STANDBY_INTERVAL = 15000;

    private LinearLayout vehicleInspection, drivingRecord, navigation, bluetoothPhone, flow, preventRob;
    private RelativeLayout mDateWeatherContainer, mScreenContainer;
    private TextView mDateTextView, mWeatherView, mTemperatureView;
    private ImageView mWeatherImage;
    private ImageButton voice_imageBtn;

    private LinearLayout mMenuButtonContainer, vipServerContainer;

    private Handler animHandler = new ViewDisappearHandler();
    private boolean isStartAnimation = false;

    @Override
    public View getView() {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_main_layout, null);

        initFragmentView(view);

        initOnClickListener(view);

        initData();

        EventBus.getDefault().unregister(this);

        EventBus.getDefault().register(this);

        return view;
    }

    private void initData() {

        initModel();

        initDate();

        getWeather();
    }

    private void initModel() {
        if (ModelUtil.needVip()) {
            vipServerContainer.setVisibility(View.VISIBLE);
        } else {
            vipServerContainer.setVisibility(View.INVISIBLE);
        }
    }

    private void initDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
        if ("1".equals(mWay)) {
            mWay = "星期天";
        } else if ("2".equals(mWay)) {
            mWay = "星期一";
        } else if ("3".equals(mWay)) {
            mWay = "星期二";
        } else if ("4".equals(mWay)) {
            mWay = "星期三";
        } else if ("5".equals(mWay)) {
            mWay = "星期四";
        } else if ("6".equals(mWay)) {
            mWay = "星期五";
        } else if ("7".equals(mWay)) {
            mWay = "星期六";
        }

        mDateTextView.setText(dateFormat.format(new Date()) + " " + mWay);
    }

    private void getWeather() {
        weatherSubscriber(WeatherFlow.getInstance().requestWeather());
        WeatherStream.getInstance().startService();

    }

    private void weatherSubscriber(Observable<WeatherInfo> observable) {
        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(weatherInfo -> {
                            updateWeatherInfo(weatherInfo.getWeather(), weatherInfo.getTemperature());
                            initDate();
                        },
                        throwable -> {
                        });
    }

    private void initOnClickListener(View view) {
        vehicleInspection.setOnClickListener(this);
        drivingRecord.setOnClickListener(this);
        navigation.setOnClickListener(this);
        bluetoothPhone.setOnClickListener(this);
        flow.setOnClickListener(this);
        preventRob.setOnClickListener(this);
        voice_imageBtn.setOnClickListener(this);
        vipServerContainer.setOnClickListener(this);
        //mScreenContainer.setOnClickListener(this);
        mScreenContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    isStartAnimation = false;
                    starDisappearTask();
                    if (mDateWeatherContainer.getVisibility() == View.GONE) {
                        toggleAnimation();
                    }
                }
                return false;
            }
        });

        mWeatherImage.setOnLongClickListener(v -> {

            return true;
        });

    }

    private void initFragmentView(View view) {

        vipServerContainer = (LinearLayout) view.findViewById(R.id.linearLayout_voip_service);
        mMenuButtonContainer = (LinearLayout) view.findViewById(R.id.button_menu_container);
        mDateWeatherContainer = (RelativeLayout) view.findViewById(R.id.date_weather_container);
        mScreenContainer = (RelativeLayout) view.findViewById(R.id.screen_container);

        vehicleInspection = (LinearLayout) view.findViewById(R.id.vehicle_inspection);
        drivingRecord = (LinearLayout) view.findViewById(R.id.driving_record_button);
        navigation = (LinearLayout) view.findViewById(R.id.navigation_button);
        bluetoothPhone = (LinearLayout) view.findViewById(R.id.bluetooth_phone_button);
        flow = (LinearLayout) view.findViewById(R.id.flow_button);
        preventRob = (LinearLayout) view.findViewById(R.id.prevent_rob);
        mDateTextView = (TextView) view.findViewById(R.id.text_date);
        mTemperatureView = (TextView) view.findViewById(R.id.text_temperature);
        mWeatherView = (TextView) view.findViewById(R.id.text_weather);
        mWeatherImage = (ImageView) view.findViewById(R.id.weather_icon);
        voice_imageBtn = (ImageButton) view.findViewById(R.id.voice_imageBtn);
    }

    @DebugLog
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.screen_container:
                if (isStartAnimation) {
                    isStartAnimation = false;
                    starDisappearTask();
                    if (mDateWeatherContainer.getVisibility() == View.GONE) {
                        toggleAnimation();
                    }
                }
                break;
            case R.id.vehicle_inspection:
            case R.id.vehicle_inspection_icon:
//                vehicleInspection.setEnabled(false);
//                iconVehicleInspection.setEnabled(false);
                //行车自检
                replaceFragment(FragmentConstants.REQUEST_NETWORK_FRAGMENT);
                requestObtainGuardMessage(FragmentConstants.CAR_CHECKING);
                break;

            case R.id.driving_record_button:
            case R.id.driving_record_icon:
//                drivingRecord.setEnabled(false);
//                iconDrivingRecord.setEnabled(false);
                //行车记录
                replaceFragment(FragmentConstants.FRAGMENT_DRIVING_RECORD);

                break;

            case R.id.navigation_button:
            case R.id.navigation_icon:
//                navigation.setEnabled(false);
//                iconNavigation.setEnabled(false);
                //导航
                NavigationProxy.getInstance().openNavi(NavigationProxy.OPEN_MANUAL);

                break;

            case R.id.bluetooth_phone_button:
            case R.id.bluetooth_phone_icon:
                //蓝牙电话
                if (VoipSDKCoreHelper.getInstance().eccall_state != VoipSDKCoreHelper.ERROR_ECCALL_PROCEEDING &&
                        VoipSDKCoreHelper.getInstance().eccall_state != VoipSDKCoreHelper.ERROR_ECCALL_ANSWERED &&
                        VoipSDKCoreHelper.getInstance().eccall_state != VoipSDKCoreHelper.ERROR_ECCALL_ALERTING) {

                    if (BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_ACTIVE) {
                        Log.d("phone", "BtPhoneUtils.btCallState:" + BtPhoneUtils.btCallState);
                        replaceFragment(FragmentConstants.BT_CALLING);
                    } else {
                        replaceFragment(FragmentConstants.BT_DIAL);
                    }
                }
                // replaceFragment(FragmentConstants.FRAGMENT_DEVICE_BINDING);
                break;

            case R.id.flow_button:
            case R.id.flow_icon:
//                flow.setEnabled(false);
//                iconFlow.setEnabled(false);
                //wifi热点
                replaceFragment(FragmentConstants.FRAGMENT_FLOW);
                break;

            case R.id.prevent_rob:
            case R.id.prevent_rob_icon:
//                preventRob.setEnabled(false);
//                iconRob.setEnabled(false);
                replaceFragment(FragmentConstants.REQUEST_NETWORK_FRAGMENT);
                requestObtainGuardMessage(FragmentConstants.FRAGMENT_SAFETY_CENTER);
                requestServerRobberyMessage();

                break;

            case R.id.voice_imageBtn:
//                voice_imageBtn.setEnabled(false);
                VoiceManagerProxy.getInstance().startVoiceService();
                break;
            case R.id.linearLayout_voip_service:
                Bundle voipBundle = new Bundle();
                voipBundle.putBoolean(VoipCallingFragment.VOIP_CALL_BUNDLE_KEY, true);
                FragmentConstants.TEMP_ARGS = voipBundle;
                replaceFragment(FragmentConstants.VOIP_CALLING_FRAGMENT);
                break;

        }
    }

    private void starDisappearTask() {
        animHandler.removeCallbacksAndMessages(null);
        animHandler.sendEmptyMessageDelayed(0, STANDBY_INTERVAL);
    }


    private void updateWeatherInfo(String weather, String temperature) {

        if (!TextUtils.isEmpty(weather) && !TextUtils.isEmpty(temperature)) {
            if (weather.contains("-")) {
                weather = weather
                        .replace("-", getString(R.string.weather_turn));
            }

            mTemperatureView.setTextSize(sp2px(getContext(), 22));

            mTemperatureView.setText(temperature + getString(R.string.temperature_degree));

            mWeatherView.setText(weather);
            mWeatherImage.setImageResource(WeatherUtils
                    .getWeatherIcon(WeatherUtils.getWeatherType(weather)));
        } else {
            //获取天气失败
            mWeatherView.setGravity(Gravity.CENTER);
            mWeatherView.setText(R.string.unkown_weather_info);
            mTemperatureView.setText("");
        }
    }

    public int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.v(TAG, "onResume()..");

        showMainFragmentAnim();

        vehicleInspection.setEnabled(true);
        drivingRecord.setEnabled(true);
        navigation.setEnabled(true);
        bluetoothPhone.setEnabled(true);
        flow.setEnabled(true);
        preventRob.setEnabled(true);

        voice_imageBtn.setEnabled(true);

        toggleBlur(true);
//        FrontCameraManage.getInstance().setPreviewBlur(true);
        ObservableFactory.getInstance().getCommonObservable().hasBackground.set(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        animHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onHide() {
        super.onHide();
        LogUtils.v(TAG, "onHide()");
        animHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onShow() {
        super.onShow();
        LogUtils.v(TAG, "onShow()..");
        showMainFragmentAnim();
        ObservableFactory.getInstance().getCommonObservable().hasBackground.set(false);
        FrontCameraManage.getInstance().setPreviewBlur(true);
    }

    private void showMainFragmentAnim() {
        mDateWeatherContainer.clearAnimation();
        mMenuButtonContainer.clearAnimation();
        mMenuButtonContainer.setVisibility(View.VISIBLE);
        mDateWeatherContainer.setVisibility(View.VISIBLE);
        isStartAnimation = false;
        animHandler.removeCallbacksAndMessages(null);
        animHandler.sendEmptyMessageDelayed(0, STANDBY_INTERVAL);
        MainRecordActivity.appActivity.showTitle(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(getActivity());
    }

    private void toggleAnimation() {
        //关闭模糊,启动录像
        toggleBlur(!mDateWeatherContainer.isShown());

        MainRecordActivity mainRecordActivity = (MainRecordActivity) getActivity();
        Fragment mainFragment = mainRecordActivity.getFragment(MainRecordActivity.MAIN_FRAGMENT);
        if (mainFragment != null && mainFragment.isVisible()) {
            //当前在主界面
            toggleMenuButtonAnimation();
            toggleDateWeatherAnimation();
        }
    }

    private void toggleBlur(boolean blur) {
        MainRecordActivity mainRecordActivity = (MainRecordActivity) getActivity();
        Fragment fragment = mainRecordActivity.getFragment(MainRecordActivity.DRIVINGRECORD_FRAGMENT);
        if (fragment != null && fragment.isVisible()) {
            //行车记录界面
        } else {
//            ((BaseActivity) getActivity()).setBlur(blur);
            FrontCameraManage.getInstance().setPreviewBlur(blur);
        }
    }


    private void toggleDateWeatherAnimation() {
        ViewAnimation.startAnimation(mDateWeatherContainer, mDateWeatherContainer.getVisibility() == View.VISIBLE ? R.anim.date_weather_disappear : R.anim.date_weather_appear, getActivity());
        ViewAnimation.onAnimPlayListener(isPlay -> isStartAnimation = true);
    }

    private void toggleMenuButtonAnimation() {
        ViewAnimation.startAnimation(mMenuButtonContainer, mMenuButtonContainer.getVisibility() == View.VISIBLE ? R.anim.menu_button_disappear : R.anim.menu_button_appear, getActivity());
    }

    private class ViewDisappearHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            toggleAnimation();
        }
    }

    private void requestObtainGuardMessage(String fragmentName) {
        RequestFactory.getGuardRequest().getLockStatus().subscribe(statusResponse -> {
                    logger.debug("请求获取防盗的网络的状态:" + statusResponse.result + "resultCode:" + statusResponse.resultCode);
                    if (statusResponse != null) {
                        if (statusResponse.resultCode == 0) {
                            SharedPreferencesUtil.putBooleanValue(Contacts.BINDING_STATE, true);
                            GetGuardStatusResponse.GetGuardStatusResult result = statusResponse.result;
                            if (result != null) {
                                int auditStatus = result.audit_state;
                                logger.debug("开始保存从服务器请求下来的审核状态：" + auditStatus);
                                if (auditStatus == Contacts.AUDIT_STATE_PASS) {
                                    String gesturePassword = result.protect_thief_signal_password;
                                    logger.debug("开始保存从服务器请求下来的手势密码：" + gesturePassword);
                                    int gesturePasswordSwitchState = result.protect_thief_signal_state;
                                    logger.debug("开始保存从服务器请求下来的手势密码的开关状态：" + (gesturePasswordSwitchState == 1 ? "开启" : "关闭"));
                                    String digitPassword = result.protect_thief_password;
                                    logger.debug("开始保存从服务器请求下来的数字密码：" + digitPassword);
                                    int digitPasswordSwitchState = result.protect_thief_state;
                                    logger.debug("开始保存从服务器请求下来的数字  密码的开关状态：" + (digitPasswordSwitchState == 1 ? "开启" : "关闭"));
                                    int guardSwitchStatus = statusResponse.result.thief_switch_state;
                                    logger.debug("开始保存从服务器请求下来的防盗的开关状态  ：" + guardSwitchStatus);
                                    DataFlowFactory.getUserMessageFlow().saveGuardStatus(gesturePassword, gesturePasswordSwitchState == 1 ? true : false, digitPassword, digitPasswordSwitchState == 1 ? true : false, auditStatus);
                                    checkGuardSwitchFromDB(guardSwitchStatus == 1);
                                    if (fragmentName.equals(FragmentConstants.CAR_CHECKING)) {
                                        Bundle bundle = new Bundle();
                                        bundle.putBoolean(VehicleConstants.START_CHECKING, true);
                                        FragmentConstants.TEMP_ARGS = bundle;
                                    }
                                    replaceFragment(fragmentName);
                                } else {
                                    DataFlowFactory.getUserMessageFlow().saveAuditState(auditStatus);
                                    if (fragmentName.equals(FragmentConstants.CAR_CHECKING)) {
                                        showLicensePromptFragment();
                                    } else {
                                        replaceFragment(fragmentName);
                                    }

                                }
                            }

                        } else if (statusResponse.resultCode == 40200) {
                            showBindingFragments();
                        }
                    }
                }
                , throwable -> {
                    logger.error("getLockStatus onError:" + throwable);
                    queryAuditStateDB(fragmentName);
                }

        );
    }

    private void checkGuardSwitchFromDB(boolean isOpen) {
        DataFlowFactory.getSwitchDataFlow().getGuardSwitch().subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                logger.debug("查看本地与服务器是否相同：" + ((isOpen == aBoolean) ? "相同" : "不相同"));
                if (!isOpen == aBoolean) {
                    syncServerGuardSwitch(aBoolean);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.debug("throwable:" + throwable);
                DataFlowFactory.getSwitchDataFlow().saveGuardSwitch(isOpen);
            }
        });
    }

    private void showLicensePromptFragment() {
        SharedPreferencesUtil.putLongValue(Contacts.LICENSE_TYPE, Contacts.DRIVING_TYPE);
        replaceFragment(FragmentConstants.LICENSE_UPLOAD_UPLOAD_FRAGMENT);
    }

    protected void showBindingFragments() {
        replaceFragment(FragmentConstants.FRAGMENT_DEVICE_BINDING);
    }

    private void queryAuditStateDB(String fragmentName) {
        boolean hasBinded = SharedPreferencesUtil.getBooleanValue(Contacts.BINDING_STATE, false);
        if (hasBinded) {
            DataFlowFactory.getUserMessageFlow().obtainUserMessage()
                    .map(userMessage -> userMessage.getAudit_state())
                    .subscribe(auditState -> {
                        logger.debug("查询数据库的审核状态：" + auditState);
                        if (auditState == Contacts.AUDIT_STATE_PASS) {
                            if (fragmentName.equals(FragmentConstants.CAR_CHECKING)) {
                                Bundle bundle = new Bundle();
                                bundle.putBoolean(VehicleConstants.START_CHECKING, true);
                                FragmentConstants.TEMP_ARGS = bundle;
                            }
                            replaceFragment(fragmentName);
                        } else {
                            if (fragmentName.equals(FragmentConstants.CAR_CHECKING)) {
                                showLicensePromptFragment();
                            } else {
                                replaceFragment(fragmentName);
                            }
                        }

                    }, throwable -> logger.error("queryAuditStateDB", throwable));

        } else {
            showBindingFragments();
        }
    }

    private void requestServerRobberyMessage() {
        RequestFactory.getRobberyRequest()
                .getRobberyState()
                .subscribe(requestResponse -> {
                    logger.debug("获取服务器的防劫的信息  resultCode：{}{}", requestResponse.resultCode, requestResponse.result);
                    if (requestResponse != null && requestResponse.result != null) {
                        String auditStatus = requestResponse.result.audit_state;
                        SharedPreferencesUtil.putLongValue(Contacts.AUDIT_STATE, Long.parseLong(auditStatus));
                        transFormRobberyMessage(requestResponse.result);
                    }
                }, throwable -> {
                    logger.error("getRobberyState", throwable);
                });
    }

    private void transFormRobberyMessage(GetRobberyStatusResponse.Result result) {

        if (!TextVerify.isEmpty(result.protect_rob_state) && !TextVerify.isEmpty(result.revolutions) && !TextVerify.isEmpty(result.numberOfOperations) && !TextVerify.isEmpty(result.completeTime)) {
            RobberyMessage robberyMessage = new RobberyMessage();
            robberyMessage.setRobberySwitch("1".equals(result.protect_rob_state) ? true : false);
            robberyMessage.setRotatingSpeed(result.revolutions);
            robberyMessage.setCompleteTime(result.completeTime);
            robberyMessage.setOperationNumber(result.numberOfOperations);
            DataFlowFactory.getRobberyMessageFlow().changeRobberyMessage(robberyMessage);
        }
    }


    public void onEventMainThread(Events.OpenSafeCenterEvent event) {
        logger.debug("收到语音打开的事件：" + event.getOpenType());
        switch (event.getOpenType()) {
            case Events.OPEN_SAFETY_CENTER:
                replaceFragment(FragmentConstants.REQUEST_NETWORK_FRAGMENT);
                requestObtainGuardMessage(FragmentConstants.FRAGMENT_SAFETY_CENTER);
                requestServerRobberyMessage();
                break;
            case Events.OPEN_VEHICLE_INSPECTION:
                replaceFragment(FragmentConstants.REQUEST_NETWORK_FRAGMENT);
                requestObtainGuardMessage(FragmentConstants.CAR_CHECKING);
                break;
        }

    }

    private void syncServerGuardSwitch(boolean isOpen) {
        logger.debug(isOpen ? "lockGuard" : "unlockGuard");
        RequestFactory.getGuardRequest()
                .checkLockCar(isOpen)
                .subscribe(setGuardStateResponse -> {
                    if (setGuardStateResponse.resultCode == 0) {
                        logger.debug(isOpen ? "加锁成功" : "解锁成功");
                    } else {
                        logger.debug(setGuardStateResponse.resultMsg);
                    }

                }, throwable -> {
                    logger.error(isOpen ? "lockGuard" : "unlockGuard" + " getError", throwable);
                });
    }


}
