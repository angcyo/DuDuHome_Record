package com.dudu.drivevideo.frontcamera.camera;

/**
 * Created by dengjun on 2016/5/18.
 * Description :
 */
public enum CameraHandlerMessage {
    INIT_CAMERA(0, "初始化摄像头"),
    RELEASE_CAMERA(1, "释放摄像头"),

    START_RECORD(2, "开启录像"),
    STOP_RECORD(3, "关闭录像"),
    RESET_RECORD(20, "重置录像"),
    RELEASE_RECORD(11, "释放录像"),

    RELEASE_CAMERA_AND_RECORD(21, "释放摄录像和摄像头"),

    TAKE_PICTURE(4, "拍照"),
    START_PREVIEW(5, "开启预览"),
    STOP_PREVIEW(6, "停止预览"),

    SAVE_VIDEO(7, "保存录像"),
    SET_VIDEO_PATH(8, "设置录像文件路径"),
    DELETE_CUR_VIDEO(9, "设置录像文件路径"),
    DELETE_UNUSED_264_VIDEO(10, "删除无用的264视频文件"),


    VIEWGROUP_PREVIEW(32, "主活动预览"),
    WINDOW_PREVIEW(33, "浮窗预览"),
    NO_PREVIEW(34, "没有预览"),

    START_UPLOAD_VIDEO_STREAM(40, "开始上传实时视频"),
    STOP_UPLOAD_VIDEO_STREAM(41, "结束上传实时视频"),
    DESTROY_UPLOAD_VIDEO_STREAM(42, "销毁上传实时视频");

    public int num;
    private String message;

    CameraHandlerMessage(int num, String message) {
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
