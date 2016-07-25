package com.dudu.aios.ui.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import com.blur.BlurGLSurfaceView;
import com.dudu.aios.ui.view.RotateAnimation;
import com.dudu.android.launcher.BuildConfig;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.databinding.ActivityLayoutCommonBinding;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.commonlib.resource.sdcard.ISdcardListener;
import com.dudu.commonlib.resource.sdcard.SdcardManager;
import com.dudu.commonlib.utils.VersionTools;
import com.dudu.drivevideo.frontcamera.FrontCameraManage;
import com.dudu.drivevideo.rearcamera.RearCameraManage;
import com.dudu.drivevideo.utils.FileUtil;
import com.dudu.init.InitManager;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.TTSType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Subscription;

public abstract class BaseActivity extends FragmentActivity implements ISdcardListener {

    private static final String TAG = "BaseActivity";
    public static Logger log = LoggerFactory.getLogger("ui.BaseActivity");
    protected ActivityLayoutCommonBinding baseBinding;
    protected ObservableFactory observableFactory;
    protected View childView;
    //    protected Handler mHandler;
    protected VolBrightnessSetting volBrightnessSetting;
    protected BlurGLSurfaceView mBlurGLSurfaceView;//模糊显示,暂停录像
    protected View mLogoImageView;
    boolean isBlur = true;//当前模糊的状态标识
    boolean isRecordWarn = false;//录像空间不足的提醒标志.一次TF卡拔插
    private Subscription setStateSubscription;
    public static String lastFragment = "default";
    public static String lastSecondFragment = "default";//在lastFragment更前面的一个
    public static String lastThirdFragment = "default";//在lastSecondFragment更前面的一个
    private RotateAnimation mRotateAnimation;
    private VolumeReceiver mVolumeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log.debug("onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        InitManager.getInstance().init();

        baseBinding = DataBindingUtil.setContentView(this, R.layout.activity_layout_common);

        childView = getChildView();
        baseBinding.mainContainer.addView(childView);

        observableFactory = ObservableFactory.getInstance();

        baseBinding.setTitle(observableFactory.getTitleObservable());

        baseBinding.setCommon(observableFactory.getCommonObservable(baseBinding));

     /*   mHandler = new Handler(msg -> {
            if (msg.what == CameraControl.RecordHandler.MSG_STOP_RECORD) {
                //停止录制的消息
                log.info("收到录制停止的消息.");
                baseBinding.getTitle().video.set(false);
//                setState(RecordControl.STATE_PREVIEW);
//                rePreview();
            } else if (msg.what == CameraControl.RecordHandler.MSG_START_RECORD) {
                //开始录制的消息
                log.info("收到录制开始的消息.");
                baseBinding.getTitle().video.set(true);
            } else if (msg.what == CameraControl.RecordHandler.MSG_ERROR_RECORD) {
                //开始录制的消息
                log.info("收到录制错误的消息.");
                baseBinding.getTitle().video.set(false);
            } else if (msg.what == CameraControl.RecordHandler.MSG_TF_SPACE) {
                //T卡空间不足,停止录像的消息
                log.info("收到录制空间不足的消息.");
                if (!isRecordWarn) {
                    isRecordWarn = true;
                    VoiceManagerProxy.getInstance().startSpeaking("存储卡空间不足，已停止录像.", TTSType.TTS_DO_NOTHING, false);
                }
            } else if (msg.what == CameraControl.CameraHandler.MSG_REBOOT) {
                log.info("收到重启设备的消息.");

                CarStatusUtils.isFired().subscribe(aBoolean -> {
                    if (!aBoolean) {
                        com.dudu.android.hideapi.SystemPropertiesProxy.getInstance().set(BaseActivity.this, "persist.sys.boot", "reboot");
                    }
                });
            }
            return true;
        });*/

        volBrightnessSetting = new VolBrightnessSetting(this, baseBinding.baseView);

//        rearCameraPreview = baseBinding.rearCameraPreview;

//        initBlurPreview();//2016-4-28 模糊,切换

        SdcardManager.instance().addListener(this);//添加T卡监听

        String version = String.valueOf(VersionTools.getAppVersionCode(this));
        if (BuildConfig.DEBUG) {
            observableFactory.getTitleObservable().version.set(version);
        }

        mLogoImageView = findViewById(R.id.logoImageView);
        mLogoImageView.setOnLongClickListener(v -> {
            T.showL(BaseActivity.this, version + " " + VersionTools.getAppVersion(this));
            return true;
        });
        registerVolumeReceiver();
    }

    protected RotateAnimation getRotateAnimation() {
        if (mRotateAnimation == null) {
            mRotateAnimation = new RotateAnimation(mLogoImageView.getMeasuredWidth() / 2, mLogoImageView.getMeasuredHeight() / 2, RotateAnimation.ROTATE_DECREASE);
            mRotateAnimation.setDuration(1000);
            mRotateAnimation.setRepeatCount(RotateAnimation.INFINITE);
        }

        return mRotateAnimation;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        log.debug("onPostCreate");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        log.debug("onPostResume");
//        rePreview();
    }


    public void setBlur(boolean blur) {
//        if (isBlur != blur) {
//            log.info("setBlur {}", blur);
//            isBlur = blur;
//            rePreview();
//        }
    }


    protected abstract View getChildView();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        log.debug("onDestroy");
        ActivitiesManager.getInstance().removeActivity(this);
        if (volBrightnessSetting != null) {
            volBrightnessSetting.removeVoiceView();
        }

        SdcardManager.instance().removeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        log.debug("onResume");

        ActivitiesManager.getInstance().addActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        log.debug("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        log.debug("onStop");
    }

    @Override
    protected void onStart() {
        super.onStart();
        log.debug("onStart");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return volBrightnessSetting.getOnTouchEventReturnFlag(event);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String msg = extras.getString("msg");
            log.info("onNewIntent {}", msg);
        }
    }

    @Override
    public void onMounted() {
        LoggerFactory.getLogger("video.sdcard").info("sd卡插入");
        judgeToStartRecord();
    }

    private void judgeToStartRecord() {
        float sdFreeSpace = FileUtil.getSdFreeSpace();//剩余空间
        if (sdFreeSpace < 0.2) {//清理
            if (sdFreeSpace < 0.1) {
                VoiceManagerProxy.getInstance().startSpeaking("存储卡空间不足，将无法录像.", TTSType.TTS_DO_NOTHING, false);
//                CameraControl.instance().setRecordState(false);            } else {
                VoiceManagerProxy.getInstance().startSpeaking("存储卡空间不足,请清理.", TTSType.TTS_DO_NOTHING, false);
                FrontCameraManage.getInstance().stopRecord();
                RearCameraManage.getInstance().stopRecord();
            }

        } else {
            VoiceManagerProxy.getInstance().startSpeaking("内存卡植入成功，请确认是否已格式化内存.", TTSType.TTS_DO_NOTHING, false);

            FrontCameraManage.getInstance().startRecord();
            RearCameraManage.getInstance().startRecord();
        }
    }

    @Override
    public void onRemoved() {
        LoggerFactory.getLogger("video.sdcard").info("sd卡拔出");
        VoiceManagerProxy.getInstance().startSpeaking("您已移除存储卡，将会导致部分功能无法使用.", TTSType.TTS_DO_NOTHING, false);
        RearCameraManage.getInstance().stopRecord();
        FrontCameraManage.getInstance().stopRecord();
        isRecordWarn = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        log.debug("keyCode:{}, event:{}", keyCode, event);

        if (BuildConfig.DEBUG) {
            if (keyCode == KeyEvent.KEYCODE_8) {
//                EventBus.getDefault().post(new EventStartStream());
                FrontCameraManage.getInstance().startUploadVideoStream();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_9) {
//                EventBus.getDefault().post(new EventStopStream());
                FrontCameraManage.getInstance().stopUploadVideoStream();
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * 注册当音量发生变化时接收的广播
     */
    private void registerVolumeReceiver() {
        mVolumeReceiver = new VolumeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        registerReceiver(mVolumeReceiver, filter);
    }

    /**
     * 处理音量变化时的界面显示
     *
     * @author long
     */
    private class VolumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //如果音量发生变化则更改seekbar的位置
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                volBrightnessSetting.reflashVoiceBar();
            }
        }
    }

    public void showBrightnessView(int showBrightness) {
        volBrightnessSetting.showBrightnessView(showBrightness);
    }
}
