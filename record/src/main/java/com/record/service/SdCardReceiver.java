package com.record.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.record.event.SdEvent;

import de.greenrobot.event.EventBus;

public class SdCardReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
//            开启启动后台录像服务 和 磁盘管理服务..
//            Intent serviceIntent = new Intent(context, BackgroundRecordService.class);
//            context.startService(serviceIntent);
//            RecordControl.sendRecord(context, true);//不要在T卡广播中开启录像,,会出现开机无法预览的BUG
            EventBus.getDefault().post(new SdEvent(SdEvent.STATE_OK));
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)) {
//            RecordControl.sendRecord(context, false);
            EventBus.getDefault().post(new SdEvent(SdEvent.STATE_NO));
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)) {
//            RecordControl.sendRecord(context, false);
            EventBus.getDefault().post(new SdEvent(SdEvent.STATE_NO));
        }
    }
}
