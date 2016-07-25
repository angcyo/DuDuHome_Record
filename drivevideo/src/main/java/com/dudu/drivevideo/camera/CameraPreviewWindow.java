package com.dudu.drivevideo.camera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.dudu.commonlib.CommonLib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dengjun on 2016/3/27.
 * Description :
 */
public class CameraPreviewWindow extends SurfaceView implements SurfaceHolder.Callback{
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;


    private Logger log = LoggerFactory.getLogger("video.frontdrivevideo");

    public CameraPreviewWindow(Context context) {
        super(context);
        init();
    }

    private void init(){
        mWindowManager = (WindowManager)CommonLib.getInstance().getContext().getSystemService(Context.WINDOW_SERVICE);

        mLayoutParams = new WindowManager.LayoutParams(1, 1, WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;

        mWindowManager.addView(this, mLayoutParams);
    }

    public void updatePreviewSize(int width, int height) {
        mLayoutParams.width = width;
        mLayoutParams.height = height;
        mWindowManager.updateViewLayout(this, mLayoutParams);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        log.debug("CameraPreviewWindow surfaceCreated创建");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        log.debug("CameraPreviewWindow  surfaceChanged改变  format = {}  w = {}  h=  {}", format, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        log.debug("CameraPreviewWindow surfaceDestroyed销毁");
    }
}
