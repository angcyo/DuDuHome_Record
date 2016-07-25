package com.dudu.drivevideo.frontcamera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.HandlerThread;
import android.view.SurfaceHolder;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.process.ProcessUtils;
import com.dudu.drivevideo.config.FrontVideoConfigParam;
import com.dudu.drivevideo.frontcamera.camera.CameraHandlerMessage;
import com.dudu.drivevideo.frontcamera.camera.FrontCamera;
import com.dudu.drivevideo.frontcamera.camera.FrontCameraRecorder;
import com.dudu.drivevideo.frontcamera.camera.PhotoObtain;
import com.dudu.drivevideo.frontcamera.camera.PictureObtain;
import com.dudu.drivevideo.frontcamera.preview.BlurGLSurfaceView;
import com.dudu.drivevideo.frontcamera.preview.FrontCameraPreviewWindow;
import com.dudu.drivevideo.frontcamera.preview.PreviewState;
import com.dudu.drivevideo.frontcamera.preview.SurfaceTextureCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by dengjun on 2016/5/21.
 * Description :
 */
public class FrontCameraService implements SurfaceTextureCallback, Camera.ShutterCallback,
        MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener, Camera.ErrorCallback, SurfaceHolder.Callback, TcpSocketService.IStreamListener {

    private Logger log = LoggerFactory.getLogger("video.frontdrivevideo");
    private FrontCameraService frontCameraService = this;

    private FrontVideoConfigParam frontVideoConfigParam;
    private FrontCamera frontCamera;
    private FrontCameraRecorder frontCameraRecorder;

    private PictureObtain pictureObtain;
    private UploadVideoStream uploadVideoStream;

    private BlurGLSurfaceView blurGLSurfaceView;
    private FrontCameraPreviewWindow frontCameraPreviewWindow;

    private PreviewState previewState = PreviewState.MAINACITITY_PREVIEW;

    private DriveVideoHandler driveVideoHandler;
    private HandlerThread handlerThread;
    private boolean handlerThreadInitFlag = false;

    private TcpSocketService mSocketService;

    private PhotoObtain mPhotoObtain;

    public FrontCameraService() {
        log.info("构造FrontCameraService实例");
        frontVideoConfigParam = new FrontVideoConfigParam();

        frontCamera = new FrontCamera(frontVideoConfigParam);
        frontCamera.setErrorCallback(this);

        frontCameraRecorder = new FrontCameraRecorder(frontCamera, frontVideoConfigParam);
        frontCameraRecorder.setOnErrorListener(this);
        frontCameraRecorder.setOnInfoListener(this);

        pictureObtain = new PictureObtain(frontCamera);
        pictureObtain.setShutterCallback(this);
        uploadVideoStream = new UploadVideoStream(frontCamera);

        frontCameraPreviewWindow = new FrontCameraPreviewWindow(CommonLib.getInstance().getContext());
        frontCameraPreviewWindow.setSurfaceHolderCallback(this);

        mSocketService = new TcpSocketService();
        mSocketService.setIStreamListener(this);

        mPhotoObtain = new PhotoObtain(frontCamera);
    }

    @Override
    public void onError(int error, Camera camera) {
        log.error("摄像头报错 error = {}", error);
//        closeCamera();
//        startPreview();
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        log.error("mediaRedcord 报错 what = {}， extra = {}", what, extra);
        relaseCameraAndMediaRecord();
        startRecord();
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        log.info("mediaRedcord回调信息 what = {}， extra = {}", what, extra);
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            startRecord();
        }
    }

    @Override
    public void surfaceCreated(SurfaceTexture surfaceTexture) {
        log.info("预览状态：{}", previewState.getMessage());

        startRecord();
    }

    @Override
    public void surfaceChanged(SurfaceTexture surfaceTexture, GL10 gl, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder, SurfaceTexture surfaceTexture) {
        log.info("预览状态：{}", previewState.getMessage());
        blurGLSurfaceView.onPause();
        stopRecord();
        stopPreview();
        previewState = PreviewState.WINDOW_PREVIEW;
        startBackgroundRecord();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        log.info("FrontCameraPreviewWindow surfaceCreated 创建");
        startRecord();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        log.info("FrontCameraPreviewWindow surfaceChanged 改变");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        log.info("FrontCameraPreviewWindow surfaceDestroyed 销毁");
        previewState = PreviewState.MAINACITITY_PREVIEW;
        stopRecord();
        stopPreview();
        blurGLSurfaceView.onResume();
    }

    @Override
    public void onShutter() {
        log.debug("onShutter 拍照次数：{}", pictureObtain.takePictureCount.get());
        startPreview();
        if (pictureObtain.takePictureCount.decrementAndGet() > 0) {
            takePictureAction();
        }
    }

    private void initHanderThread() {
        handlerThread = new HandlerThread("Front Camera Thread") {
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                log.info("handlerThread 初始化完成");
                if (driveVideoHandler == null) {
                    driveVideoHandler = new DriveVideoHandler(handlerThread.getLooper(), frontCameraService);
                }
                openCamera();
                if (blurGLSurfaceView != null && blurGLSurfaceView.getSurfaceTexture() != null) {
                    log.info("blurGLSurfaceView != null 开启预览");
                    startPreview();
                } else {
                    startPreviewDelay(5);
                }
                startRecord();
            }
        };

        handlerThread.setPriority(Thread.MAX_PRIORITY);
        handlerThread.start();
    }

    public synchronized void init() {
        log.info("当前线程：{}，当前进程：{}", Thread.currentThread().getName(), ProcessUtils.getCurProcessName(CommonLib.getInstance().getContext()));
        synchronized (this){
            if (handlerThread == null && handlerThreadInitFlag == false) {
                log.info("FrontCameraService初始化");
                initHanderThread();
                handlerThreadInitFlag = true;
            }else {
                log.info("handlerThread 已经初始化了");
            }
        }
    }

    public synchronized void release() {
        synchronized (this){
            if (handlerThread != null && handlerThread.isAlive()) {
                log.info("FrontCameraService释放");
                removeMessage(CameraHandlerMessage.START_RECORD);
                removeMessage(CameraHandlerMessage.START_PREVIEW);
                removeMessage(CameraHandlerMessage.INIT_CAMERA);
                destroyStream();
                stopRecord();
                stopPreview();
                closeCamera();
                handlerThread.quitSafely();
                driveVideoHandler = null;
                handlerThread = null;
                handlerThreadInitFlag = false;
            }
        }
    }

    public synchronized void openCamera() {
        sendMessage(CameraHandlerMessage.INIT_CAMERA);
    }

    public synchronized void closeCamera() {
        sendMessage(CameraHandlerMessage.RELEASE_CAMERA);
    }

    public synchronized void destroyStream() {
        sendMessage(CameraHandlerMessage.DESTROY_UPLOAD_VIDEO_STREAM);
    }

    public synchronized void startPreview() {
        sendMessage(CameraHandlerMessage.INIT_CAMERA);
        sendMessage(CameraHandlerMessage.START_PREVIEW);
    }

    public synchronized void startPreviewDelay(int seconds) {
        sendMessageDelay(CameraHandlerMessage.INIT_CAMERA, seconds);
        sendMessageDelay(CameraHandlerMessage.START_PREVIEW, seconds);
    }

    public synchronized void stopPreview() {
        sendMessage(CameraHandlerMessage.STOP_PREVIEW);
    }

    public synchronized void startRecord() {
        sendMessage(CameraHandlerMessage.STOP_UPLOAD_VIDEO_STREAM);
        sendMessage(CameraHandlerMessage.RELEASE_RECORD);
        sendMessage(CameraHandlerMessage.SAVE_VIDEO);
        sendMessage(CameraHandlerMessage.INIT_CAMERA);
        sendMessage(CameraHandlerMessage.START_PREVIEW);

        log.debug("录像使能状态：{}", frontCameraRecorder.isRecordEnable());
        if(frontCameraRecorder.isRecordEnable()){
            sendMessage(CameraHandlerMessage.START_RECORD);
        }
    }

    public synchronized void stopRecord() {
        sendMessage(CameraHandlerMessage.RELEASE_RECORD);
        sendMessage(CameraHandlerMessage.SAVE_VIDEO);
    }

    private synchronized void relaseCameraAndMediaRecord() {
        sendMessage(CameraHandlerMessage.RELEASE_CAMERA_AND_RECORD);
    }

    public synchronized void startForegroundRecord() {
        log.info("开启主活动预览录像");
        previewState = PreviewState.MAINACITITY_PREVIEW;
        frontCameraPreviewWindow.removeFromWindow();
    }

    public synchronized void startBackgroundRecord() {
        log.info("开启浮窗预览录像");
        previewState = PreviewState.WINDOW_PREVIEW;
        frontCameraPreviewWindow.addToWindow();
    }

    public synchronized void stopUploadVideoStream() {
        sendMessage(CameraHandlerMessage.STOP_UPLOAD_VIDEO_STREAM);
        frontCameraRecorder.setRecordEnable(true);
        startRecord();
    }

    public synchronized void startUploadVideoStream() {
        stopRecord();
        frontCameraRecorder.setRecordEnable(false);
        sendMessage(CameraHandlerMessage.START_PREVIEW);
        sendMessage(CameraHandlerMessage.START_UPLOAD_VIDEO_STREAM);
    }

    public synchronized void takePicture() {
//        log.debug("takePicture 拍照次数：{}", pictureObtain.takePictureCount.incrementAndGet());
        takePictureAction();
    }

    private void takePictureAction() {
//        log.debug("takePictureAction 拍照次数：{}", pictureObtain.takePictureCount.get());
//        if (pictureObtain.takePictureCount.get() > 0) {
            sendMessage(CameraHandlerMessage.TAKE_PICTURE);
//        }
    }

    private void sendMessage(CameraHandlerMessage cameraHandlerMessage) {
        if (driveVideoHandler != null && handlerThread != null && handlerThread.isAlive()) {
            driveVideoHandler.sendMessage(cameraHandlerMessage);
        }
    }

    private void sendMessageDelay(CameraHandlerMessage cameraHandlerMessage, int seconds) {
        if (driveVideoHandler != null && handlerThread != null && handlerThread.isAlive()) {
            driveVideoHandler.sendMessageDelay(cameraHandlerMessage, seconds);
        }
    }

    private void removeMessage(CameraHandlerMessage cameraHandlerMessage){
        if (driveVideoHandler != null && handlerThread != null && handlerThread.isAlive()) {
            driveVideoHandler.removeMessage(cameraHandlerMessage);
        }
    }

    public void setPreviewBlur(boolean isBlur) {
        blurGLSurfaceView.setBlur(isBlur);
    }

    public FrontCamera getFrontCamera() {
        return frontCamera;
    }

    public FrontCameraRecorder getFrontCameraRecorder() {
        return frontCameraRecorder;
    }

    public BlurGLSurfaceView getBlurGLSurfaceView() {
        return blurGLSurfaceView;
    }

    public void setBlurGLSurfaceView(BlurGLSurfaceView blurGLSurfaceView) {
        this.blurGLSurfaceView = blurGLSurfaceView;
        blurGLSurfaceView.setSurfaceTextureCallback(this);
    }

    public PhotoObtain getPhotoObtain() {
        return mPhotoObtain;
    }

    public FrontCameraPreviewWindow getFrontCameraPreviewWindow() {
        return frontCameraPreviewWindow;
    }

    public PreviewState getPreviewState() {
        return previewState;
    }

    public PictureObtain getPictureObtain() {
        return pictureObtain;
    }

    public UploadVideoStream getUploadVideoStream() {
        return uploadVideoStream;
    }

    @Override
    public void onStartStream() {
        startUploadVideoStream();
    }

    @Override
    public void onStopStream() {
        stopUploadVideoStream();
    }
}
