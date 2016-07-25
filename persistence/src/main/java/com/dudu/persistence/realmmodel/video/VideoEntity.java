package com.dudu.persistence.realmmodel.video;

/**
 * Created by dengjun on 2016/4/12.
 * Description :
 */
public class VideoEntity{
    public static final int UPLOAD_NORMAL_STATE = 0;
    public static final int UPLOADING_STATE = 1;
    public static final int UPLOAD_SUCCESS_STATE = 2;
    public static final int UPLOAD_FAIL_STATE = 3;


    /* 用于区分前后置摄像头 为true表示前置摄像头， false表示后置USB摄像头*/
    private boolean cameraFlag;

    private String fileName;
    /* 录像文件绝对路径*/
    private String absolutePath;
    /* 录像文件大小*/
    private String fileSize;
    /* 录像文件缩略图绝对路径*/
    private String thumbnailAbsolutePath;
    /* 录像文件缩略图大小*/
    private String thumbnailFileSize;

    private String createTime;

    /* 时间戳*/
    private long timeStamp;

    private boolean lockFlag;
    private int uploadState;

    private int itemPosition;

    public int getItemPosition() {
        return itemPosition;
    }

    public void setItemPosition(int itemPosition) {
        this.itemPosition = itemPosition;
    }

    public VideoEntity(VideoEntityRealm videoEntityRealm) {
        cameraFlag = videoEntityRealm.isCameraFlag();
        fileName = videoEntityRealm.getFileName();
        absolutePath = videoEntityRealm.getAbsolutePath();
        createTime = videoEntityRealm.getCreateTime();
        timeStamp = videoEntityRealm.getTimeStamp();
        fileSize = videoEntityRealm.getFileSize();
        lockFlag = videoEntityRealm.isLockFlag();
        uploadState = videoEntityRealm.getUploadState();
        thumbnailAbsolutePath = videoEntityRealm.getThumbnailAbsolutePath();
        thumbnailFileSize = videoEntityRealm.getThumbnailFileSize();
    }

    public boolean isCameraFlag() {
        return cameraFlag;
    }

    public void setCameraFlag(boolean cameraFlag) {
        this.cameraFlag = cameraFlag;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getCreateTime() {
        return createTime;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isLockFlag() {
        return lockFlag;
    }

    public void setLockFlag(boolean lockFlag) {
        this.lockFlag = lockFlag;
    }

    public int getUploadState() {
        return uploadState;
    }

    public void setUploadState(int uploadState) {
        this.uploadState = uploadState;
    }

    public String getThumbnailAbsolutePath() {
        return thumbnailAbsolutePath;
    }

    public void setThumbnailAbsolutePath(String thumbnailAbsolutePath) {
        this.thumbnailAbsolutePath = thumbnailAbsolutePath;
    }

    public String getThumbnailFileSize() {
        return thumbnailFileSize;
    }

    public void setThumbnailFileSize(String thumbnailFileSize) {
        this.thumbnailFileSize = thumbnailFileSize;
    }
}
