package com.dudu.persistence.realmmodel.picture;

/**
 * Created by dengjun on 2016/4/20.
 * Description :
 */
public class PictureEntity {
    public static final int UPLOAD_NORMAL_STATE = 0;
    public static final int UPLOADING_STATE = 1;
    public static final int UPLOAD_SUCCESS_STATE = 2;
    public static final int UPLOAD_FAIL_STATE = 3;

    private String fileName;
    private String absolutePath;

    private String fileSize;

    private boolean lockFlag;
    private int uploadState;

    private String createTime;
    /* 时间戳*/
    private long timeStamp;

    public int itemPosition = 0;//在Adapter中的位置,用于刷新Recycler

    public PictureEntity() {
    }

    public PictureEntity(PictureEntityRealm pictureEntityRealm) {
        fileName = pictureEntityRealm.getFileName();
        absolutePath = pictureEntityRealm.getAbsolutePath();
        fileSize = pictureEntityRealm.getFileSize();
        lockFlag = pictureEntityRealm.isLockFlag();
        uploadState = pictureEntityRealm.getUploadState();
        createTime = pictureEntityRealm.getCreateTime();
        timeStamp = pictureEntityRealm.getTimeStamp();
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

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
