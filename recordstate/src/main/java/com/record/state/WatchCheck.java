package com.record.state;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * 守护进程检查,自动启动录像服务
 * <p>
 * Created by robi on 2016-06-19 00:52.
 */
public class WatchCheck extends Thread {
    private static WatchCheck sWatchCheck = new WatchCheck();
    private static boolean isRun = false;
    private static Context sContext;

    public synchronized static void s(Context context) {
        if (isRun) {
            return;
        }

        sContext = context.getApplicationContext();
        sWatchCheck.start();
        isRun = true;
    }

    @Override
    public void run() {
        while (true) {
            if (sContext != null) {
                final boolean processRunning = CmdUtil.isProcessRunning(sContext, "com.record");
                if (!processRunning) {

                    final int saveRecordState = RecordControl.getSaveRecordState(sContext);
                    Intent intent;
                    if (saveRecordState == 1) {
                        intent = new Intent("com.record", Uri.parse("record:start"));
                    } else {
                        intent = new Intent("com.record", Uri.parse("record:stop"));
                    }
                    sContext.startService(intent);
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
