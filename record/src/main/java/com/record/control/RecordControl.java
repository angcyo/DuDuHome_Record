package com.record.control;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;

import com.dudu.aios.uvc.Native;
import com.dudu.commonlib.CommonLib;
import com.dudu.drivevideo.rearcamera.RearCameraManage;
import com.orhanobut.hawk.Hawk;
import com.record.service.RecordService;
import com.record.util.CmdUtil;
import com.record.util.Debug;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by robi on 2016-06-06 16:48.
 */
public class RecordControl {

    public static final String KEY = "record_enable";
    public static final String DOCK_AUDIO_MEDIA_ENABLED = "dock_audio_media_enabled";
    private static boolean initIng = false;

    public static void killDev() {
        Native.uvcPlugRelease();
    }

    /**
     * 是否正在录制
     */
    public static boolean isRecord() {
        return RearCameraManage.getInstance().isRecord();
    }

    /**
     * 启动,关闭录制, 不保存使能状态
     */
    public static void sendRecordNo(Context context, boolean record) {
        if (context == null) {
            return;
        }

        String method = record ? RecordService.START_RECORD_NO : RecordService.STOP_RECORD_NO;
        Intent intent = new Intent("com.record", Uri.parse("record:" + method));
        context.startService(intent);
    }

    /**
     * 启动,关闭录制, 保存使能状态
     */
    public static void sendRecord(Context context, boolean record) {
        if (context == null) {
            return;
        }

        String method = record ? RecordService.START_RECORD : RecordService.STOP_RECORD;
        Intent intent = new Intent("com.record", Uri.parse("record:" + method));
        context.startService(intent);
    }

    /**
     * 启动,关闭录制
     */
    public static void getRecordState(Context context) {
        Intent intent = new Intent("com.record", Uri.parse("record:state"));
        context.startService(intent);
    }

    /**
     * 获取录像是否激活
     */
    public static boolean getRecordEnable() {
        boolean enable = false;
        try {
            enable = Hawk.get(KEY, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return enable;
    }

    /**
     * 是否激活录像
     */
    public static void setRecordEnable(boolean enable) {
        Debug.show(CommonLib.getInstance().getContext(), "激活录像:" + enable, Color.parseColor("#3872F0"));
        Hawk.put(KEY, enable);
    }

    public static void saveRecordState(Context context, int n) {
//        Settings.Global.putInt(context.getContentResolver(), "record", n);
    }

    public static int getSaveRecordState(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "record", -1);
//        return Settings.Global.getInt(context.getContentResolver(), DOCK_AUDIO_MEDIA_ENABLED, -1);
    }

    public static void init(Context context) {
        synchronized (RecordControl.class) {
            if (initIng) {
                return;
            }

            new Thread() {
                @Override
                public void run() {
                    initIng = true;
                    if (!CmdUtil.isAppInstalled(context, "com.record.state")) {
                        String apkFile = "/storage/sdcard0/record_state.apk";
                        copyApkToSd(context, apkFile);
                        CmdUtil.startInstallApk(context, new File(apkFile));
                    }
                    initIng = false;
                }
            }.start();
        }
    }

    private static void copyApkToSd(Context context, String apkFile) {
        InputStream open = null;
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;
        try {
            open = context.getAssets().open("state.apk");
            dataInputStream = new DataInputStream(open);

            File file = new File(apkFile);
            if (file.exists()) {
                file.delete();
            }

            dataOutputStream = new DataOutputStream(new FileOutputStream(file));
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = dataInputStream.read(buffer)) > 0) {
                dataOutputStream.write(buffer, 0, len);
            }
            dataOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                open.close();
            } catch (Exception e) {
            }
            try {
                dataInputStream.close();
            } catch (Exception e) {
            }
            try {
                dataOutputStream.close();
            } catch (Exception e) {
            }
        }
    }
}
