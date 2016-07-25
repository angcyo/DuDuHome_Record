package com.record.control;

import com.dudu.drivevideo.rearcamera.RearCameraManage;
import com.orhanobut.hawk.Hawk;

/**
 * Created by robi on 2016-06-06 17:57.
 */
public class RecordTimeControl {
    public static final String KEY = "time";

    /**
     * 获取录像时长,默认1分钟
     */
    public static int getRecordTime() {
        return Hawk.get(KEY, 1);
    }

    /**
     * 设置录像时长,默认单位为分钟
     */
    public static void setRecordTime(int time) {
        RearCameraManage.getInstance().setRecordTime(time);
        Hawk.put(KEY, time);
    }

    public static void setRecordTime() {
        RearCameraManage.getInstance().setRecordTime(getRecordTime());
    }
}
