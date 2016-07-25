package com.dudu.drivevideo.rearcamera.camera;

/**
 * Created by dengjun on 2016/4/29.
 * Description :
 */
public interface RearCameraListener {

    public void onInfo(RearCameraListenerMessage rearCameraListenerMessage);

    public void onError(RearCameraListenerMessage rearCameraListenerMessage);
}
