package com.dudu.drivevideo.rearcamera.utils;

import com.dudu.drivevideo.MsgEvent;
import com.dudu.drivevideo.rearcamera.RearCameraHandler;
import com.dudu.drivevideo.utils.RearVideoSaveTools;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by robi on 2016-06-12 16:44.
 */
public class TempFileCheck implements Runnable {

    public static void start() {
        new Thread(new TempFileCheck()).start();
    }

    @Override
    public void run() {
        final String videoPath = RearCameraHandler.curVideoPath;
        File file = new File(videoPath);
        EventBus.getDefault().post(new MsgEvent("检查临时文件:" + videoPath));
        if (file.exists()) {
            EventBus.getDefault().post(new MsgEvent("保存临时文件:" + videoPath));
            RearVideoSaveTools.saveCurVideoInfo(videoPath);
        }
    }
}
