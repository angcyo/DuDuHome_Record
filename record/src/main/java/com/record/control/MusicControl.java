package com.record.control;

import com.orhanobut.hawk.Hawk;

/**
 * Created by robi on 2016-06-06 16:27.
 */
public class MusicControl {

    public static final String KEY = "mute";

    public static MusicControl instance() {
        return Holder.control;
    }

    /**
     * 是否开启声效
     */
    public boolean isMusic() {
        return Hawk.get(KEY, true);
    }

    /**
     * 设置声效
     */
    public void setMusic(boolean mute) {
        Hawk.put(KEY, mute);
    }

    /**
     * 设置静音
     */
    public void setMusic() {
        Hawk.put(KEY, !isMusic());
    }

    static class Holder {
        static final MusicControl control = new MusicControl();
    }
}
