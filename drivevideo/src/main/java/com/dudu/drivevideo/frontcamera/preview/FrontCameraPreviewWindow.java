package com.dudu.drivevideo.frontcamera.preview;

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
public class FrontCameraPreviewWindow extends SurfaceView  {

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;

    private Logger log = LoggerFactory.getLogger("video.frontdrivevideo");

    public FrontCameraPreviewWindow(Context context) {
        super(context);
        initView(context);
        initWindow();
    }

    private void initView(Context context){

    }

    private void initWindow(){
        mWindowManager = (WindowManager)CommonLib.getInstance().getContext().getSystemService(Context.WINDOW_SERVICE);

        mLayoutParams = new WindowManager.LayoutParams(1, 1, WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
    }


    public void setSurfaceHolderCallback(SurfaceHolder.Callback surfaceHolderCallback) {
        this.getHolder().addCallback(surfaceHolderCallback);
    }

    public void addToWindow(){
        try {
            if (this.getParent() == null){
                mWindowManager.addView(this, mLayoutParams);
            }
        } catch (Exception e) {
            log.error("异常", e);
        }
    }

    public void removeFromWindow(){
        try {
            if (this.getParent() != null){
                mWindowManager.removeView(this);
            }
        } catch (Exception e) {
            log.error("异常", e);
        }
    }
}
