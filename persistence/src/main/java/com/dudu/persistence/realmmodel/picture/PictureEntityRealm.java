package com.dudu.persistence.realmmodel.picture;

import io.realm.RealmObject;

/**
 * Created by dengjun on 2016/4/12.
 * Description :
 */
public class PictureEntityRealm extends RealmObject {
    /* 用于区分前后置摄像头 为true表示前置摄像头， false表示后置USB摄像头*/
    private boolean cameraFlag;

    private String fileName;
    private String absolutePath;

    private String fileSize;

    private boolean lockFlag;
    private int uploadState;

    private String createTime;
    /* 时间戳*/
    private long timeStamp;

    public PictureEntityRealm() {
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

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getUploadState() {
        return uploadState;
    }

    public void setUploadState(int uploadState) {
        this.uploadState = uploadState;
    }
}
