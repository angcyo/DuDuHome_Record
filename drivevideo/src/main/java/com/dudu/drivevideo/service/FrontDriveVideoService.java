package com.dudu.drivevideo.service;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceHolder;

import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.drivevideo.exception.CameraErrorCode;
import com.dudu.drivevideo.camera.CameraInstance;
import com.dudu.drivevideo.exception.DirveVideoException;
import com.dudu.drivevideo.video.VideoSaveTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dengjun on 2016/4/24.
 * Description :
 */
public class FrontDriveVideoService implements MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener, Camera.ErrorCallback{
    private static  FrontDriveVideoService instance = null;

    public static final int INIT_CAMERA = 0;
    public static final int RELEASE_CAMERA = 1;
    public static final int START_RECORD = 2;
    public static final int STOP_RECORD = 3;
    public static final int RESET_RECORD = 4;
    public static final int RELEASE_RECORD = 5;

    public static final int RELEASE_CAMERA_AND_RECORD = 6;
    public static final int TAKE_PICTURE = 7;
    public static final int START_PREVIEW = 8;
    public static final int STOP_PREVIEW = 9;
    public static final int SAVE_VIDEO = 10;

    private CameraInstance cameraInstance;
    private HandlerThread handlerThread;
    private DriveVideoHandler driveVideoHandler;

    private Logger log = LoggerFactory.getLogger("video.frontdrivevideo");

    private FrontDriveVideoService() {
        handlerThread = new HandlerThread("Camera Thread"){
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                log.info("handlerThread 初始化完成");
                cameraInstance = new CameraInstance();
                cameraInstance.setOnInfoListener(FrontDriveVideoService.this);
                cameraInstance.setErrorCallback(FrontDriveVideoService.this);
                cameraInstance.setOnErrorListener(FrontDriveVideoService.this);

                driveVideoHandler = new DriveVideoHandler(handlerThread.getLooper());
                sendMessage(INIT_CAMERA);
            }
        };
    }

    private class DriveVideoHandler extends Handler {
        public DriveVideoHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case INIT_CAMERA:
                        log.info("收到初始化摄像头消息");
                        cameraInstance.initCamera();
                        break;
                    case RELEASE_CAMERA:
                        log.info("收到释放摄像头消息");
                        cameraInstance.releaseCamera();
                        break;
                    case START_RECORD:
                        log.info("收到开启录像消息");
                        if (FileUtil.isTFlashCardExists()) {
                            cameraInstance.startRecord();
                        } else {
                            log.info("TF卡不存在，不开启录像");
                        }
                        break;
                    case STOP_RECORD:
                        log.info("收到停止录像消息");
                        cameraInstance.stopRecord();
                        break;
                    case RELEASE_RECORD:
                        cameraInstance.releaseMediaRecorder();
                        break;
                    case RELEASE_CAMERA_AND_RECORD:
                        log.info("收到释放摄像头和停止录像消息");
                        cameraInstance.releaseMediaRecordAndCamera();
                        break;
                    case TAKE_PICTURE:
                        cameraInstance.takePicture();
                        break;
                    case START_PREVIEW:
                        log.info("收到开启预览消息");
                        cameraInstance.startPreview();
                        break;
                    case STOP_PREVIEW:
                        log.info("收到停止预览消息");
                        cameraInstance.stopPreview();
                        break;
                    case SAVE_VIDEO:
                        VideoSaveTools.saveCurVideoInfo(cameraInstance.getCurVideoFileAbsolutePath());
                        break;
                }
            } catch (DirveVideoException e) {
                CameraErrorCode errorCode = e.getErrorCode();
                log.error(errorCode.getDetailErrMsg(), e);
                switch (errorCode)
                {
                    case OPEN_CAMERA_ERROR:
                        sendEmptyMessageDelayed(INIT_CAMERA, 300);
                        break;
                    case CLOSE_CAMERA_ERROR:
                    case START_PREVIEW_ERROR:
                    case STOP_PREVIEW_ERROR:
                    case STOP_RECORD_ERROR:
                        //not do nothing
                        break;
                    case START_RECORD_ERROR:
                        sendEmptyMessage(RELEASE_RECORD);
                        sendEmptyMessage(START_RECORD);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 初始化行车记录
     */
    public void init(){
        if (handlerThread.isAlive())
            return;
        log.info("FrontDriveVideoService初始化");
        handlerThread.setPriority(Thread.MAX_PRIORITY);
        handlerThread.start();
    }

    /**
     * 释放整个录像资源
     */
    public void release(){
        log.info("FrontDriveVideoService释放");
        sendMessage(RELEASE_CAMERA_AND_RECORD);
        handlerThread.quitSafely();
    }

    /**
     * 启动后台录像
     */
    public void startBackgroundRecord() {
        cameraInstance.setForegoundPreview(false);
        startRecord();
    }

    /**
     * 启动前台录像
     * @param surfaceHolder
     */
    public void startForegroundRecord(SurfaceHolder surfaceHolder){
        cameraInstance.setForegoundPreview(true);
        cameraInstance.setForegroundSurfaceHolder(surfaceHolder);
        startRecord();
    }

    /**
     * 开启录像
     */
    public void startRecord(){
        sendMessage(RELEASE_RECORD);
        sendMessage(SAVE_VIDEO);
        sendMessage(INIT_CAMERA);
        sendMessage(START_PREVIEW);
        sendMessage(START_RECORD);
    }

    /**
     * 停止录像
     */
    public void stopRecord(){
        sendMessage(RELEASE_RECORD);
        sendMessage(SAVE_VIDEO);
    }

    private void sendMessage(int message){
        if (driveVideoHandler != null){
            driveVideoHandler.sendEmptyMessage(message);
        }
    }

    @Override
    public void onError(int error, Camera camera) {
        log.error("摄像头报错 error = {}", error);
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        log.error("mediaRedcord 报错 what = {}， extra = {}", what, extra);
        sendMessage(RELEASE_CAMERA_AND_RECORD);
        startRecord();
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        log.info("mediaRedcord回调信息 what = {}， extra = {}", what, extra);
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            startRecord();
        }
    }

    /* camera实例操作都在主线程进行，使用此方法获取摄像头实例时，调用确保在主线程*/
    public CameraInstance getCameraInstance() {
        return cameraInstance;
    }


    /**
     * 拍摄照片
     */
    public void takePicture(){
        sendMessage(TAKE_PICTURE);
    }

    public static FrontDriveVideoService getInstance(){
        if (instance == null){
            synchronized (FrontDriveVideoService.class){
                if (instance == null){
                    instance = new FrontDriveVideoService();
                }
            }
        }
        return instance;
    }
}
