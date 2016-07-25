package com.blur;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.dudu.drivevideo.camera.PictureObtain;
import com.dudu.drivevideo.config.FrontVideoConfigParam;
import com.dudu.drivevideo.utils.FileUtil;
import com.dudu.drivevideo.video.VideoSaveTools;
import com.dudu.persistence.factory.RealmCallFactory;
import com.dudu.persistence.realm.RealmCallBack;
import com.dudu.persistence.realmmodel.picture.PictureEntity;
import com.dudu.persistence.realmmodel.picture.PictureEntityRealm;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wysaid.camera.CameraInstance;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

import ch.qos.logback.core.android.SystemPropertiesProxy;

/**
 * Created by robi on 2016-05-18 11:04.
 */
public class CameraControl {

    /**
     * 不开启预览
     */
    public static final int STATE_NO = -1;
    /**
     * 模糊预览
     */
    public static final int STATE_BLUR = 1;
    /**
     * 清晰预览
     */
    public static final int STATE_PREVIEW = 2;

    /**
     * 释放摄像头资源，并且停止录制
     */
    public static final int STATE_RELEASE = 3;

    public static Logger log = LoggerFactory.getLogger("video.frontdrivevideo");
    static CameraControl mRecordControl;
    private BlurGLSurfaceView mBlurSurfaceTexture;//模糊预览View

    private CameraHandler mCameraHandler;
    private HandlerThread mCameraThread;
    private RecordHandler mRecordHandler;
    private HandlerThread mRecordThread;

    private WeakReference<Handler> mMainHandler;//主线程的Handler

    /**
     * 是否开启录制, 默认开启
     */
    private volatile boolean recordState = true;
    /**
     * 是否模糊的标识
     */
    private volatile boolean blurState = false;

    /**
     * 是否推流的标识
     */
    private volatile boolean streamState = false;

    private
    @RecordState
    int mCurState = STATE_NO;

    public CameraControl() {
        mCameraThread = new HandlerThread("mCameraThread");
        mRecordThread = new HandlerThread("mRecordThread");
        mCameraThread.start();
        mRecordThread.start();
        mCameraHandler = new CameraHandler(mCameraThread.getLooper());
        mRecordHandler = new RecordHandler(mRecordThread.getLooper());

        mCameraHandler.sendEmptyMessage(CameraHandler.CAMERA_OPEN);//打开摄像头
    }

    public static CameraControl instance() {
        if (mRecordControl == null) {
            synchronized (CameraControl.class) {
                if (mRecordControl == null) {
                    mRecordControl = new CameraControl();
                }
            }
        }
        return mRecordControl;
    }

    public static synchronized void exit() {
        if (mRecordControl != null) {

            if (mRecordControl.mRecordHandler.isRecordStart()) {
                mRecordControl.mRecordHandler.sendEmptyMessage(RecordHandler.RECORD_RELEASE_CAMERA);
            } else {
                mRecordControl.mRecordHandler.sendEmptyMessage(RecordHandler.RECORD_RELEASE);
                mRecordControl.mCameraHandler.sendEmptyMessage(CameraHandler.CAMERA_RELEASE);
            }

            mRecordControl.mRecordThread.quit();
            try {
                mRecordControl.mRecordThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mRecordControl.mCameraThread.quit();
            try {
                mRecordControl.mCameraThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mRecordControl = null;
        }
    }

    public static boolean savePictureAction(byte[] pictureData) {
        String picturePath = getPhotoFileName();
        try {
            FileOutputStream fos = new FileOutputStream(new File(picturePath));
            fos.write(pictureData);
            fos.flush();
            fos.close();
//            e("图片保存至:" + picturePath);

            RealmCallFactory.savePictureInfo(true, picturePath, new RealmCallBack<PictureEntityRealm, Exception>() {
                @Override
                public void onRealm(PictureEntityRealm result) {
                    log.debug("保存的照片信息：{}", new Gson().toJson(new PictureEntity(result)));
                }

                @Override
                public void onError(Exception error) {

                }
            });
            return true;
        } catch (Exception e) {
            log.error("保存图片失败:{}", e.getMessage());
        }

        return false;
    }

    public static String getPhotoFileName() {
//        return getFileName(".png");
        return PictureObtain.generatePicturePath();
    }

    public static boolean isMultiMic() {
        boolean ret = false;
        String mic = SystemPropertiesProxy.getInstance().get("persist.sys.mic.multi", "0");

        if (TextUtils.equals(mic, "0")) {
            ret = false;
        } else if (TextUtils.equals(mic, "1")) {
            ret = true;
        }

        return ret;
    }

    public static boolean isStream() {
        if (mRecordControl == null) {
            return false;
        } else {
            return mRecordControl.streamState;
        }
    }

    public static boolean isRecorder() {
        if (mRecordControl == null) {
            return false;
        } else {
            return mRecordControl.recordState;
        }
    }

    public synchronized void setMainHandler(Handler mainHandler) {
        mMainHandler = new WeakReference<>(mainHandler);
    }

    public synchronized void setBlurSurfaceTexture(BlurGLSurfaceView blurSurfaceTexture) {
        mBlurSurfaceTexture = blurSurfaceTexture;
    }

    /**
     * 设置预览状态<br/>
     * 模糊预览，可能会开启录制，取决于{@link #recordState}<br/>
     * 清晰预览，可能会开启录制，取决于{@link #recordState}<br/>
     * 取消预览，并且会停止录制<br/>
     *
     * @param state 决定预览的状态
     * @see com.blur.CameraControl#STATE_BLUR
     * @see com.blur.CameraControl#STATE_PREVIEW
     * @see com.blur.CameraControl#STATE_NO
     */
    public synchronized void setState(@RecordState int state) {
        log.info("重新设置状态: 当前:{} 新:{}", mCurState, state);
        if (mCurState != state) {
            if (state == STATE_RELEASE) {
                log.info("不保存状态: 释放资源 {}", state);
                if (mRecordHandler.isRecordStart()) {
                    mRecordHandler.sendEmptyMessage(RecordHandler.RECORD_RELEASE_CAMERA);
                } else {
                    mRecordHandler.sendEmptyMessage(RecordHandler.RECORD_RELEASE);
                    mCameraHandler.sendEmptyMessage(CameraHandler.CAMERA_RELEASE);
                }
            } else {
                mCurState = state;
                updateState();
            }
        }
    }

    /**
     * 设置录制的标识，在预览开启的情况下，才会开启录像。 如果预览没有开启，则只是标志了一下
     *
     * @param record 录制标志
     */
    public synchronized void setRecordState(boolean record) {
        recordState = record;
        recordControl();
    }

    /**
     * 不改变状态的情况下,改变录制
     */
    public synchronized void setRecordNoState(boolean record) {
        if (record) {
            mRecordHandler.sendEmptyMessage(RecordHandler.RECORD_START);
        } else {
            mRecordHandler.sendEmptyMessage(RecordHandler.RECORD_STOP);
        }
    }

    public synchronized void setBlurState(boolean blur) {
        blurState = blur;
        blurControl();
    }

    public synchronized void setStreamState(boolean stream) {
        if (streamState != stream) {
            streamState = stream;
            if (streamState) {
                mCameraHandler.sendEmptyMessage(CameraHandler.CAMERA_STREAM);
            } else {
                mCameraHandler.sendEmptyMessage(CameraHandler.CAMERA_STREAM_STOP);
            }
        }
    }

    /**
     * 拍照的入口
     */
    public synchronized void takePhoto() {
        if (isStream()) {
            log.error("正在推流状态, 不允许拍照!");
        } else {
            mCameraHandler.sendEmptyMessage(CameraHandler.CAMERA_TAKE_PICTURE);
        }
    }

    public void recordControl() {
        //在没有预览的情况下, 不开启录像
        if (recordState && mCurState != STATE_NO && mCurState != STATE_RELEASE) {
            if (isStream()) {
                log.error("正在推流状态, 不允许录制!");
            } else {
                log.info("发送开始录制的消息...");
                mRecordHandler.sendEmptyMessage(RecordHandler.RECORD_START);
            }
        } else {
            log.info("发送停止录制的消息...");

            mRecordHandler.sendEmptyMessage(RecordHandler.RECORD_STOP);
        }
    }

    private void blurControl() {
        if (mBlurSurfaceTexture != null) {
            mBlurSurfaceTexture.setBlur(blurState);
        }
    }

    private void sendMsgToMain(int what) {
        if (mMainHandler != null) {
            Handler handler = mMainHandler.get();
            if (handler != null) {
                handler.sendEmptyMessage(what);
            }
        }
    }

    /**
     * 更新至最后一次的状态
     */
    public void updateState() {
        if (mBlurSurfaceTexture == null) {
            log.error("未设置模糊 mBlurSurfaceTexture");
            return;
        }
        if (mCurState == STATE_BLUR) {
            log.info("准备预览至状态: 模糊 {}", mCurState);
            setBlurState(true);
            mCameraHandler.sendEmptyMessage(CameraHandler.CAMERA_RESTART_PREVIEW);
        } else if (mCurState == STATE_PREVIEW) {
            log.info("准备预览至状态: 清晰 {}", mCurState);
            setBlurState(false);
            mCameraHandler.sendEmptyMessage(CameraHandler.CAMERA_RESTART_PREVIEW);
        } else if (mCurState == STATE_NO) {
            log.info("准备预览至状态: 无预览 {}", mCurState);
            //停止预览
            if (mRecordHandler.isRecordStart()) {
                mRecordHandler.sendEmptyMessage(RecordHandler.RECORD_STOP_PREVIEW);//下次恢复预览的时候,可以根据当前录像状态,恢复录像
            } else {
                mCameraHandler.sendEmptyMessage(CameraHandler.CAMERA_RESTART_PREVIEW);
            }
        }
    }

    /**
     * 获取当前的状态
     */
    @RecordState
    public int getCurState() {
        return mCurState;
    }

    /**
     * 获取当前正在录制的文件路径
     */
    public String getCurRecordFile() {
        return mRecordHandler.getCurFilePath();
    }

    @IntDef({STATE_BLUR, STATE_PREVIEW, STATE_NO, STATE_RELEASE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RecordState {
    }

    /**
     * 控制Camera的线程
     */
    public class CameraHandler extends Handler implements Camera.PictureCallback {
        public static final int CAMERA_OPEN = 1;
        public static final int CAMERA_RESTART_PREVIEW = 2;
        public static final int CAMERA_STOP_PREVIEW = 3;
        public static final int CAMERA_RELEASE = 5;
        public static final int CAMERA_TAKE_PICTURE = 6;
        public static final int MSG_CHECK_TAKE_END = 7;
        public static final int CAMERA_STREAM = 8;
        public static final int CAMERA_STREAM_STOP = 9;

        public static final int MSG_REBOOT = 30;//发送至主线程的重启设备消息


        public static final long CHECK_TAKE_DELAY = 2000;//2秒后,检查拍照是否结束,重置takePictureCount(拍照次数)
        private static final long DELAY_OPEN_TIME = 100;
        public CameraInstance mCameraInstance;
        AtomicInteger takePictureCount = new AtomicInteger(0);
        boolean isFirstOnFrame = false;
        long frameCount = 0;//计算传输帧率
        long lastTime = 0;//计算帧率的时间
        RESControl mRESControl;
        private boolean isFirst = true;//第一次开始预览的时候,先停止预览,保证预览的成功率
        private boolean takeEnd = true;//拍照是否结束,在快速切换的时候,有可能接收不到拍照的回调.
        private int openCount = 0;

        public CameraHandler(Looper looper) {
            super(looper);
            mCameraInstance = CameraInstance.getInstance();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CAMERA_OPEN:
                    openCamera();
                    break;
                case CAMERA_RESTART_PREVIEW:
                    restartPreview();
                    break;
                case CAMERA_STOP_PREVIEW:
                    if (msg.obj != null) {
                        stopPreview((Boolean) msg.obj);
                    } else {
                        stopPreview(false);
                    }
                    break;
                case CAMERA_RELEASE:
                    releaseCamera();
                    break;
                case CAMERA_TAKE_PICTURE:
                    log.info("请求拍照的消息...");
                    takePictureCount(true);
                    break;
                case MSG_CHECK_TAKE_END:
                    if (!takeEnd) {
                        takePictureCount.set(0);
                    }
                    break;
                case CAMERA_STREAM:
                    startStream();
                    break;
                case CAMERA_STREAM_STOP:
                    stopStream();
                    break;
                default:
                    break;
            }
        }

        private void startStream() {
            if (mCameraInstance.getCameraDevice() != null) {
                if (mRESControl == null) {
                    mRESControl = new RESControl();
                    mRESControl.init(mCameraInstance.getCameraDevice());
                    mRESControl.start();
                }

                if (mRecordHandler.isRecordStart()) {
                    log.debug("停止录像之后, 准备开始推流.");
//                    streamCallback();
//                    mRecordHandler.sendMessage(mRecordHandler.obtainMessage(RecordHandler.RECORD_STOP_PREVIEW, new Boolean(true)));//需要停止录像, 否则拿不到回调数据
                } else {
                    log.debug("准备开始推流.");
//                    streamCallback();
                }
            }
        }

        private void streamCallback() {
            if (mCameraInstance.getCameraDevice() != null) {
                mCameraInstance.getCameraDevice().addCallbackBuffer(new byte[1920 * 1080 * 3 / 2]);
                mCameraInstance.getCameraDevice().addCallbackBuffer(new byte[1920 * 1080 * 3 / 2]);
                mCameraInstance.getCameraDevice().setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        long nowTime = System.currentTimeMillis();
                        frameCount++;
                        if (lastTime == 0) {
                            lastTime = nowTime;
                        } else {
                            if (nowTime - lastTime > 1000) {
                                log.info("帧率:{}/s", frameCount);
                                frameCount = 0;
                                lastTime = nowTime;
                            }
                        }

                        if (!isFirstOnFrame) {
                            log.info("onPreviewFrame {}", data.length);
                            isFirstOnFrame = true;
                        }
//                        mRESControl.queueVideo(data);
                        camera.addCallbackBuffer(data);
                    }
                });
            }
        }


        private void stopStream() {
            if (mRESControl != null) {
                mRESControl.stop();
                mRESControl = null;
            }

//            if (mCameraInstance.getCameraDevice() != null) {
//                log.debug("准备停止推流.");
//                if (mCameraInstance.getCameraDevice() != null) {
//                    mCameraInstance.getCameraDevice().setPreviewCallbackWithBuffer(null);
//                }
//            }
        }

        /**
         * 累积拍照次数
         */
        private void takePictureCount(boolean increase) {
            if (mCameraInstance.getCameraDevice() != null) {
                if (takePictureCount.get() == 0) {
                    if (!startTakePicture()) {
                        return;
                    }
                }
                if (increase) {
                    takePictureCount.getAndIncrement();
                }
                log.debug("将要拍照的次数 takePictureCount: {}", takePictureCount);
            }
        }

        /**
         * 开始拍照
         */
        private boolean startTakePicture() {
            try {
                takeEnd = false;
                checkTakeEnd();
                mCameraInstance.getCameraDevice().takePicture(null, null, this);
                return true;
            } catch (Exception e) {
                log.error("拍照异常:{}", e);
                takePictureCount.set(0);
                rePreview();
            }
            return false;
        }

        /**
         * 在部分情况下,拍照后,预览会定屏
         */
        private void rePreview() {
            sendEmptyMessage(CAMERA_STOP_PREVIEW);
            sendEmptyMessage(CAMERA_RESTART_PREVIEW);
        }

        /**
         * 2秒内,拍照没有结束的话,强制重置结束
         */
        private void checkTakeEnd() {
            removeMessages(MSG_CHECK_TAKE_END);
            sendEmptyMessageDelayed(MSG_CHECK_TAKE_END, CHECK_TAKE_DELAY);
        }

        private void restartPreview() {
            if (mCurState == STATE_NO) {
                stopPreview(false);
            } else {
                if (mCameraInstance.getCameraDevice() == null) {
                    tryOpenCamera(10);
                    return;
                }

                if (isFirst) {
                    log.info("首次开启预览,先停止预览...");
                    stopPreview(false);
                    isFirst = false;
                }
                log.info("开启预览...");
                startPreview();
            }
            recordControl();
        }

        private void startPreview() {
            if (mBlurSurfaceTexture != null) {
                mCameraInstance.startPreview(mBlurSurfaceTexture.getSurfaceTexture(), 270);
                mBlurSurfaceTexture.onFrameAvailable(mBlurSurfaceTexture.getSurfaceTexture());
            }
        }

        private void releaseCamera() {
            stopPreview(false);
            try {

                log.info("释放摄像头资源.");
                mCameraInstance.stopCamera();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("释放摄像头,失败:{}", e);
            }
        }

        private void stopPreview(boolean needStream) {
            log.info("停止预览");

            if (needStream) {
//                startPreview();
//                streamCallback();
            } else {
                mCameraInstance.stopPreview();
            }

        }

        private void openCamera() {
            log.debug("准备打开摄像头...");
            if (mCameraInstance.getCameraDevice() != null) {
                return;
            }

            mCameraInstance.tryOpenCamera(null);
            if (mCameraInstance.getCameraDevice() == null) {
                log.error("摄像头打开失败.");
                openCount++;
                if (openCount > 5) {
                    log.error("摄像头打开失败次数过多, 即将重启设备...");
                    sendMsgToMain(MSG_REBOOT);
                } else {
                    sendEmptyMessageDelayed(CAMERA_OPEN, DELAY_OPEN_TIME);
                }
            } else {
                openCount = 0;
                mCameraInstance.getCameraDevice().setErrorCallback((error, camera) -> {
                    log.error("摄像头 ErrorCallback {}", error);
                    tryOpenCamera(DELAY_OPEN_TIME);
                });
//                mCameraInstance.getCameraDevice().setPreviewCallback((data, camera) -> {
//                    log.debug("onPreviewFrame {}", data.length);
//                    mCameraInstance.getCameraDevice().addCallbackBuffer(data);
//                });
//                int bufferSize = 1920 * 1080 * 3 / 2;
//                mCameraInstance.getCameraDevice().addCallbackBuffer(new byte[bufferSize]);
//                mCameraInstance.getCameraDevice().addCallbackBuffer(new byte[bufferSize]);
//                mCameraInstance.getCameraDevice().addCallbackBuffer(new byte[bufferSize]);
//                mCameraInstance.getCameraDevice().addCallbackBuffer(new byte[bufferSize]);
                log.info("摄像头,打开成功...");
            }
        }

        private void tryOpenCamera(long time) {
            log.info("摄像头将在 {} 毫秒后, 重新打开.", time);
            mRecordHandler.sendEmptyMessage(RecordHandler.RECORD_STOP);//停止录像,为了下次能友好的开启录像
            removeMessages(CAMERA_RELEASE);
            removeMessages(CAMERA_OPEN);
            removeMessages(CAMERA_RESTART_PREVIEW);
            sendEmptyMessage(CAMERA_RELEASE);
            sendEmptyMessageDelayed(CAMERA_OPEN, time);
            sendEmptyMessageDelayed(CAMERA_RESTART_PREVIEW, time + 100);
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            log.info("拍照回调 onPictureTaken {} count: {}", data.length, takePictureCount);
            takeEnd = true;

            rePreview();
            savePictureAction(data);

            if (mBlurSurfaceTexture != null) {
                mBlurSurfaceTexture.onFrameAvailable(mBlurSurfaceTexture.getSurfaceTexture());
            }

            takePictureCount.decrementAndGet();
            if (takePictureCount.get() > 0) {
                startTakePicture();
            } else {
                takePictureCount.set(0);
            }
            log.info("onPictureTaken count: {} ", takePictureCount);
        }
    }

    /**
     * 控制录像的线程
     */
    public class RecordHandler extends Handler implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {
        public static final int MSG_STOP_RECORD = 1;//发给主线程的消息,通知录像停止了
        public static final int MSG_START_RECORD = 2;
        public static final int MSG_ERROR_RECORD = 3;
        public static final int MSG_TF_SPACE = 4;

        public static final int RECORD_START = 1;//启动录制
        public static final int RECORD_STOP = 2;//停止录制
        public static final int RECORD_RELEASE = 3;//释放录制
        public static final int RECORD_STOP_PREVIEW = 4;//停止录像,并停止预览
        public static final int RECORD_RELEASE_CAMERA = 5;//释放录制并释放摄像头

        public static final int MAX_DURATION = 60 * 1000;//最常录制时间
        MediaRecorder mMediaRecorder;
        volatile boolean isRecordStart = false;
        String curFilePath;
        private String alreadySaveFilePath = "-";//保存过的文件路径,防止重复保存

        public RecordHandler(Looper looper) {
            super(looper);
            createMediaRecorder();
        }

        private void releaseMediaRecorder() {
            stopRecorder();
            if (mMediaRecorder != null) {
                log.info("释放MediaRecorder资源.");
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        }

        private void createMediaRecorder() {
            releaseMediaRecorder();
            mMediaRecorder = new MediaRecorder();
        }

        private void stopRecorder() {
            if (isRecordStart && mMediaRecorder != null) {
                log.info("正在停止录制...");
                mMediaRecorder.reset();
                isRecordStart = false;
                saveFile();
                onRecordStop();
            }
        }

        private void saveFile() {
            //保存录像
            if (!alreadySaveFilePath.equalsIgnoreCase(curFilePath)) {
                log.info("即将保存录制文件:{}", curFilePath);
                VideoSaveTools.saveCurVideoInfo(curFilePath);
                alreadySaveFilePath = curFilePath;
            }
        }

        private void restartRecorder(String filePath, boolean save) {
            if (isRecordStart) {
                log.info("录制已经开始啦...(重复调用)");
                return;
            }

            if (mMediaRecorder == null) {
                createMediaRecorder();
            }

            Camera cameraDevice = mCameraHandler.mCameraInstance.getCameraDevice();
            mMediaRecorder.reset();

            if (save) {
                saveFile();
            }

            if (cameraDevice == null) {
                log.error(" camera == null, 终止录制.");
                isRecordStart = false;
                return;
            }

            if (!FileUtil.isTFlashCardExists()) {
                log.info("TF卡不存在, 终止录制");
                onRecordFailed();
                return;
            }

            float sdFreeSpace = FileUtil.getSdFreeSpace();//剩余空间
            if (sdFreeSpace < 0.1) {
                log.error("存储卡空间不足,终止录制.");
                onRecordFailed();
                sendMsgToMain(MSG_TF_SPACE);
                return;
            }

            try {
                cameraDevice.unlock();
            } catch (Exception e) {
                log.error("启动录制失败:{}", e);
                onRecordFailed();
                return;
            }

            mMediaRecorder.setCamera(cameraDevice);

            CamcorderProfile mProfile = CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_HIGH);
            mProfile.audioSampleRate = 16000;//16K, 必须
//            mProfile.audioBitRate = 256000;
//            mProfile.audioBitRate = 16000;
//            mProfile.audioChannels = 1;
            mProfile.audioCodec = MediaRecorder.AudioEncoder.AMR_WB;//必须

            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

//            log.debug("{} {} {} {}", mProfile.audioSampleRate, mProfile.audioBitRate, mProfile.audioChannels, mProfile.audioCodec);
//            log.debug("{} {} {} {} {}", mProfile.videoFrameRate, mProfile.videoBitRate, mProfile.videoFrameWidth, mProfile.videoFrameHeight, mProfile.videoCodec);
            log.debug("{} {} {} {}", mProfile.audioChannels, mProfile.audioSampleRate, mProfile.audioBitRate, mProfile.audioCodec);

            boolean isMultiMic = isMultiMic();
//            boolean isMultiMic = true;
            if (isMultiMic) {
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            }

            mMediaRecorder.setOutputFormat(mProfile.fileFormat);
            if (isMultiMic) {
                /*一个都不能少*/
                mMediaRecorder.setAudioChannels(mProfile.audioChannels);
                mMediaRecorder.setAudioSamplingRate(mProfile.audioSampleRate);
                mMediaRecorder.setAudioEncodingBitRate(mProfile.audioBitRate);
                mMediaRecorder.setAudioEncoder(mProfile.audioCodec);
            }

            /*一个都不能少*/
            mMediaRecorder.setVideoEncodingBitRate(FrontVideoConfigParam.DEFAULT_VIDEOBITRATE);
            mMediaRecorder.setVideoFrameRate(FrontVideoConfigParam.DEFAULT_RATE);
            mMediaRecorder.setVideoSize(FrontVideoConfigParam.DEFAULT_WIDTH, FrontVideoConfigParam.DEFAULT_HEIGHT);
            mMediaRecorder.setVideoEncoder(mProfile.videoCodec);

            curFilePath = filePath;//记录当前正在录制的视频文件路径
            mMediaRecorder.setOutputFile(filePath);
            mMediaRecorder.setMaxDuration(MAX_DURATION);
            mMediaRecorder.setOnInfoListener(this);
            mMediaRecorder.setOnErrorListener(this);

            try {
                mMediaRecorder.prepare();
                mMediaRecorder.start();
                isRecordStart = true;
                log.info("录制已开始:{}, 是否录音:{}", filePath, isMultiMic);
                sendMsgToMain(MSG_START_RECORD);
            } catch (Exception e) {
                log.error("录制解析/开始失败:{}", e);
                onRecordFailed();

                sendEmptyMessageDelayed(RECORD_START, 100);//100毫秒后,重启录制
            }
        }

        public boolean isRecordStart() {
            return isRecordStart;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECORD_START:
                    restartRecorder(getVideoFileName(), false);
                    break;
                case RECORD_STOP:
                    stopRecorder();
                    break;
                case RECORD_RELEASE:
                    releaseMediaRecorder();
                    break;
                case RECORD_STOP_PREVIEW:
                    stopRecorder();
                    mCameraHandler.sendMessage(mCameraHandler.obtainMessage(CameraHandler.CAMERA_STOP_PREVIEW, msg.obj));
                    break;
                case RECORD_RELEASE_CAMERA:
                    releaseMediaRecorder();
                    mCameraHandler.sendEmptyMessage(CameraHandler.CAMERA_RELEASE);
                    break;
            }
        }

        public String getCurFilePath() {
            return curFilePath;
        }

        private String getVideoFileName() {
            return com.dudu.drivevideo.camera.CameraInstance.generateCurVideoName();
        }

        private void onRecordStop() {
            log.info("录像停止,发送消息至主线程");
            isRecordStart = false;
            sendMsgToMain(MSG_STOP_RECORD);
        }

        private void onRecordFailed() {
            log.info("录像错误,发送消息至主线程");
            isRecordStart = false;
            sendMsgToMain(MSG_ERROR_RECORD);
        }

        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            log.info("录制时间已到,准备切换文件继续录制..{}..{}", what, extra);
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                isRecordStart = false;
                restartRecorder(getVideoFileName(), true);
            }
        }

        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            log.info("录制错误的回调..{}..{}", what, extra);
            FileUtil.deleteFile(curFilePath);//删除录制有问题的文件
            onRecordFailed();
        }
    }
}
