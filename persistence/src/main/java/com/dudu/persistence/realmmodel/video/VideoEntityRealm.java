package com.dudu.persistence.realmmodel.video;

import io.realm.RealmObject;

/**
 * Created by dengjun on 2016/4/12.
 * Description :
 */
public class VideoEntityRealm extends RealmObject {
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

    /* 时间戳，用于查询删除最久的视频*/
    private long timeStamp;

    private boolean lockFlag;
    private int uploadState;

    public VideoEntityRealm() {
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
