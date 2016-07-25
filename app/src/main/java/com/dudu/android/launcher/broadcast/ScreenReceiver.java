package com.dudu.android.launcher.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dudu.init.InitManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2016/5/20.
 */
public class ScreenReceiver extends BroadcastReceiver {

    Logger logger = LoggerFactory.getLogger("video.ScreenReceiver");

    @Override
    public void onReceive(Context context, Intent intent) {
        logger.debug("onReceive");
        String action = intent.getAction();
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            logger.debug("screen on");
            InitManager.screenOnControl();
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            logger.debug("screen off");
            InitManager.getInstance().screenOffControl();
        } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
            logger.debug("screen unlock");
        }
    }
}
