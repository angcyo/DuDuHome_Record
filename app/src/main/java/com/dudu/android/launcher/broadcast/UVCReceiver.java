package com.dudu.android.launcher.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dudu.drivevideo.config.RearVideoConfigParam;
import com.dudu.drivevideo.rearcamera.RearCameraManage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UVCReceiver extends BroadcastReceiver {
    private final static String ACTION_UVC_CHANGED = "android.intent.action.UVC";

    private Logger log;
    private static int videoCount = 0;

    public UVCReceiver() {
        log = LoggerFactory.getLogger("video.reardrivevideo");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_UVC_CHANGED)) {
            String action = intent.getStringExtra("ACTION");
            String dev_name = intent.getStringExtra("DEVNAME");
            log.debug("收到UVCReceiver DEV广播:{}->Action:{}", dev_name, action);
            if ("add".equals(action)){
                if (videoCount == 0){
                    RearCameraManage.getInstance().getRearVideoConfigParam().setVideoDevice(dev_name);
                    RearCameraManage.getInstance().setPreviewEnable(true);
//                    RearCameraManage.getInstance().openCamera();//收到消息不打开camera，开启预览的时候才去打开
                    if(ACCReceiver.isBackCarIng == true){
                        log.info("正在倒车，恢复预览-------------");
                        RearCameraManage.getInstance().stopPreview();
                        RearCameraManage.getInstance().startDrivingPreview();
                    }

                    videoCount++;
                } else if (videoCount == 1) {
                    RearCameraManage.getInstance().getRearVideoConfigParam().setVideoRecordDevice(dev_name);
                    videoCount--;
                    RearCameraManage.getInstance().startRecord();//如果已经使能了开启录制，没有则实际不会开启录像
                }
            }else if ("remove".equals(action)){
                if (RearVideoConfigParam.DEFAULT_VIDEO_DEVICE.equals(dev_name)){
                    log.info("停止预览 dev_name：{}", dev_name);
                    RearCameraManage.getInstance().setPreviewEnable(false);
                    RearCameraManage.getInstance().stopPreview();
                } else if (RearVideoConfigParam.DEFAULT_VIDEO_RECORD_DEVICE.equals(dev_name)) {
                    log.info("停止录像 dev_name：{}", dev_name);
                    RearCameraManage.getInstance().stopRecord();
                }
              /*  RearCameraManage.getInstance().setPreviewEnable(false);
                RearCameraManage.getInstance().closeCamera();
                RearCameraManage.getInstance().stopRecord();*/
            }
        }
    }

    private void sendBackCarBroadcast(Context context, boolean backFlag){
        Intent intent = new Intent("android.intent.action.ACC_BL");
        intent.putExtra("backed", backFlag);
        context.sendBroadcast(intent);
    }
}
