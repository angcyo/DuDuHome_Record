package com.dudu.drivevideo;

/**
 * Created by robi on 2016-06-07 11:38.
 */
public class CheckSizeEvent {
    boolean isSizeChange = false;//文件大小是否改变

    public CheckSizeEvent(boolean isSizeChange) {
        this.isSizeChange = isSizeChange;
    }

    public boolean isSizeChange() {
        return isSizeChange;
    }
}
