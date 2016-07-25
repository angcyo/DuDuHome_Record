package com.dudu.drivevideo.frontcamera.preview;

import android.graphics.SurfaceTexture;
import android.view.SurfaceHolder;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by dengjun on 2016/5/21.
 * Description : 生命周期同SurfaceHolder.Callback
 */
public interface SurfaceTextureCallback {

    public void surfaceCreated(SurfaceTexture surfaceTexture);


    public void surfaceChanged(SurfaceTexture surfaceTexture, GL10 gl, int width, int height);


    public void surfaceDestroyed(SurfaceHolder holder, SurfaceTexture surfaceTexture);
}
