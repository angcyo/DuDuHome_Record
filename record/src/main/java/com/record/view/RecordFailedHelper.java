package com.record.view;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.record.service.RecordService;

/**
 * Created by robi on 2016-07-15 15:40.
 */
public class RecordFailedHelper {

    private String curRecordFile = "";//当前录制的文件全路径
    private boolean isShow = false;//当前录制的文件,是否显示过异常提示

    private RecordFailedHelper() {
    }

    public static RecordFailedHelper instance() {
        return Holder.helper;
    }

    public static void showFailedDialog(Context context) {
        new AlertDialog.Builder(context).setTitle("行车记录").setMessage("录像异常,请检查.").setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                RecordService.startMainActivity(context);
            }
        }).create().show();
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
        this.curRecordFile = curRecordFile;
    }

    static class Holder {
        static RecordFailedHelper helper = new RecordFailedHelper();
    }
}
