package com.dudu.drivevideo.frontcamera.event;

/**
 * Created by robi on 2016/5/25.
 */
public class StreamEvent {
    public static final int START = 1;
    public static final int STOP = 0;

    private int state;

    public StreamEvent(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
