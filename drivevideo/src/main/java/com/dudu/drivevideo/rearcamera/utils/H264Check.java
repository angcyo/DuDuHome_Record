package com.dudu.drivevideo.rearcamera.utils;

import com.dudu.drivevideo.config.RearVideoConfigParam;
import com.dudu.drivevideo.utils.RearVideoSaveTools;

import java.io.File;

/**
 * Created by robi on 2016-06-12 16:44.
 */
public class H264Check implements Runnable {

    private static boolean isStart = false;

    public static void start() {
        synchronized (H264Check.class) {
            if (isStart) {
                return;
            }
            new Thread(new H264Check()).start();
            isStart = true;
        }
    }

    @Override
    public void run() {
        File file = new File("/storage/sdcard1/_Record/" + RearVideoConfigParam.VIDEO_STORAGE_PATH_TEMP);
        if (!file.exists()) {
            isStart = false;
            return;
        }

        File[] listFiles = file.listFiles();
        for (int i = 0; i < listFiles.length; i++) {
            File listFile = listFiles[i];
            if ((System.currentTimeMillis() - listFile.lastModified()) > 1000 * 60) {
                RearVideoSaveTools.saveCurVideoInfo(listFile.getAbsolutePath());
            }

        }

    }
}
