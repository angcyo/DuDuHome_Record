package com.dudu.aios.ui.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.dudu.android.launcher.R;
import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.WindowUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dengjun on 2016/3/27.
 * Description :
 */
public class BackCarPreviewWindow{
    private final static String ACTION_ACC_BL_CHANGED = "android.intent.action.ACC_BL";

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;

    private View priviewView;
    private ImageView previewImageView;

    private Logger log = LoggerFactory.getLogger("video.reardrivevideo");

    public BackCarPreviewWindow(Context context) {
        initView(context);
        initWindow();
    }

    private void initView(Context context){
        priviewView = LayoutInflater.from(context).inflate(R.layout.back_car_preview_layout, null, false);
        previewImageView = (ImageView)priviewView.findViewById(R.id.preview);

        ((Button)priviewView.findViewById(R.id.start_preview)).setOnClickListener((v -> {
            log.info("发送开启预览广播");
            sendBackCarBroadcast(context,true);
        }));
        ((Button)priviewView.findViewById(R.id.stop_preview)).setOnClickListener((v -> {
            log.info("发送停止预览广播");
            sendBackCarBroadcast(context,false);
        }));
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

//        mWindowManager.addView(priviewView, mLayoutParams);
    }

    public void updatePreviewSize(int width, int height) {
        mLayoutParams.width = width;
        mLayoutParams.height = height;
        mWindowManager.updateViewLayout(priviewView, mLayoutParams);
    }


    public void addToWindow(){
        mWindowManager.addView(priviewView, mLayoutParams);
    }

    public void removeFromWindow(){
        mWindowManager.removeView(priviewView);
    }


    private void sendBackCarBroadcast(Context context, boolean backFlag){
        Intent intent = new Intent(ACTION_ACC_BL_CHANGED);
        intent.putExtra("backed", backFlag);
       context.sendBroadcast(intent);
    }

    public ImageView getPreviewImageView() {
        return previewImageView;
    }
}
