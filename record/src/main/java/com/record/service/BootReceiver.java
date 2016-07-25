package com.record.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.record.control.RecordControl;

public class BootReceiver extends BroadcastReceiver {

    static final String action_boot = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
//        //开启启动后台录像服务 和 磁盘管理服务..
//        Intent serviceIntent = new Intent(context, BackgroundRecordService.class);
//        context.startService(serviceIntent);

        if (intent.getAction().equals(action_boot)) {
//            Log.e("angcyo", "开机广播 " + Settings.Global.getInt(context.getContentResolver(), "record", -1));
//            FileUtil.writeToFile("开机广播 " + Settings.Global.getInt(context.getContentResolver(), "record", -1) + "\n", "/storage/sdcard1/angcyo_log.txt", true);
            if (RecordControl.getRecordEnable()) {
                RecordControl.sendRecord(context, true);
            }
        }
    }
}
