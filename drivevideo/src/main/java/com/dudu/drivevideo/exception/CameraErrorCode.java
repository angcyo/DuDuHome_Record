package com.dudu.drivevideo.exception;

/**
 * Created by dengjun on 2016/4/27.
 * Description :
 */
public enum CameraErrorCode {
    OPEN_CAMERA_ERROR("打开摄像头错误"),
    CLOSE_CAMERA_ERROR("关闭摄像头失败"),

    START_PREVIEW_ERROR("开始相机预览错误"),
    STOP_PREVIEW_ERROR("关闭预览失败"),

    START_PREVIEW_SURFACE_NULL_ERROR("开始相机预览surface 空指针错误"),

    START_RECORD_ERROR("开始录像失败"),
    STOP_RECORD_ERROR("停止录像失败")
    ;

    private String errMsg;

    CameraErrorCode(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getDetailErrMsg() {
        return errMsg;
    }
}