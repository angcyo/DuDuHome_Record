package com.record.control;

import com.dudu.drivevideo.utils.RearVideoSaveTools;
import com.orhanobut.hawk.Hawk;

/**
 * Created by robi on 2016-06-14 17:31.
 */
public class ConvertControl {

    public static final String KEY = "convert";
    public static final String KEY_DEL = "convert_delete";


    /**
     * 返回是否自动转换成mp4文件
     */
    public static boolean isConvertMp4() {
        return Hawk.get(KEY, false);
    }

    /**
     * 设置是否自动转换
     */
    public static void setConvertMp4(boolean convert) {
        Hawk.put(KEY, convert);
        RearVideoSaveTools.convertToMp4 = convert;
    }

    public static void init() {
        RearVideoSaveTools.convertToMp4 = isConvertMp4();
        RearVideoSaveTools.deleteH264 = isDeleteH264();
    }

    /**
     * 返回是否自动转换成mp4文件
     */
    public static boolean isDeleteH264() {
        return Hawk.get(KEY_DEL, false);
    }

    /**
     * 设置是否自动转换
     */
    public static void setDeleteH264(boolean delete) {
        Hawk.put(KEY_DEL, delete);
        RearVideoSaveTools.deleteH264 = delete;
    }
}
