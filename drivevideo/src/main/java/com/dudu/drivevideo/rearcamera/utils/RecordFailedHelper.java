package com.dudu.drivevideo.rearcamera.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.dudu.drivevideo.MsgEvent;

import java.io.File;

import de.greenrobot.event.EventBus;


/**
 * Created by robi on 2016-07-15 15:40.
 */
public class RecordFailedHelper {

    private String curRecordFile = "";//当前录制的文件全路径
    private boolean isShow = false;//当前录制的文件,是否显示过异常提示
    private long lastSize = 0l;//最后记录的文件大小,5秒之内大小没有改变,提示异常

    private RecordFailedHelper() {
    }

    public static RecordFailedHelper instance() {
        return Holder.helper;
    }

    public static void showFailedDialog(Context context) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context.getApplicationContext())
                .setTitle("行车记录")
                .setMessage("录像异常,请检查.")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        try {
                            Class main = Class.forName("com.record.MainActivity");
                            final Intent intent = new Intent(context, main);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            context.startActivity(intent);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }).create();
        final Window alertDialogWindow = alertDialog.getWindow();
        alertDialogWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialogWindow.setType(WindowManager.LayoutParams.TYPE_TOAST);
        final WindowManager.LayoutParams attributes = alertDialogWindow.getAttributes();
        attributes.gravity = Gravity.TOP | Gravity.RIGHT;
        attributes.width = 300;
        attributes.height = 100;
        attributes.format = PixelFormat.RGBA_8888;
        attributes.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN// 覆盖状态栏
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL//窗口外可以点击
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE//不监听按键事件
//                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
//                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS//突破窗口限制
//                    | WindowManager.LayoutParams.FLAG_FULLSCREEN
        ;
        alertDialogWindow.setAttributes(attributes);
        alertDialog.show();
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }

    public String getCurRecordFile() {
        return curRecordFile;
    }

    public void setCurRecordFile(String curRecordFile) {
        EventBus.getDefault().post(new MsgEvent("准备检查大小的文件:" + curRecordFile));
        this.curRecordFile = curRecordFile;
        lastSize = new File(curRecordFile).length();
        isShow = false;
        EventBus.getDefault().post(new MsgEvent("当前文件大小:" + lastSize));
    }

    /**
     * 检查大小是否改变
     */
    public boolean checkSizeChange() {
        long size = getFileSize(curRecordFile);
        if (size == -1) {
            return true;
        }
        if (size == lastSize) {
            return false;
        }

        lastSize = size;
        return true;
    }

    public static long getFileSize(String filePath) {
        final File file = new File(filePath);
        if (!file.exists()) {
            return -1;
        }

        return file.length();
    }

    static class Holder {
        static RecordFailedHelper helper = new RecordFailedHelper();
    }
}
