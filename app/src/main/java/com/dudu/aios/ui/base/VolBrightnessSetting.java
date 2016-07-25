package com.dudu.aios.ui.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.BtPhoneUtils;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.android.launcher.utils.DensityUtil;
import com.dudu.voip.VoipSDKCoreHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dengjun on 2016/1/23.
 * Description :
 */
public class VolBrightnessSetting implements GestureDetector.OnGestureListener, View.OnTouchListener {

    private Logger logger = LoggerFactory.getLogger("volume");
    private static final int GESTURE_MODIFY_VOLUME = 1;
    private static final int GESTURE_MODIFY_BRIGHTNESS = 2;
    private static final float STEP_VOLUME = 2f;// 协调音量滑动时的步长，避免每次滑动都改变，导致改变过快
    private static final float STEP_BRIGHTNESS = 2f;// 协调亮度滑动时的步长，避免每次滑动都改变，导致改变过快
    public static int staticBrightness = 255;
    int width;
    private Activity activity;
    private int GESTURE_FLAG = 0;// 1，调节音量 2，调节亮度
    private AudioManager audiomanager;
    private int maxVolume, currentVolume;
    private GestureDetector gestureDetector;
    private int maxBrightness, currentBrightness;
    private WindowManager windowManager;

    private View mVoiceView;

    private View mBrightnessView;

    private SeekBar mVoiceSeekBar;

    private SeekBar mBrightnessSeekBar;

    private WindowManager.LayoutParams mParams;// 窗口的属性

    private Handler voiceViewDisappearHandler = new VoiceViewDisappearHandler();

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new MySeekBarChangeListener();

    private boolean isVoiceViewAppear = false;

    private boolean isBrightViewAppear = false;

    /**
     * 设置当前屏幕亮度值 0--255
     */
    public static void saveScreenBrightness(Context context, int paramInt) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, paramInt);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    private void setParams() {
        mParams = new WindowManager.LayoutParams();
        mParams.type = WindowManager.LayoutParams.TYPE_PHONE;// 系统提示window
        // mParams.format = PixelFormat.TRANSLUCENT;// 支持透明
        mParams.format = PixelFormat.RGBA_8888;
        mParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        ;// 焦点
        mParams.width = 710;//窗口的宽和高
        mParams.height = 114;
        //默认在屏幕的中间
        mParams.x = 0;
        mParams.y = 0;
        mParams.alpha = 1.0f;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        switch (GESTURE_FLAG) {  // 如果每次触摸屏幕后第一次scroll是调节音量，那之后的scroll事件都处理音量调节，直到离开屏幕执行下一次操作
            case GESTURE_MODIFY_VOLUME://设置音量
                setVolume(distanceX, distanceY);
                break;
            case GESTURE_MODIFY_BRIGHTNESS: //设置亮度
                setBrightness(distanceX, distanceY);
                break;
        }
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float touchX = event.getX();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (touchX > (float) width / 2) {
                GESTURE_FLAG = GESTURE_MODIFY_VOLUME;
            } else {
                GESTURE_FLAG = GESTURE_MODIFY_BRIGHTNESS;
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            GESTURE_FLAG = 0;// 手指离开屏幕后，重置调节音量或进度的标志

        }
        return gestureDetector.onTouchEvent(event);
    }

    private void setVolume(float distanceX, float distanceY) {

        getCurrentVolumeAndMaxVolume();
        if (Math.abs(distanceY) > Math.abs(distanceX)) {
            if ((distanceY >= DensityUtil.dip2px(activity, STEP_VOLUME)) && (currentVolume < maxVolume)) {// 音量调大   纵向移动大于横向移动
                currentVolume++;  // 音量调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正；为避免调节过快，distanceY应大于一个设定值
            } else if ((distanceY <= -DensityUtil.dip2px(activity, STEP_VOLUME)) && (currentVolume > 0)) {
                currentVolume--;// 音量调小
            }
            logger.debug("currentVolume =" + currentVolume + "  maxVolume = " + maxVolume);

            if (BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_ACTIVE ||
                    BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_INCOMING ||
                    BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_DIALING) {
                audiomanager.setStreamVolume(Constants.STREAM_BLUETOOTH_SCO/*AudioManager.STREAM_BLUETOOTH_SCO*/, currentVolume, 0);

            } else if (VoipSDKCoreHelper.getInstance().eccall_state == VoipSDKCoreHelper.ERROR_ECCALL_ANSWERED) {
                audiomanager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currentVolume, 0);
            } else {
                audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
            }
            if (mBrightnessView.getParent() != null) {
                windowManager.removeView(mBrightnessView);
                isBrightViewAppear = false;
            }
            if (!isVoiceViewAppear) {
                windowManager.addView(mVoiceView, mParams);
                isVoiceViewAppear = true;
            }
            mVoiceSeekBar.setProgress(Math.round(((currentVolume) * 100 / maxVolume)));
            sendVoiceViewDisappearMessage();
        }
    }

    public void reflashVoiceBar() {
        getCurrentVolumeAndMaxVolume();
        if (mBrightnessView.getParent() != null) {
            windowManager.removeView(mBrightnessView);
            isBrightViewAppear = false;
        }
        if (!isVoiceViewAppear) {
            windowManager.addView(mVoiceView, mParams);
            isVoiceViewAppear = true;
        }

        mVoiceSeekBar.setProgress(Math.round(((currentVolume) * 100 / maxVolume)));
        sendVoiceViewDisappearMessage();
    }

    private void getCurrentVolumeAndMaxVolume() {
        //根据当前的通话类型获取不同类型的当前音量值
        if (BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_ACTIVE ||
                BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_INCOMING ||
                BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_DIALING) {
            maxVolume = audiomanager.getStreamMaxVolume(Constants.STREAM_BLUETOOTH_SCO/*AudioManager.STREAM_BLUETOOTH_SCO*/); // 获取系统最大音量
            currentVolume = audiomanager.getStreamVolume(Constants.STREAM_BLUETOOTH_SCO/*AudioManager.STREAM_BLUETOOTH_SCO*/); // 获取当前值
            logger.debug("onScroll setVolume AudioManager.STREAM_BLUETOOTH_SCO currentVolume:" + currentVolume + ",max:" + maxVolume);
        } else if (VoipSDKCoreHelper.getInstance().eccall_state == VoipSDKCoreHelper.ERROR_ECCALL_ANSWERED) {
            maxVolume = audiomanager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL); // 获取系统最大音量
            currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_VOICE_CALL); // 获取当前值

            logger.debug("onScroll setVolume AudioManager.STREAM_VOICE_CALL currentVolume:" + currentVolume + ",max:" + maxVolume);
        } else {
            maxVolume = audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量
            currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值

            logger.debug("onScroll setVolume AudioManager.STREAM_MUSIC currentVolume:" + currentVolume + ",max:" + maxVolume);
        }
    }

    private void setBrightness(float distanceX, float distanceY) {
        currentBrightness = Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);//取得当前亮度
        if (Math.abs(distanceY) > Math.abs(distanceX)) {// 亮度调大
            if ((distanceY >= DensityUtil.dip2px(activity, STEP_BRIGHTNESS)) && (currentBrightness < maxBrightness)) {// 纵向移动大于横向移动
                currentBrightness = currentBrightness + 17;// 亮度调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正 // 为避免调节过快，distanceY应大于一个设定值
                if (currentBrightness > maxBrightness) {
                    currentBrightness = maxBrightness;
                }
            } else if (distanceY <= -DensityUtil.dip2px(activity, STEP_BRIGHTNESS)) {// 亮度调小
                if (currentBrightness >= 17) {
                    currentBrightness = currentBrightness - 17;
                    if (currentBrightness < 17) {
                        currentBrightness = 0;
                    }
                }
            }
            synchroBrightness(currentBrightness);
            if (mVoiceView.getParent() != null) {
                windowManager.removeView(mVoiceView);
                isVoiceViewAppear = false;
            }
            if (!isBrightViewAppear) {
                windowManager.addView(mBrightnessView, mParams);
                isBrightViewAppear = true;
            }
            mBrightnessSeekBar.setProgress(Math.round(((currentBrightness) * 100 / maxBrightness)));
            sendVoiceViewDisappearMessage();
        }
    }

    public void synchroBrightness(int brightness) {
        setScreenMode(activity, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
        currentBrightness = Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);

//        Log.i("lcc", "brightness =" + brightness+"  currentBrightness = "+ currentBrightness);

        staticBrightness = currentBrightness;

        WindowManager.LayoutParams wl = activity.getWindow().getAttributes();
        float tmpFloat = (float) brightness / 255;
        if (tmpFloat > 0 && tmpFloat <= 1) {
            wl.screenBrightness = tmpFloat;
        }
        activity.getWindow().setAttributes(wl);
    }

    public boolean getOnTouchEventReturnFlag(MotionEvent motionEvent) {
        return gestureDetector.onTouchEvent(motionEvent);
    }

    public void removeVoiceView() {
        if (windowManager != null) {
            if (isVoiceViewAppear) {
                if (mVoiceView.getParent() != null) {
                    windowManager.removeView(mVoiceView);
                }
                voiceViewDisappearHandler.removeCallbacksAndMessages(null);
            }
            if (isBrightViewAppear) {
                if (mBrightnessView.getParent() != null) {
                    windowManager.removeView(mBrightnessView);
                }
                voiceViewDisappearHandler.removeCallbacksAndMessages(null);
            }
        }
    }

    private void sendVoiceViewDisappearMessage() {
        voiceViewDisappearHandler.removeCallbacksAndMessages(null);
        voiceViewDisappearHandler.sendEmptyMessageDelayed(0, 2000);
    }

    private void enlargeSeekBar(SeekBar mSeekBar) {

        mSeekBar.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Rect seekRect = new Rect();
                mSeekBar.getHitRect(seekRect);

                if ((event.getY() >= (seekRect.top - 50)) && (event.getY() <= (seekRect.bottom + 50))) {

                    float y = seekRect.top + seekRect.height() / 2;
                    //seekBar only accept relative x
                    float x = event.getX() - seekRect.left;
                    if (x < 0) {
                        x = 0;
                    } else if (x > seekRect.width()) {
                        x = seekRect.width();
                    }
                    MotionEvent me = MotionEvent.obtain(event.getDownTime(), event.getEventTime(),
                            event.getAction(), x, y, event.getMetaState());
                    mSeekBar.onTouchEvent(me);

                }
                return false;

            }

        });
    }

    private class VoiceViewDisappearHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (windowManager != null) {
                if (mVoiceView.getParent() != null) {
                    windowManager.removeView(mVoiceView);
                    isVoiceViewAppear = false;
                }
                if (mBrightnessView.getParent() != null) {
                    windowManager.removeView(mBrightnessView);
                    isBrightViewAppear = false;
                }
            }
        }
    }

    private class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        private float currentProgress;

        // 停止拖动时执行
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub


            switch (GESTURE_FLAG) {
                case GESTURE_MODIFY_VOLUME://设置声音
                    accordingProgressUpdateVoice(currentProgress);
                    break;
                case GESTURE_MODIFY_BRIGHTNESS: //设置亮度
                    accordingProgressUpdateBright(currentProgress);
                    break;
            }

            voiceViewDisappearHandler.sendEmptyMessageDelayed(0, 2000);
        }

        // 在进度开始改变时执行
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub
            voiceViewDisappearHandler.removeCallbacksAndMessages(null);
        }

        // 当进度发生改变时执行
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {

            float pro = seekBar.getProgress();

            float num = seekBar.getMax();

            float result = (pro / num) * 100;

            currentProgress = result;

        }
    }

    private void accordingProgressUpdateBright(float currentProgress) {
        synchroBrightness(Math.round((currentProgress) * maxBrightness / 100));
    }

    private void accordingProgressUpdateVoice(float currentProgress) {
        if (BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_ACTIVE ||
                BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_INCOMING ||
                BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_DIALING) {
            //如果蓝牙电话正在通话中
                /*STREAM_VOICE_CALL-- 通话，
                STREAM_SYSTEM -- 系统音量,
                STREAM_RING -- 铃声
                STREAM_MUSIC -- 音乐,
                STREAM_ALARM -- 闹钟 ,
                STREAM_NOTIFICATION -- 通知,
                STREAM_BLUETOOTH_SCO -- 蓝牙通话*/
            maxVolume = audiomanager.getStreamMaxVolume(Constants.STREAM_BLUETOOTH_SCO); // 获取系统最大音量
            logger.trace("AudioManager.STREAM_BLUETOOTH_SCO currentProgress:" + Math.round(((currentProgress) * maxVolume / 100)));
            audiomanager.setStreamVolume(6/*AudioManager.STREAM_BLUETOOTH_SCO*/,
                    Math.round(((currentProgress) * maxVolume / 100)), 0);
        } else if (VoipSDKCoreHelper.getInstance().eccall_state == VoipSDKCoreHelper.ERROR_ECCALL_ANSWERED) {
            maxVolume = audiomanager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL); // 获取系统最大音量
            logger.trace("AudioManager.STREAM_VOICE_CALL currentProgress:" + Math.round(((currentProgress) * maxVolume / 100)));
            //如果网络电话正在通话中
            audiomanager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    Math.round(((currentProgress) * maxVolume / 100)), 0);
        } else {
            maxVolume = audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量
            logger.trace("AudioManager.STREAM_MUSIC currentProgress:" + Math.round(((currentProgress) * maxVolume / 100)));
            audiomanager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    Math.round(((currentProgress) * maxVolume / 100)), 0);
        }
    }

    public VolBrightnessSetting(Activity activity, View view) {

        windowManager = (WindowManager) LauncherApplication.getContext().getSystemService(Context.WINDOW_SERVICE);

        mVoiceView = LayoutInflater.from(activity).inflate(R.layout.voice_adjust_layout, null);

        mVoiceSeekBar = (SeekBar) mVoiceView.findViewById(R.id.voice_seekBar);

        mVoiceSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        enlargeSeekBar(mVoiceSeekBar);

        mBrightnessView = LayoutInflater.from(activity).inflate(R.layout.brightness_adjust_layout, null);

        mBrightnessSeekBar = (SeekBar) mBrightnessView.findViewById(R.id.brightness_seekBar);

        mBrightnessSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        enlargeSeekBar(mBrightnessSeekBar);

        setParams();

        this.activity = activity;

        audiomanager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量
        currentVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值

        gestureDetector = new GestureDetector(activity, this);
        gestureDetector.setIsLongpressEnabled(true);
        view.setOnTouchListener(this);

        maxBrightness = 255;
        currentBrightness = Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);//取得当前亮度

        if (currentBrightness < 80) {
            currentBrightness = 80;
        }


        WindowManager wm = activity.getWindowManager();
        width = wm.getDefaultDisplay().getWidth();


//        Log.i("lcc", "staticBrightness =" + staticBrightness);
//        synchroBrightness(staticBrightness);
    }

    /**
     * 设置当前屏幕亮度的模式
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC=1 为自动调节屏幕亮度
     * SCREEN_BRIGHTNESS_MODE_MANUAL=0 为手动调节屏幕亮度
     */
    public static void setScreenMode(Context context, int paramInt) {
        try {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, paramInt);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    public void showBrightnessView(int showBrightness) {
        if (mVoiceView.getParent() != null) {
            windowManager.removeView(mVoiceView);
            isVoiceViewAppear = false;
        }
        if (!isBrightViewAppear) {
            windowManager.addView(mBrightnessView, mParams);
            isBrightViewAppear = true;
        }
        mBrightnessSeekBar.setProgress(Math.round(((showBrightness) * 100 / maxBrightness)));
        sendVoiceViewDisappearMessage();
    }

}
