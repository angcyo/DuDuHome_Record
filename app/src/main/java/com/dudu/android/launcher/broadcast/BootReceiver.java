package com.dudu.android.launcher.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dudu.android.launcher.utils.Utils;
import com.dudu.drivevideo.utils.UsbControl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootReceiver extends BroadcastReceiver {
    private final static String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";

    private Logger log;

    public BootReceiver() {
        log = LoggerFactory.getLogger("init.receiver.boot");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_BOOT_COMPLETED)) {
            log.debug("onReceive boot completed:{}", intent.getExtras());

            if (!Utils.isDemoVersion(context)) {
                LoggerFactory.getLogger("video.reardrivevideo").info("启动完毕，设置usb为host");
                UsbControl.setToHost();
            }
        }
    }
}
