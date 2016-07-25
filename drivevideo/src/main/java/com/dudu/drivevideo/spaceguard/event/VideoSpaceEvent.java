package com.dudu.drivevideo.spaceguard.event;

/**
 * Created by dengjun on 2016/5/24.
 * Description :
 */
public class VideoSpaceEvent {
    public static final String driveRecordHaveNoStorageSpace = "存储卡空间不足，将无法录像";

    private String mesageToSpeak;

    public VideoSpaceEvent(String mesageToSpeak) {
        this.mesageToSpeak = mesageToSpeak;
    }

    public String getMesageToSpeak() {
        return mesageToSpeak;
    }

    public void setMesageToSpeak(String mesageToSpeak) {
        this.mesageToSpeak = mesageToSpeak;
    }
}
