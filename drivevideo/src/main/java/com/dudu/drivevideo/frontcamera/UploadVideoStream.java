package com.dudu.drivevideo.frontcamera;

import com.dudu.commonlib.CommonLib;
import com.dudu.drivevideo.frontcamera.camera.FrontCamera;
import com.dudu.drivevideo.frontcamera.event.StreamEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;
import me.lake.librestreaming.client.RESClient;
import me.lake.librestreaming.core.listener.RESConnectionListener;
import me.lake.librestreaming.model.RESConfig;

/**
 * Created by dengjun on 2016/5/23.
 * Description :
 */
public class UploadVideoStream implements RESConnectionListener {
    Logger log = LoggerFactory.getLogger("video.frontdrivevideo");
    private RESClient resClient;
    private RESConfig resConfig;
    private FrontCamera frontCamera;
    private String rtmp = "rtmp://182.254.227.45/myapp/" + CommonLib.getInstance().getObeId();
    private boolean isStart = false;

    public UploadVideoStream(FrontCamera frontCamera) {
        this.frontCamera = frontCamera;
        resClient = new RESClient();

        resConfig = RESConfig.obtain();
        resConfig.setRtmpAddr(rtmp);
    }

    public void startUploadVideoStream() {
        if (frontCamera != null && frontCamera.getCamera() != null && !isStart) {
            resClient.setCamera(frontCamera.getCamera());
            if (!resClient.prepare(resConfig)) {
                log.error("aa", "prepare,failed!!");
            }
            resClient.setConnectionListener(this);

            log.info("即将连接至:{}", rtmp);
            resClient.start();
            isStart = true;
        }
    }

    public void stopUploadVideoStream() {
        EventBus.getDefault().post(new StreamEvent(StreamEvent.STOP));

        if (!isStart) {
            return;
        }
        log.info("即将断开连接:{}", rtmp);
        try {
            resClient.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        isStart = false;
//        resClient.destroy();?
    }

    public void destroyUploadVideoStream() {
        if (isStart) {
            if (resClient != null) {
                resClient.stop();
                log.info("即将销毁连接:{}", rtmp);
                resClient.destroy();
            }
            isStart = false;
        }
    }

    @Override
    public void onOpenConnectionResult(int result) {
        if (result == 0) {
            log.info("连接成功:{}", rtmp);
            EventBus.getDefault().post(new StreamEvent(StreamEvent.START));
        } else {
            log.info("连接失败:{}", rtmp);
            EventBus.getDefault().post(new StreamEvent(StreamEvent.STOP));
        }
    }

    @Override
    public void onWriteError(int error) {
        log.error("writeError = " + error);
    }

    @Override
    public void onCloseConnectionResult(int result) {
        EventBus.getDefault().post(new StreamEvent(StreamEvent.STOP));
        if (result == 0) {
            log.info("断开成功:{}", rtmp);
        } else {
            log.info("断开失败:{}", rtmp);
        }
    }
}
