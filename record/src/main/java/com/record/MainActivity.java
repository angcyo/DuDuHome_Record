package com.record;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ToggleButton;

import com.blur.SoundPlayManager;
import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.drivevideo.rearcamera.RearCameraHandler;
import com.dudu.drivevideo.rearcamera.RearCameraManage;
import com.dudu.drivevideo.rearcamera.preview.RearCameraPreview;
import com.dudu.drivevideo.spaceguard.SpaceCheck;
import com.dudu.drivevideo.spaceguard.VideoStorageResource;
import com.dudu.drivevideo.utils.TimeUtils;
import com.dudu.drivevideo.utils.UsbControl;
import com.record.base.RBaseViewHolder;
import com.record.base.T;
import com.record.control.IRecord;
import com.record.control.MusicControl;
import com.record.control.PreviewControl;
import com.record.control.RecordControl;
import com.record.control.RecordImpl;
import com.record.control.WatermarkControl;
import com.record.event.RecordEvent;
import com.record.event.SdEvent;
import com.record.event.StopPreviewEvent;
import com.record.mode.MenuDialogMode;
import com.record.service.PhoneBroadcastReceiver;
import com.record.service.RecordService;
import com.record.util.Debug;
import com.record.util.RUtil;
import com.record.view.MenuDialogFragment;
import com.record.view.RippleBackground;
import com.record.view.SpaceTipWindow;
import com.record.view.TipDialogFragment;

import java.io.File;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity implements MenuDialogMode.OnMenuListener, PreviewControl.IPreviewListener {

    private static long takePhotoTime = 0l;
    RBaseViewHolder mViewHolder;
    MenuDialogFragment dialogFragment;
    TipDialogFragment mSdcardDialogFragment;
    TipDialogFragment mSpaceDialogFragment;
    TipDialogFragment mQuitTipDialogFragment;
    TipDialogFragment m8GCardTipDialogFragment;
    boolean sdDialogShow = false;
    boolean spaceDialogShow = false;
    IRecord mRecord = RecordImpl.instance();
    PreviewControl mPreviewControl;
    SurfaceHolder mSurfaceHolder;
    Runnable time = new Runnable() {
        @Override
        public void run() {
            mViewHolder.tV(R.id.timeView).setText(TimeUtils.format("HH:mm:ss"));
            mViewHolder.tV(R.id.timeView).postDelayed(time, 1000);
        }
    };
    boolean isPreviewFail = false;
    boolean isRecordFail = false;
    boolean isSendRecordOk = false;
    boolean isRecordAnimStart = false;
    boolean hasFocus = false;
    private Animation mRotateAnim;
    private boolean waitQuit = false;
    private long clickTime = 0l;

    public static String getDevs() {
        File file = new File("/dev");
        StringBuffer stringBuffer = new StringBuffer();

        if (file.exists()) {
            File[] files = file.listFiles();
            stringBuffer.append("video设备文件：");
            for (File file1 : files) {
                if (file1.getName().startsWith("video")) {
                    stringBuffer.append(file1.getName() + ",");
                }
            }
        }
        return stringBuffer.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewHolder = new RBaseViewHolder(findViewById(R.id.rootView));
        dialogFragment = new MenuDialogFragment();
        mSdcardDialogFragment = new TipDialogFragment();
        mSdcardDialogFragment.setContent("请插入SD卡...");
        mSpaceDialogFragment = new TipDialogFragment();
        mSpaceDialogFragment.setContent("存储卡空间不足,已停止录像.");
        mQuitTipDialogFragment = new TipDialogFragment();
        mQuitTipDialogFragment.setContent("正在退出...");
        m8GCardTipDialogFragment = new TipDialogFragment();
        m8GCardTipDialogFragment.setContent("请插入8G或以上容量SD卡...");

        SoundPlayManager.init(this.getApplicationContext(), R.raw.take_photo2);

//        startRipple();
        initMenuView();
        initTakePhotoView();
        initRecordView();
//        initTimeView();

        ((SurfaceView) mViewHolder.v(R.id.preView)).getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurfaceHolder = holder;
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mSurfaceHolder = holder;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        EventBus.getDefault().register(this);

        mPreviewControl = new PreviewControl(this);

        RecordControl.init(this);

//        RecordCheck.instance(this.getApplicationContext());

        mRecord.init();

        RecordService.start(this);//启动服务
    }

    private void initTimeView() {
        mViewHolder.tV(R.id.timeView).post(time);
    }

    public void onUsb(View view) {
        if (view.getTag() == null) {
            UsbControl.setToHost();
            view.setTag("--");
        } else {
            UsbControl.setToClient();
            view.setTag(null);
        }
    }

    public void onStartPreview(View view) {
//        mRecord.startPreview(null);
        RearCameraManage.getInstance().startPreviewHolder(mSurfaceHolder);
    }

    public void onInitCamera(View view) {
        RearCameraManage.getInstance().init();
    }

    public void onPrintVideo(View view) {
        RearCameraHandler.printDevVideos();

        Debug.show(this, getDevs());
    }

    public void onBack(View view) {
        onBackPressed();
//        RearCameraManage.getInstance().release();
    }

    private void initTakePhotoView() {
        mViewHolder.v(R.id.takePhotoView).setOnClickListener(v -> {
            RearCameraPreview.log.info("拍照事件.");

            if (!RUtil.canPreview()) {
                RearCameraPreview.log.info("拍照拒绝: 请连接设备.");

                T.show(this, "请连接设备.");
                return;
            }

            if (FileUtil.isTFlashCardExists()) {
                RearCameraPreview.log.info("拍照: T卡存在.");

                final long nowTime = System.currentTimeMillis();
                if ((nowTime - takePhotoTime) > 800) {
                    Debug.show(this, "声效:" + MusicControl.instance().isMusic());
                    RearCameraPreview.log.info("拍照事件:声效状态", MusicControl.instance().isMusic());

                    if (MusicControl.instance().isMusic()) {
                        SoundPlayManager.play();
                    }
                    mRecord.takePhoto();
//                    mViewHolder.v(R.id.takePhotoView).setVisibility(View.INVISIBLE);
//                    mViewHolder.v(R.id.takePhotoView).postDelayed(() -> mViewHolder.v(R.id.takePhotoView).setVisibility(View.VISIBLE), 1500);
                    takePhotoTime = nowTime;
                } else {
                    RearCameraPreview.log.info("拍照事件: 拍照速度过快.");
                }
            } else {
                T.show(this, "TF卡不存在.");
                RearCameraPreview.log.info("拍照事件:T卡不存在.");
            }
        });
    }

    private void initRecordView() {
        updateRecordView();
        mViewHolder.imgV(R.id.recorderView).setOnClickListener(v -> {

            final long nowTime = System.currentTimeMillis();
            if (nowTime - clickTime < 1500) {
                return;
            }
            clickTime = nowTime;

            WatermarkControl.initWatermark();//初始化水印

            Debug.show(this, getDevs());

            if (!RUtil.canRecord()) {
                T.show(this, "请连接设备.");
                return;
            }

            if (!FileUtil.isTFlashCardExists()) {
                T.show(this, "TF卡不存在.");
                return;
            }

            if (RecordControl.isRecord()) {
//                mRecord.stopRecord();
                Debug.show(this, "请求停止录像.");
                RecordControl.setRecordEnable(false);
                RecordControl.sendRecord(this, false);
                stopRecordAnim();
            } else {
//                mRecord.startRecord();
                Debug.show(this, "请求开始录像.");
                RecordControl.setRecordEnable(true);
                RecordControl.sendRecord(this, true);
            }

//            mViewHolder.v(R.id.recorderView).setVisibility(View.INVISIBLE);
            mViewHolder.v(R.id.recorderView).postDelayed(() -> {
//                mViewHolder.v(R.id.recorderView).setVisibility(View.VISIBLE);
                updateRecordView();
            }, 1500);
        });
    }

    private void updateRecordView() {
        if (RecordControl.isRecord()) {
            mViewHolder.imgV(R.id.recorderView).setImageResource(R.mipmap.v_stop_record);
            startRecordAnim();

            RecordService.sendRecordState(this, RecordService.START_RECORD);
        } else {
            mViewHolder.imgV(R.id.recorderView).setImageResource(R.mipmap.v_start_record);
            stopRecordAnim();

            RecordService.sendRecordState(this, RecordService.STOP_RECORD);
        }
    }

    private void startRecordAnim() {
        if (isRecordAnimStart) {
            return;
        }
        if (mRotateAnim == null) {
            mRotateAnim = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            mRotateAnim.setInterpolator(new LinearInterpolator());
            mRotateAnim.setRepeatCount(Animation.INFINITE);
            mRotateAnim.setDuration(4000);
        }

        mViewHolder.v(R.id.recorderAnimView).setVisibility(View.VISIBLE);
        mViewHolder.v(R.id.recorderAnimView).startAnimation(mRotateAnim);
        isRecordAnimStart = true;

        Debug.show(this, "开始录制动画.");
    }

    private void stopRecordAnim() {
        if (isRecordAnimStart) {
            mViewHolder.v(R.id.recorderAnimView).clearAnimation();
            mViewHolder.v(R.id.recorderAnimView).setVisibility(View.INVISIBLE);
            isRecordAnimStart = false;

            Debug.show(this, "停止录制动画.");
        }
    }

    private void initMenuView() {
        ((ToggleButton) mViewHolder.v(R.id.menuView)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                dialogFragment.show(getSupportFragmentManager(), "dialog");
            } else {
                dialogFragment.dismiss();
            }
        });
    }

    private void startRipple() {
        mViewHolder.v(R.id.rippleLayout).setVisibility(View.VISIBLE);
        ((RippleBackground) mViewHolder.v(R.id.rippleBackgroundLayout)).startRippleAnimation();
//        mViewHolder.postDelay(() -> {
//            ((RippleBackground) mViewHolder.v(R.id.rippleBackgroundLayout)).stopRippleAnimation();
//            mViewHolder.v(R.id.rippleLayout).setVisibility(View.GONE);
//        }, 2000);
    }

    private void stopRipple() {
        ((RippleBackground) mViewHolder.v(R.id.rippleBackgroundLayout)).stopRippleAnimation();
        mViewHolder.v(R.id.rippleLayout).setVisibility(View.GONE);
    }

    @Override
    public void onDismiss() {
        ((ToggleButton) mViewHolder.v(R.id.menuView)).setChecked(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Debug.show(this, "MainActivity onStop");
        mPreviewControl.setPreviewListener(null);
        mPreviewControl.exitCheck();

//        startRipple();
//        mRecord.stopPreview();
//        isPreviewFail = true;

//        onPreviewFail();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        mPreviewControl.setPreviewListener(this);
        mPreviewControl.checkPreview();

        if (!FileUtil.isTFlashCardExists()) {
            onSdNo();
        } else {
            check8GCard();
        }

//        RecordFailedHelper.showFailedDialog(this);
//        new RecordFailedWindow(this).setContent("录像异常中断，请重新连接车充恢复。").show();
    }

    /**
     * 检查空间容量是否大于8G
     */
    private void check8GCard() {
        if (SpaceCheck.isAbove8G()) {
            Debug.show(this, "8G以上容量,needClean=" + VideoStorageResource.needClean() + ",tooSmall=" + SpaceCheck.isRecordFolderTooSmall(), Color.RED);
            Debug.show(this, "录制文件夹大小:" + SpaceCheck.getRecordFolderSize() / 1024 / 1024 + "MB", Color.RED);
            hide8GCardTip();
            onSpaceRecord(SpaceCheck.canRecord());
        } else {
            onNot8GCard();
        }
    }

    private void hide8GCardTip() {
        if (m8GCardTipDialogFragment.isVisible()) {
            m8GCardTipDialogFragment.dismiss();
        }
    }

    private void onNot8GCard() {
        if (m8GCardTipDialogFragment.isVisible()) {
            return;
        }
        m8GCardTipDialogFragment.show(getSupportFragmentManager(), "m8GCardTipDialogFragment");
//        RecordControl.sendRecordNo(this, false);
        RecordService.sendRecordState(this, RecordService.STOP_RECORD);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mRecord.stopPreview();
        mPreviewControl.exit();
        stopRipple();
        stopRecordAnim();
//        RecordCheck.instance(this.getApplicationContext()).quit();
        EventBus.getDefault().unregister(this);

        mViewHolder.tV(R.id.timeView).removeCallbacks(time);
        Debug.show(this, "MainActivity onDestroy");
    }

    /**
     * 录像事件
     */
    public void onEventMainThread(RecordEvent event) {
        Debug.show(this, "onEventMainThread RecordEvent :" + event.isRecord());
        if (event.isRecord()) {
            mViewHolder.imgV(R.id.recorderView).setImageResource(R.mipmap.v_stop_record);
            startRecordAnim();
        } else {
            mViewHolder.imgV(R.id.recorderView).setImageResource(R.mipmap.v_start_record);
            stopRecordAnim();

            if (waitQuit) {
                Debug.show(this, "收到退出请求,即将退出...");
                RecordService.sendRecordState(this, RecordService.STOP_RECORD);
                RecordControl.setRecordEnable(false);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
                System.exit(1);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    }

    /**
     * SD卡事件
     */
    public void onEventMainThread(SdEvent event) {
        Debug.show(this, "SD事件..." + event.getState());

        if (event.getState() == SdEvent.STATE_NO) {
            onSdNo();
        } else if (event.getState() == SdEvent.STATE_OK) {
            onSdOk();
        }
    }

    public void onEventMainThread(StopPreviewEvent event) {
        PhoneBroadcastReceiver.log.info("收到 StopPreviewEvent 停止预览事件 事件");
        Debug.show(this, "停止预览事件");
        onPreviewFail();
    }

    /**
     * SD卡插入
     */
    private void onSdOk() {
        if (mSdcardDialogFragment.isVisible()) {
            mSdcardDialogFragment.dismiss();
            sdDialogShow = false;
        }
        check8GCard();
    }

    /**
     * SD卡移出
     */
    private void onSdNo() {
        if (!mSdcardDialogFragment.isVisible() && !sdDialogShow) {
            sdDialogShow = true;
            mSdcardDialogFragment.show(getSupportFragmentManager(), "mSdcardDialogFragment");
        }

        if (mSpaceDialogFragment.isVisible()) {
            mSpaceDialogFragment.dismiss();
            spaceDialogShow = false;
        }

        hide8GCardTip();

        mViewHolder.imgV(R.id.recorderView).setImageResource(R.mipmap.v_start_record);
        RecordControl.sendRecordNo(this, false);
    }

    @Override
    public void onCheckStart() {
//        Debug.show(this, getDevs());
    }

    @Override
    public void onPreviewOk() {
//        T.show(this, "record " + RecordControl.getSaveRecordState(this));
        Debug.show(this, "onPreviewOk...");

        stopRipple();
        if (!mRecord.isPreviewing()) {
            if (mSurfaceHolder == null) {
//            mViewHolder.itemView.postDelayed(() -> {
//                Debug.show(this, ("1秒后重试开始预览");
//                mRecord.startPreview(mSurfaceHolder);
//            }, 1000);
                mPreviewControl.checkPreview();
            } else {
                mRecord.startPreview(mSurfaceHolder);
            }
        }

        isPreviewFail = false;
    }

    @Override
    public void onPreviewFail() {
        startRipple();
        if (!isPreviewFail) {
//        H264Check.start();
            mRecord.stopPreview();
        }

        isPreviewFail = true;
    }

    @Override
    public void onRecordOk() {
        isRecordFail = false;
        if (!isSendRecordOk) {
            mViewHolder.v(R.id.recorderView).postDelayed(this::updateRecordView, 1500);
        }
        isSendRecordOk = true;
    }

    @Override
    public void onRecordFail() {
//        updateRecordView();

        if (!isRecordFail) {
            mViewHolder.imgV(R.id.recorderView).setImageResource(R.mipmap.v_start_record);
            stopRecordAnim();

            RecordService.sendRecordState(this, RecordService.STOP_RECORD);
        }
        isRecordFail = true;
        isSendRecordOk = false;
    }

    @Override
    public void onCheckEnd() {

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        moveTaskToBack(true);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.hasFocus = hasFocus;

        Debug.show(this, "hasFocus:" + hasFocus);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    private void onSpaceRecord(boolean record) {
        if (record) {
            /*空间满足录制*/
            Debug.show(this, hasFocus + "..空间满足.." + mSpaceDialogFragment.isAdded());
            if (mSpaceDialogFragment.isVisible()) {
                mSpaceDialogFragment.dismiss();
                spaceDialogShow = false;
            }
            if (RecordControl.getRecordEnable() && RUtil.canRecord()) {
                Debug.show(this, "空间满足..开始录像.");
                RecordControl.sendRecord(this, true);
            } else {
                Debug.show(this, "空间满足.." + RecordControl.getRecordEnable() + " " + RUtil.canRecord());
            }
        } else if (FileUtil.isTFlashCardExists()) {
            /*空间不满足录制*/
            Debug.show(this, hasFocus + "..空间不足.." + mSpaceDialogFragment.isAdded());
            Log.e("angcyo", "onSpaceRecord: " + hasFocus + "..空间不足.." + mSpaceDialogFragment.isVisible() + " " + mSpaceDialogFragment.isAdded());

            if (!mSpaceDialogFragment.isVisible() && !spaceDialogShow) {
                spaceDialogShow = true;
                mSpaceDialogFragment.show(getSupportFragmentManager(), "mSpaceDialogFragment");
            }
            RecordControl.sendRecordNo(this, false);

            SpaceTipWindow.showTip(this, "存储空间不足,已停止录像");
        }
    }

    /**
     * 等待视频停止后,再退出
     */
    public void waitQuit() {
        waitQuit = true;
        if (!mQuitTipDialogFragment.isVisible()) {
            mQuitTipDialogFragment.show(getSupportFragmentManager(), "mQuitTipDialogFragment");
        }
    }
}
