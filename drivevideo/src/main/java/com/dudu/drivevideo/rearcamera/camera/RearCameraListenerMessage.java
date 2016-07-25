package com.dudu.drivevideo.rearcamera.camera;

/**
 * Created by dengjun on 2016/5/19.
 * Description :
 */
public enum RearCameraListenerMessage {
    CAMERA_ERROR(0, "摄像头错误"),
    MEDIA_RECORDER_INFO_MAX_DURATION_REACHED(2, "当前录制时间到"),
    PREVIEW_ERROR(3,"预览错误"),
    PREVIEW_FINISH(4, "预览结束"),
    RECORD_ERROR(5,"录像出错");


    public int  what;
    public String message;

    RearCameraListenerMessage(int what, String message) {
        this.what = what;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getWhat() {
        return what;
    }

    public void setWhat(int what) {
        this.what = what;
    }
}
