package com.dudu.voice.window;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.dudu.aios.ui.activity.MainRecordActivity;
import com.dudu.aios.ui.base.BaseFragmentManagerActivity;
import com.dudu.aios.ui.base.VolBrightnessSetting;
import com.dudu.aios.ui.utils.blur.RxBlurEffective;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.aios.ui.voice.VoiceEvent;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.model.WindowMessageEntity;
import com.dudu.android.launcher.ui.adapter.MessageAdapter;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.map.NavigationProxy;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.engine.SemanticEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import static com.dudu.aios.ui.voice.VoiceEvent.SHOW_ANIM;

/**
 * Created by 赵圣琪 on 2016/1/4.
 */
public class BlueWindowManager extends BaseWindowManager {


    private ListView mMessageListView;

    private MessageAdapter mMessageAdapter;

    private List<WindowMessageEntity> mMessageData;

    private Logger logger;

    private Button voiceBack;

    private LinearLayout voice_animLayout;

    private LinearLayout voice_animLayout_blur;

//    private VoiceCircleAnimView voiceCircleAnimView;

    private ImageView voiceCircleAnimView;

//    private VoiceRippleAnimView voiceRippleAnimView;

    private ImageView voiceRippleAnimView;

    private ImageView blur_Circle, blur_Ripple;

    private View message_layout;

    private boolean isInit = false;

    private VolBrightnessSetting volBrightnessSetting;

    private AnimationDrawable rippleAnimation;
    private RotateAnimation circleAnimation;

    @Override
    public void initWindow() {

        if (isInit)
            return;

        logger = LoggerFactory.getLogger("voice.float");


        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mLayoutParams.width = mContext.getResources().getDisplayMetrics().widthPixels;
        mLayoutParams.height = mContext.getResources().getDisplayMetrics().heightPixels;
        mLayoutParams.x = 0;
        mLayoutParams.y = 0;


        voiceBack = (Button) mFloatWindowView.findViewById(R.id.voiceBack);

        voiceBack.setOnClickListener(v -> {
            VoiceManagerProxy.getInstance().stopSpeaking();
            removeFloatWindow();
        });

        mMessageData = new ArrayList<>();
        mMessageListView = (ListView) mFloatWindowView.findViewById(R.id.message_listView);
        mMessageAdapter = new MessageAdapter(mContext, mMessageData);
        mMessageListView.setAdapter(mMessageAdapter);
        message_layout = mFloatWindowView.findViewById(R.id.message_layout);

        initAnimView();

        volBrightnessSetting = new VolBrightnessSetting(ActivitiesManager.getInstance().getTopActivity(), mFloatWindowView);

        isInit = true;
    }

    private void initAnimView() {
        voice_animLayout = (LinearLayout) mFloatWindowView.findViewById(R.id.voice_anim_layout);

        voiceRippleAnimView = (ImageView) mFloatWindowView.findViewById(R.id.voice_ripple);
        voiceCircleAnimView = (ImageView) mFloatWindowView.findViewById(R.id.voice_circle);

//        voiceRippleAnimView.setZOrderOnTop(true);
//        voiceRippleAnimView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        // 从XML配置文件中读取animatioln-list，初始化AnimationDrawable
        rippleAnimation = (AnimationDrawable) mContext.getResources().getDrawable(R.drawable.speeking_animation);
        // ImageView将AnimationDrawable设置为背景
        voiceRippleAnimView.setBackground(rippleAnimation);

//        voiceCircleAnimView.setZOrderOnTop(true);
//        voiceCircleAnimView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        circleAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        circleAnimation.setInterpolator(new LinearInterpolator());//不停顿
        circleAnimation.setRepeatMode(Animation.RESTART);
        circleAnimation.setRepeatCount(3000);
        circleAnimation.setFillAfter(true);//停在最后
        circleAnimation.setDuration(2000);
        //动画开始
        voiceCircleAnimView.startAnimation(circleAnimation);

        voice_animLayout_blur = (LinearLayout) mFloatWindowView.findViewById(R.id.voice_anim_blur_layout);
        blur_Circle = (ImageView) mFloatWindowView.findViewById(R.id.voice_circle_blur);
        blur_Ripple = (ImageView) mFloatWindowView.findViewById(R.id.voice_ripple_blur);

    }


    @Override
    public int getFloatWindowLayout() {
        return R.layout.speech_dialog_window_new;
    }

    @Override
    public void showMessage(WindowMessageEntity message) {

        if (NavigationProxy.getInstance().isShowList()) {
            return;
        }

        if (!(ActivitiesManager.getInstance().getTopActivity() instanceof MainRecordActivity)
                || LauncherApplication.getContext().isReceivingOrder()) {
            mFloatWindowView.setBackgroundResource(R.drawable.black_bg);
        }

        addFloatView();

        stopAnimWindow();


        blur(message_layout);

        message_layout.setVisibility(View.VISIBLE);

        mMessageListView.setVisibility(View.VISIBLE);

        mMessageAdapter.addMessage(message);

        mMessageListView.smoothScrollToPosition(mMessageData.size() - 1);

        mShowFloatWindow = true;

        EventBus.getDefault().post(VoiceEvent.SHOW_MESSAGE);
    }

    @Override
    public void showStrategy() {
    }

    @Override
    public void showAddress() {
    }

    @Override
    public void onVolumeChanged(int volume) {
    }

    @Override
    public void onNextPage() {
    }

    @Override
    public void onPreviousPage() {
    }

    @Override
    public void onChoosePage(int page) {
    }

    @Override
    public void removeFloatWindow() {

        logger.debug("removeFloatWindow");

        removeFloatView();

        isInit = false;

        stopAnimWindow();

        mMessageData.clear();

        EventBus.getDefault().post(VoiceEvent.DISMISS_WINDOW);

        Activity topActivity = ActivitiesManager.getInstance().getTopActivity();
        if (topActivity instanceof MainRecordActivity) {
            String currentStackTag = ((BaseFragmentManagerActivity) topActivity).getCurrentStackTag();
            if (FragmentConstants.CAR_CHECKING.equals(currentStackTag)
                    || FragmentConstants.REPAIR_FAULT_CODE_FRAGMENT.equals(currentStackTag)
                    || FragmentConstants.VEHICLE_ANIMATION_FRAGMENT.equals(currentStackTag)) {
                return;
            }
        }
        SemanticEngine.getProcessor().switchSemanticType(SceneType.HOME);

    }

    @Override
    public void setItemClickListener(AdapterView.OnItemClickListener listener) {
    }

    public void showAnimWindow() {


        stopAnimWindow();

        if (!(ActivitiesManager.getInstance().getTopActivity() instanceof MainRecordActivity)
                || LauncherApplication.getContext().isReceivingOrder()) {
            mFloatWindowView.setBackgroundResource(R.drawable.black_bg);
        }
        addFloatView();

        visibleAnimView();

        message_layout.setVisibility(View.GONE);

        voice_animLayout.setBackgroundColor(Color.TRANSPARENT);

//        voiceCircleAnimView.startAnim();
//        voiceRippleAnimView.startAnim();
        rippleAnimation.start();
        voiceCircleAnimView.startAnimation(circleAnimation);

        mShowFloatWindow = true;
    }

    public void stopAnimWindow() {

//        voiceCircleAnimView.stopAnim();
//        voiceRippleAnimView.stopAnim();
        rippleAnimation.stop();
        voiceCircleAnimView.clearAnimation();

        voice_animLayout.setVisibility(View.GONE);
        voiceCircleAnimView.setVisibility(View.GONE);
        voiceRippleAnimView.setVisibility(View.GONE);

    }


    public void removeWithBlur() {

        Observable.timer(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread()).subscribe(aLong -> {

            try {
                removeFloatWindow();
            } catch (Exception e2) {

            }
        }, throwable -> logger.error("removeWithBlur", throwable));

        if (message_layout == null) {
            return;
        }
        try {
            Bitmap currentBitmap;
            message_layout.setDrawingCacheEnabled(true);
            Bitmap drawingCache = message_layout.getDrawingCache();
            currentBitmap = Bitmap.createBitmap(drawingCache.getWidth(), drawingCache.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(currentBitmap);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setFlags(Paint.FILTER_BITMAP_FLAG);
            canvas.drawBitmap(drawingCache, 0, 0, paint);

            Bitmap blurBitmap_back = RxBlurEffective
                    .bestBlur(mContext, currentBitmap, 20, 0.1f)
                    .toBlocking()
                    .first();

            visibleAnimView();
            voice_animLayout.setBackground(new BitmapDrawable(mContext.getResources(), blurBitmap_back));

            message_layout.setVisibility(View.GONE);
            mMessageListView.setVisibility(View.GONE);
//            voiceCircleAnimView.startAnim();
//            voiceRippleAnimView.startAnim();
            rippleAnimation.start();
            voiceCircleAnimView.startAnimation(circleAnimation);
            mMessageData.clear();
        } catch (Exception e) {

        }

    }

    private void visibleAnimView() {
        voice_animLayout_blur.setVisibility(View.GONE);
        blur_Circle.setVisibility(View.GONE);
        blur_Circle.setVisibility(View.GONE);

        voice_animLayout.setVisibility(View.VISIBLE);
        voiceRippleAnimView.setVisibility(View.VISIBLE);
        voiceCircleAnimView.setVisibility(View.VISIBLE);
    }

    private void blur(View view) {


        if (ActivitiesManager.getInstance().getTopActivity() instanceof MainRecordActivity) {
            BaseFragmentManagerActivity baseFragmentManagerActivity = (BaseFragmentManagerActivity) ActivitiesManager.getInstance().getTopActivity();
            String currentStackTag = baseFragmentManagerActivity.getCurrentStackTag();
            if (!FragmentConstants.CAR_CHECKING.equals(currentStackTag)
                    && !FragmentConstants.REPAIR_FAULT_CODE_FRAGMENT.equals(currentStackTag)
                    && !FragmentConstants.VEHICLE_ANIMATION_FRAGMENT.equals(currentStackTag)) {
                EventBus.getDefault().post(SHOW_ANIM);

                voice_animLayout_blur.setVisibility(View.VISIBLE);

                Bitmap blurBitmap1 = RxBlurEffective
                        .bestBlur(mContext, BitmapFactory.decodeResource(mContext.getResources(), R.drawable.voice_circle_1), 3, 0)
                        .toBlocking()
                        .first();

                Bitmap blurBitmap2 = RxBlurEffective
                        .bestBlur(mContext, BitmapFactory.decodeResource(mContext.getResources(), R.drawable.d02_voice_00), 3, 0)
                        .toBlocking()
                        .first();


                blur_Ripple.setVisibility(View.VISIBLE);
                blur_Circle.setVisibility(View.VISIBLE);

                blur_Circle.setImageDrawable(new BitmapDrawable(mContext.getResources(), blurBitmap1));
                blur_Ripple.setImageDrawable(new BitmapDrawable(mContext.getResources(), blurBitmap2));
            }
        } else {
            voice_animLayout.setVisibility(View.GONE);
            voice_animLayout_blur.setVisibility(View.GONE);
            blur_Ripple.setVisibility(View.GONE);
            blur_Circle.setVisibility(View.GONE);
            view.setBackgroundResource(R.drawable.black_bg);
        }
    }
}
