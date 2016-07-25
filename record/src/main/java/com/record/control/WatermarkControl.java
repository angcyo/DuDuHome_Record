package com.record.control;

import com.dudu.aios.uvc.Native;
import com.hclydao.webcam.Ffmpeg;
import com.orhanobut.hawk.Hawk;

/**
 * Created by robi on 2016-06-06 17:59.
 */
public class WatermarkControl {
    public static final String KEY = "mark";

    public static boolean isMark() {
        return Hawk.get(KEY, true);//默认开启水印

    }

    /**
     * 设置水印的隐藏
     */
    public static void setMark(boolean mark) {
        Hawk.put(KEY, mark);//是否添加水印
        Native.uvcHideOSD(!mark);//是否隐藏水印
        Ffmpeg.HideOSD(!mark);
    }

    public static void initWatermark() {
        Native.uvcHideOSD(!isMark());//是否隐藏水印
        Ffmpeg.HideOSD(!isMark());
    }

    public static void setMark() {
        Hawk.put(KEY, !isMark());
    }
}
