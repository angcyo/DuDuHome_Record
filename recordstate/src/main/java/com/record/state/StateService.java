package com.record.state;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class StateService extends Service {
    public static final String STOP_RECORD = "stop";
    public static final String START_RECORD = "start";
    public static final String ERROR_RECORD = "SD Card Not Found";
    public static final String ERROR_NO_DEVICE = "Device Not Found";

    public StateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //开启守护
        WatchCheck.s(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String dataString = intent.getDataString();
        String info = dataString.split("info:")[1];
//        T.show(this, "收到信息:" + info);
        if (info.contains(START_RECORD)) {
            //开始录像了
            RecordControl.saveRecordState(this, 1);
        } else if (info.contains(STOP_RECORD)) {
            //录像停止了
            RecordControl.saveRecordState(this, 0);
        } else if (info.contains(ERROR_RECORD)) {
            //录像出错
            RecordControl.saveRecordState(this, 0);
        } else if (info.contains(ERROR_NO_DEVICE)) {
            //设备没有找到
            RecordControl.saveRecordState(this, 0);
        }
//        T.show(this, "state:" + RecordControl.getSaveRecordState(this));
        return START_STICKY;
    }
}
