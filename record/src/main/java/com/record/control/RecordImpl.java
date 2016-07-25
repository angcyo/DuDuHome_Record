package com.record.control;

import android.view.SurfaceHolder;

import com.dudu.commonlib.CommonLib;
import com.dudu.drivevideo.rearcamera.RearCameraManage;
import com.dudu.drivevideo.rearcamera.preview.RearCameraPreview;
import com.record.util.Debug;

/**
 * Created by robi on 2016-06-04 14:39.
 */
public class RecordImpl implements IRecord {

    private RecordImpl() {
//        RearCameraManage.getInstance().init();
    }

    public static RecordImpl instance() {
        return Holder.impl;
//        return new RecordImpl();
    }

    @Override
    public void startPreview(SurfaceHolder surfaceHolder) {
        Debug.show(CommonLib.getInstance().getContext(), "RecordImpl 开始预览.");
        RearCameraManage.getInstance().startPreviewHolder(surfaceHolder);
    }

    @Override
    public void stopPreview() {
        Debug.show(CommonLib.getInstance().getContext(), "RecordImpl 停止预览.");
        RearCameraManage.getInstance().stopPreViewHolder();
    }

    @Override
    public void startRecord() {
        Debug.show(CommonLib.getInstance().getContext(), "RecordImpl 开始录制.");
        RearCameraManage.getInstance().setRecordEnable(true);
        RearCameraManage.getInstance().startRecord();
    }

    @Override
    public void stopRecord() {
        Debug.show(CommonLib.getInstance().getContext(), "RecordImpl 停止录制.");
        RearCameraManage.getInstance().stopRecord();
    }

    @Override
    public void takePhoto() {
        Debug.show(CommonLib.getInstance().getContext(), "拍照.");
        RearCameraPreview.log.info("开始拍照.RecordImpl");
        RearCameraManage.getInstance().takePhoto();
    }

    @Override
    public boolean isPreviewing() {
        return RearCameraManage.getInstance().isPreviewing();
    }

    @Override
    public boolean isRecording() {
        return RearCameraManage.getInstance().isRecord();
    }

    @Override
    public void release() {
        Debug.show(CommonLib.getInstance().getContext(), "RecordImpl 释放资源.");
        RearCameraManage.getInstance().release();
    }

    @Override
    public void init() {
        RearCameraManage.getInstance().init();
    }

    static class Holder {
        static final RecordImpl impl = new RecordImpl();
    }
}
