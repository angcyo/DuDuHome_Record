package com.dudu.drivevideo.rearcamera.camera;

import com.dudu.drivevideo.exception.DirveVideoException;

/**
 * Created by dengjun on 2016/4/29.
 * Description :
 */
public interface RearCameraInterface <T, V>{
    public void init() throws DirveVideoException;
    public void release();

    public void startPreview()  throws DirveVideoException;
    public void stopPreview() throws DirveVideoException;

    public void setPreviewView(V view);
    public V getPreviewView();

    public void startRecord();
    public void stopRecord();

    public void setCofigParam(T configParam);
    public T getConfigParam();

    public void setRearCameraListener(RearCameraListener rearCameraListener);

    public void setCurVideoPath(String videoPath);

    public String getCurVideoPath();

    public void takePicture();
}
