package com.record.util;

import com.dudu.drivevideo.config.RearVideoConfigParam;

import java.io.File;

/**
 * Created by robi on 2016-05-07 14:50.
 */
public class RUtil {
    public static boolean canPreview() {
//        File file = new File("/dev/video1");
        File file = new File("/dev/" + RearVideoConfigParam.DEFAULT_VIDEO_DEVICE);
        return file.exists();
    }

    public static boolean canRecord() {
//        File file2 = new File("/dev/video2");
        boolean result = false;
        String device2 = RearVideoConfigParam.DEFAULT_VIDEO_RECORD_DEVICE;
        String device3 = "video3";
        String device4 = "video4";
        File file2 = new File("/dev/" + device2);
        File file3 = new File("/dev/" + device3);
        File file4 = new File("/dev/" + device4);
        if (file2.exists()) {
            RearVideoConfigParam.VIDEO_RECORD_DEVICE = device2;
            result = true;
        }
//        else if (file3.exists()) {
//            RearVideoConfigParam.VIDEO_RECORD_DEVICE = device3;
//            result = true;
//        } else if (file4.exists()) {
//            RearVideoConfigParam.VIDEO_RECORD_DEVICE = device4;
//            result = true;
//        }

        return result;
    }

    public static boolean failFlag() {
        String device3 = "video3";
        String device4 = "video4";
        File file3 = new File("/dev/" + device3);
        File file4 = new File("/dev/" + device4);

        if (file3.exists() || file4.exists()) {
            return true;
        }

        return false;
    }
}
