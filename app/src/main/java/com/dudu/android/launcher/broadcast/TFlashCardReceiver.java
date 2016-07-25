package com.dudu.android.launcher.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dudu.android.launcher.utils.AgedUtils;
import com.dudu.android.launcher.utils.Utils;
import com.dudu.commonlib.resource.sdcard.SdcardManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 赵圣琪 on 2016/1/3.
 */
public class TFlashCardReceiver extends BroadcastReceiver {
    private Logger log;

    public TFlashCardReceiver() {
        log = LoggerFactory.getLogger("init.receiver.t-flash");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        log.debug("TFlashCardReceiver onReceive mount:{} action:{}", intent.getExtras(), action);
        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            if (Utils.isDemoVersion(context)) {
                AgedUtils.proceedAgeTest(context);
            }
            SdcardManager.instance().notifyEvent(true);
        } else if (action.equals(Intent.ACTION_MEDIA_REMOVED)) {
            SdcardManager.instance().notifyEvent(false);
        } else if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_EJECT)) {
            SdcardManager.instance().notifyEvent(false);
        }
    }

}
