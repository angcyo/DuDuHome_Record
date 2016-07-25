package com.dudu.aios.ui.fragment;

import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dudu.aios.ui.fragment.base.RBaseFragment;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.ui.view.PointerView;
import com.dudu.android.launcher.utils.FileUtils;
import com.dudu.carChecking.VideoTextureView;
import com.dudu.commonlib.event.Events;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;
import com.dudu.workflow.common.ObservableFactory;
import com.dudu.workflow.obd.SpeedFlow;
import com.dudu.workflow.obd.VehicleConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/4/30.
 */
public class AccelerationTestFragment extends RBaseFragment implements View.OnClickListener {

    private RelativeLayout mVideoContainer;

    private VideoTextureView mVideoView;

    private PointerView pointerView;

    private TextView mSpeedText, mAccelerationText, mTimeConsuming;

    private ImageButton mBackButton;

    private Subscription speedSubscription;

    private boolean isStarting = false;

    private Logger logger = LoggerFactory.getLogger("AccelerationTestFragment");

    @Override
    protected int getContentView() {
        return R.layout.fragment_accelerate_test;
    }

    @Override
    protected void initViewData() {
        mBaseActivity.showTitle(false);
        iniView();
        iniListener();
    }

    private void iniListener() {
        mBackButton.setOnClickListener(this);
    }

    private void iniView() {
        mVideoContainer = (RelativeLayout) mViewHolder.v(R.id.video_container);
        pointerView = (PointerView) mViewHolder.v(R.id.pointView);
        mSpeedText = (TextView) mViewHolder.v(R.id.speed_per_hour);
        mAccelerationText = (TextView) mViewHolder.v(R.id.acceleration_time);
        mTimeConsuming = (TextView) mViewHolder.v(R.id.time_consuming);
        mBackButton = (ImageButton) mViewHolder.v(R.id.button_back);
    }

    private void initData() {
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
        mVideoView = new VideoTextureView(getActivity());
        mVideoContainer.addView(mVideoView);
        iniAnimData();
        startAccelerationAnim();
    }

    private void iniAnimData() {
        File file;
        file = new File(VehicleConstants.SYSTEM_ANIMATION_DIR + "/" + VehicleConstants.ACCELERATION, VehicleConstants.ACCELERATION_MP4);
        if (!file.exists()) {
            file = new File(FileUtils.getAnimDir() + "/" + VehicleConstants.ACCELERATION, VehicleConstants.ACCELERATION_MP4);
        }
        logger.debug("加速测试动画的目录:" + file.getPath() + (file.exists() ? "存在" : "不存在"));
        if (file.exists()) {
            mVideoView.setFilePath(file.getPath());
            mVideoView.setFps(25);
            mVideoView.setLoopMode(true);
        }
    }

    @Override
    public void onShow() {
        super.onShow();
        logger.debug("--onShow()");
        mBaseActivity.showTitle(false);
        initData();
    }

    private void showSpeed() {
        logger.debug("开始订阅实时速度的事件");
        try {
            speedSubscription = SpeedFlow.testCarSpeed().subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Double>() {
                        @Override
                        public void call(Double aDouble) {
                            logger.debug("收到实时速度的事件成功：" + aDouble);
                            if (isStarting) {
                                mSpeedText.setText("" + aDouble + " km/h");
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            logger.debug("收到实时速度的事件失败：" + throwable);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startAccelerationAnim() {
        Observable.timer(400, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                if (mVideoView != null) {
                    mVideoView.startPlay();
                    isStarting = true;
                }

            }
        }, throwable -> logger.error("startAccelerationAnim", throwable));

        Observable.timer(1, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                showSpeed();
            }
        }, throwable -> logger.error("startAccelerationAnim", throwable));

    }

    @Override
    public void onHide() {
        super.onHide();
        logger.debug("--onHide()");
        mBaseActivity.showTitle(true);
        stopAcceleration();
        pointerView.reset();
        mTimeConsuming.setText("");
        mSpeedText.setText("");
        mAccelerationText.setText("");
        speedSubscription.unsubscribe();
    }

    private void stopAcceleration() {
        isStarting = false;
        if (mVideoView != null) {
            mVideoView.stopPlay();
            mVideoView.setVisibility(View.GONE);
            mVideoView = null;
        }
        if (mVideoContainer.getChildCount() > 0) {
            mVideoContainer.removeAllViews();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        logger.debug("--onResume()");
        mBaseActivity.showTitle(false);
        initData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                mBaseActivity.showMain();
                ObservableFactory.stopAccelerationTestFlow();
                break;
        }
    }

    public void onEventMainThread(Events.TestSpeedEvent data) {
        logger.debug("收到消息：" + data.getEvent() + "  :" + data.getSpeedTotalTime() + "  speed:" + data.getSpeed());
        int testSpeedStatus = data.getEvent();

        switch (testSpeedStatus) {
            case Events.TEST_SPEED_START:
                if (mVideoView != null & !isStarting) {
                    mVideoView.startPlay();
                    isStarting = true;
                    mTimeConsuming.setText("");
                    mSpeedText.setText("");
                    mAccelerationText.setText("");
                    showSpeed();
                }
                break;
            case Events.TEST_SPEED_STOP:
                if (speedSubscription != null) {
                    speedSubscription.unsubscribe();
                }
                mTimeConsuming.setText("耗时");
                String consumeTime = data.getSpeedTotalTime();
                if (consumeTime.length() > 4) {
                    consumeTime = consumeTime.substring(0, 3);
                }
                consumeTime = new DecimalFormat("######0.00").format(Double.parseDouble(consumeTime));
                mAccelerationText.setText(consumeTime + "s");
                if (Float.parseFloat(data.getSpeed()) != 0f) {
                    mSpeedText.setText(data.getSpeed() + " km/h");
                }
                String maxSpeed = SharedPreferencesUtil.getStringValue(getActivity(), SharedPreferencesUtil.MAX_SPEED, "");
                mSpeedText.setText(maxSpeed + " km/h");
                if (mVideoView != null) {
                    mVideoView.stopPlay();
                    isStarting = false;
                }
                break;
        }


    }

}
