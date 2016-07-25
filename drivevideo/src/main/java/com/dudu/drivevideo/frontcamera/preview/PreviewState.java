package com.dudu.drivevideo.frontcamera.preview;

/**
 * Created by dengjun on 2016/5/21.
 * Description :
 */
public enum  PreviewState {
    MAINACITITY_PREVIEW(12, "主活动预览"),
    WINDOW_PREVIEW(13, "浮窗预览"),
    NO_PREVIEW(14, "无预览");


    public int num;
    private String message;

    PreviewState(int num, String message) {
        this.num = num;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
