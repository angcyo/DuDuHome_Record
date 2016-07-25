package com.dudu.drivevideo.rearcamera;

import android.view.SurfaceHolder;

import com.dudu.drivevideo.config.RearVideoConfigParam;

/**
 * Created by dengjun on 2016/5/18.
 * Description :
 */
public class RearCameraManage {
    private static RearCameraManage instance = null;

    private RearCameraHandler rearCameraHandler;

    private RearCameraManage() {
        rearCameraHandler = new RearCameraHandler();
    }

    public static RearCameraManage getInstance() {
        if (instance == null) {
            synchronized (RearCameraManage.class) {
                if (instance == null) {
                    instance = new RearCameraManage();
                }
            }
        }
        return instance;
    }

    public void init() {
        rearCameraHandler.init();
    }

    public void release() {
        rearCameraHandler.release();
        instance = null;
    }


    public void openCamera() {
        rearCameraHandler.openCamera();
    }

    public void closeCamera() {
        rearCameraHandler.closeCamera();
    }

    public void startDrivingPreview() {
        rearCameraHandler.startPreview(true);
    }

    public void startPreviewHolder(SurfaceHolder surfaceHolder) {
        rearCameraHandler.startPreviewHolder(surfaceHolder);
    }

    public boolean isPreviewing() {
        return rearCameraHandler.isPreviewing();
    }

    public void stopPreViewHolder() {
        rearCameraHandler.stopPreviewHolder();
    }

    public void stopPreViewHolder2() {
        rearCameraHandler.stopPreviewHolder2();
    }

    public void setRecordTime(int time) {
        rearCameraHandler.setRecordTime(time);
    }

    public void takePhoto() {
        rearCameraHandler.takePhoto();
    }

    public void startBackCarPreview() {
        rearCameraHandler.startPreview(false);
    }

    public void stopPreview() {
        rearCameraHandler.stopPreview();
    }

    public void startRecord() {
        rearCameraHandler.startRecord();
    }

    public boolean isRecord() {
        return rearCameraHandler.isRecording();
    }

    public void stopRecord() {
        rearCameraHandler.stopRecord();
    }

    public RearVideoConfigParam getRearVideoConfigParam() {
        return rearCameraHandler.getRearVideoConfigParam();
    }

    public void setPreviewEnable(boolean previewEnable) {
        rearCameraHandler.setPreviewEnable(previewEnable);
    }

    public void setRecordEnable(boolean recordEnable) {
        rearCameraHandler.setRecordEnable(recordEnable);
    }
}
