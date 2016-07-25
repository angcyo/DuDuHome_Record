package com.dudu.drivevideo.rearcamera.camera;

import com.dudu.aios.uvc.Native;
import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.drivevideo.config.RearVideoConfigParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by dengjun on 2016/5/18.
 * Description :
 */
public class RearCameraRecorder {
    private RearVideoConfigParam rearVideoConfigParam;
    private String curVideoPath;

    private RearCameraListener rearCameraListener;
    private Subscription recordSubscription;

    private boolean recordEnable = false;
    /* 标记是否在录像*/
    private boolean isRecording = false;

    private Logger log = LoggerFactory.getLogger("video.reardrivevideo");

    public RearCameraRecorder(RearVideoConfigParam rearVideoConfigParam) {
        this.rearVideoConfigParam = rearVideoConfigParam;
    }


    public void startRecord() {
        if (isRecording == true) {
            log.info("后置正在录像，不重复开启");
            return;
        }
        new Thread(() -> {
            log.info("开启后置录像:{}", "/dev/" + rearVideoConfigParam.getVideoRecordDevice());
            isRecording = true;
            int ret = Native.uvcAP("/dev/" + rearVideoConfigParam.getVideoRecordDevice());
            isRecording = false;
            log.info("UVC AP 录像结束 Exit:{}", ret);
            preStartRecordReturnValue(ret);
        }).start();
    }

    private void preStartRecordReturnValue(int ret) {
        if (ret == 1) {
            if (rearCameraListener != null) {
                rearCameraListener.onError(RearCameraListenerMessage.RECORD_ERROR);
            }
        }
    }

    public void stopRecord() {
        log.info("停止后置录像");
        cancerSendVideoFinishMessage();
        Native.uvcStop();
    }

    private void cancerSendVideoFinishMessage() {
        if (recordSubscription != null && !recordSubscription.isUnsubscribed()) {
            recordSubscription.unsubscribe();
        }
    }

    public void setRearCameraListener(RearCameraListener rearCameraListener) {
        this.rearCameraListener = rearCameraListener;
    }

    public String getCurVideoPath() {
        return curVideoPath;
    }

    public void setCurVideoPath(String videoPath) {
        curVideoPath = videoPath;
        log.info("当前录像文件路径：{} , 录像时长: {}", curVideoPath, rearVideoConfigParam.getRecordTime());
        byte[] fn = curVideoPath.getBytes();
        Native.uvcSetFileName(fn, fn.length);

        delaySendVideoFinishMessage();
    }

    private void delaySendVideoFinishMessage() {
        recordSubscription =
                Observable
                        .timer(rearVideoConfigParam.getRecordTime(), TimeUnit.MINUTES)
                        .subscribeOn(Schedulers.newThread())
                        .subscribe(l -> {
                            if (rearCameraListener != null) {
                                rearCameraListener.onInfo(RearCameraListenerMessage.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED);
                            }
                        }, throwable -> {
                            log.error("异常", throwable);
                        });
    }

    public boolean detectCamera() {
        return FileUtil.detectFileExist("/dev/" + rearVideoConfigParam.getVideoRecordDevice());
    }

    public boolean isRecordEnable() {
        return recordEnable;
    }

    public void setRecordEnable(boolean recordEnable) {
        this.recordEnable = recordEnable;
    }

    public boolean isRecording() {
        return isRecording;
    }
}
