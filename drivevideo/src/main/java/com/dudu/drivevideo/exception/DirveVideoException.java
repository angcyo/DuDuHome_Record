package com.dudu.drivevideo.exception;


/**
 * Created by dengjun on 2016/4/27.
 * Description :
 */
public class DirveVideoException extends Exception{
    CameraErrorCode errorCode;
    public DirveVideoException(CameraErrorCode errorCode, Throwable throwable) {
            super(errorCode.getDetailErrMsg(), throwable);
            this.errorCode = errorCode;
    }

    /**
     * 获取错误编码
     * @return
     */
    public CameraErrorCode getErrorCode() {
        return errorCode;
    }

    public DirveVideoException(CameraErrorCode errorCode) {
        super(errorCode.getDetailErrMsg());
        this.errorCode = errorCode;
    }
}
