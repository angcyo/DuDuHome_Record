package com.record.event;

/**
 * Created by robi on 2016-06-12 15:31.
 */
public class SdEvent {
    public static final int STATE_NO = 0;
    public static final int STATE_OK = 1;

    private int state;

    public SdEvent(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
