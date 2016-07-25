package com.dudu.drivevideo.frontcamera.event;

/**
 * Created by dengjun on 2016/5/24.
 * Description :
 */
public class VideoEvent {
    public static final int ON = 1;
    public static final int OFF = 0;

    private int state;

    public VideoEvent(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
