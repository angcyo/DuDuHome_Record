package com.dudu.drivevideo.config;

import android.media.CamcorderProfile;

/**
 * Created by dengjun on 2016/1/26.
 * Description :
 */
public class RearVideoConfigParam {
    public static final String REAR_VIDEO_STORAGE_PATH = "/storage/sdcard1/dudu/rearVideo";
    public static final String VIDEO_STORAGE_PATH = "/Video";
    public static final String VIDEO_STORAGE_PATH_TEMP = "/temp";

    public static final String VIDEO_STORAGE_RAW_PATH_H264 = "/mp4raw";

    public static final String REAR_VIDEO_THUMBNAIL_STORAGE_PATH = "/storage/sdcard1/dudu/rearVideoThumbnail";
    public static final String VIDEO_THUMBNAIL_STORAGE_PATH = "/rearVideoThumbnail";
    public static final String VIDEO_PICTURE_STORAGE_PATH = "/rearPicture";


    /* 默认录像间隔*/
    public static final int DEFAULT_VIDEO_INTERVAL = 1 * 60 * 1000;

    /**
     * 默认录像间隔时间, 分钟
     */
    public static final int DEFAULT_VIDEO_RECORD_TIME = 1;

    public static final int DEFAULT_VIDEOBITRATE = 2 * 1024 * 1024;//2M

    public static final int DEFAULT_WIDTH = 1280;
    public static final int DEFAULT_HEIGHT = 720;

    public static final int DEFAULT_D_WIDTH = 640;
    public static final int DEFAULT_D_HEIGHT = 480;

    public static final int DEFAULT_RATE = 18;
    public static final int DEFAULT_QUALITY = CamcorderProfile.QUALITY_HIGH;

    public static final String DEFAULT_VIDEO_DEVICE = "video1";
    public static final String DEFAULT_VIDEO_RECORD_DEVICE = "video2";

    public static String VIDEO_RECORD_DEVICE = "video2";

    private int videoInterval;
    /*video output bit rate */
    private int videoBitRate;
    /* */
    private int width;
    /* */
    private int height;

    private int dwidth;
    private int dheight;

    /* */
    private int rate;
    /* */
    private int quality;

    private String videoDevice;

    private String videoRecordDevice;

    private int recordTime;

    public RearVideoConfigParam() {
        videoInterval = DEFAULT_VIDEO_INTERVAL;
        videoBitRate = DEFAULT_VIDEOBITRATE;
        width = DEFAULT_WIDTH;
        height = DEFAULT_HEIGHT;
        dwidth = DEFAULT_D_WIDTH;
        dheight = DEFAULT_D_HEIGHT;
        rate = DEFAULT_RATE;
        quality = DEFAULT_QUALITY;

        videoDevice = DEFAULT_VIDEO_DEVICE;
        videoRecordDevice = DEFAULT_VIDEO_RECORD_DEVICE;

        recordTime = DEFAULT_VIDEO_RECORD_TIME;
    }

    public int getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(int recordTime) {
        this.recordTime = recordTime;
    }

    public int getVideoInterval() {
        return videoInterval;
    }

    public void setVideoInterval(int videoInterval) {
        this.videoInterval = videoInterval;
    }

    public int getVideoBitRate() {
        return videoBitRate;
    }

    public void setVideoBitRate(int videoBitRate) {
        this.videoBitRate = videoBitRate;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getDheight() {
        return dheight;
    }

    public void setDheight(int dheight) {
        this.dheight = dheight;
    }

    public int getDwidth() {
        return dwidth;
    }

    public void setDwidth(int dwidth) {
        this.dwidth = dwidth;
    }

    public String getVideoDevice() {
        return videoDevice;
    }

    public void setVideoDevice(String videoDevice) {
        this.videoDevice = videoDevice;
    }


    public String getVideoRecordDevice() {
        return videoRecordDevice;
//        return VIDEO_RECORD_DEVICE;
    }

    public void setVideoRecordDevice(String videoRecordDevice) {
        this.videoRecordDevice = videoRecordDevice;
    }
}
