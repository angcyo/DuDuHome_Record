package com.record.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;
import com.record.control.RecordControl;
import com.record.util.Constant;

public class BackgroundRecordService extends Service {
    public BackgroundRecordService() {
    }

    /**
     * 是否启动录像
     *
     * @return
     */
    public static boolean isBootRecord() {
        return SharedPreferencesUtil.getBooleanValue(CommonLib.getInstance().getContext(), Constant.KEY_IS_START_RECORD, false);
    }

    @Override
    public void onCreate() {
        super.onCreate();

//        开启磁盘空间管理服务.
//        StorageServiceManager.getInstance().startStorageService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        RecordControl.sendRecord(this, true);//开启录像,并发送录像状态,第三方APP可以接受这个状态
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
