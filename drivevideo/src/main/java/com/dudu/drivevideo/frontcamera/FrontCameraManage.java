package com.dudu.drivevideo.frontcamera;

import com.dudu.drivevideo.frontcamera.preview.BlurGLSurfaceView;

/**
 * Created by dengjun on 2016/5/21.
 * Description :
 */
public class FrontCameraManage {
    private static FrontCameraManage instance = null;

    private FrontCameraService frontCameraService;

    private FrontCameraManage() {
        frontCameraService = new FrontCameraService();
    }

    public static FrontCameraManage getInstance() {
        if (instance == null) {
            synchronized (FrontCameraManage.class) {
                if (instance == null) {
                    instance = new FrontCameraManage();
                }
            }
        }
        return instance;
    }

    public void init() {
        frontCameraService.init();
    }

    public void release() {
        frontCameraService.release();
    }

    public void startRecord() {
        frontCameraService.startRecord();
    }

    public void stopRecord() {
        frontCameraService.stopRecord();
    }

    public void startPreview() {
        frontCameraService.startPreview();
    }

    public void stopPreview() {
        frontCameraService.stopPreview();
    }

    public void setPreviewBlur(boolean isBlur) {
        frontCameraService.setPreviewBlur(isBlur);
    }

    public void startForegroundRecord() {
        frontCameraService.startForegroundRecord();
    }

    /*public void startBackgroundRecord() {
        frontCameraService.startBackgroundRecord();
    }*/

    public void startUploadVideoStream() {
        frontCameraService.startUploadVideoStream();
    }

    public void stopUploadVideoStream() {
        frontCameraService.stopUploadVideoStream();
    }


    public void setBlurGLSurfaceView(BlurGLSurfaceView blurGLSurfaceView) {
        frontCameraService.setBlurGLSurfaceView(blurGLSurfaceView);
    }

    public void takePicture() {
        frontCameraService.takePicture();
    }

    public void setRecordEnable(boolean recordEnable) {
        frontCameraService.getFrontCameraRecorder().setRecordEnable(recordEnable);
    }

    public boolean isRecording() {
        return frontCameraService.getFrontCameraRecorder().isRecording();
    }
}
