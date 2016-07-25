package com.dudu.drivevideo.rearcamera.preview;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.WindowUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dengjun on 2016/3/27.
 * Description :
 */
public class RearCameraPreviewWindow {

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;

    private View priviewView;
    private SurfaceView previewSurfaceView;
    private ImageButton backImageButton;


    private Logger log = LoggerFactory.getLogger("video.reardrivevideo");

    public RearCameraPreviewWindow(Context context) {
        initView(context);
        initWindow();
    }

    private void initView(Context context){
//        priviewView = LayoutInflater.from(context).inflate(com.dudu.drivevideo.R.layout.rear_camera_preview_layout, null, false);
//        previewSurfaceView = (SurfaceView)priviewView.findViewById(com.dudu.drivevideo.R.id.preview);
//
//
//        backImageButton = (ImageButton)priviewView.findViewById(com.dudu.drivevideo.R.id.button_back_window);
    }

    private void initWindow(){
        mWindowManager = (WindowManager)CommonLib.getInstance().getContext().getSystemService(Context.WINDOW_SERVICE);

        mLayoutParams =
                new WindowManager.LayoutParams(
                        WindowUtils.getScreenWidthAndHeight()[0],
                        WindowUtils.getScreenWidthAndHeight()[1],
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
        mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
    }

    public void setBackButtonVisibleState(boolean visibleState){
        backImageButton.setVisibility(visibleState?View.VISIBLE:View.INVISIBLE);
    }

    public void addToWindow(){
        try {
            if (priviewView.getParent() == null){
                mWindowManager.addView(priviewView, mLayoutParams);
            }
        } catch (Exception e) {
            log.error("异常", e);
        }
    }

    public void removeFromWindow(){
        if (priviewView == null) {
            return;
        }
        try {
            if (priviewView.getParent() != null){
                mWindowManager.removeView(priviewView);
            }
        } catch (Exception e) {
            log.error("异常", e);
        }
    }


    public void addSurfaceHolderCallback(SurfaceHolder.Callback holderCallback){
        if (previewSurfaceView != null){
            previewSurfaceView.getHolder().addCallback(holderCallback);
        }
    }

    public void removeSurfaceHolderCallback(SurfaceHolder.Callback holderCallback){
        if (previewSurfaceView != null){
            previewSurfaceView.getHolder().removeCallback(holderCallback);
        }
    }


    public void setBackButtonOnClickListener(View.OnClickListener onClickListener){
        if (backImageButton != null){
            backImageButton.setOnClickListener(onClickListener);
        }
    }
}
