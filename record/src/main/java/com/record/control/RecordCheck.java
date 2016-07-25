package com.record.control;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.record.MainActivity;
import com.record.event.RecordFailedEvent;
import com.record.util.Debug;
import com.record.util.RUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by robi on 2016-06-19 00:52.
 */
public class RecordCheck extends Handler {

    public static final int CHECK = 100;
    public static final long delayMillis = 2000;
    private static final String TAG = "RecordCheck";
    private static RecordCheck sRecordCheck;
    private static HandlerThread sHandlerThread;
    private boolean isSendStart = false;
    private boolean isSendStop = false;
    private IRecordListener mRecordListener;
    private Context mContext;

    public RecordCheck(Looper looper) {
        super(looper);
    }

    public static RecordCheck instance(final Context context) {
        if (sRecordCheck == null) {
            synchronized (RecordCheck.class) {
                if (sRecordCheck == null) {
                    sHandlerThread = new HandlerThread("") {
                        @Override
                        protected void onLooperPrepared() {
                            sRecordCheck = new RecordCheck(sHandlerThread.getLooper());
                            sRecordCheck.setContext(context.getApplicationContext());
                            sRecordCheck.checkRecord();
                        }
                    };
                    sHandlerThread.start();
                }
            }
        }

        return sRecordCheck;
    }

    public void quit() {
        this.removeMessages(CHECK);
        sHandlerThread.quit();
        sHandlerThread = null;
        sRecordCheck = null;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void setRecordListener(IRecordListener recordListener) {
        mRecordListener = recordListener;
    }

    @Override
    public void handleMessage(Message msg) {
//        if (msg.what == CHECK) {
        if (msg.what == CHECK) {
            Debug.show(mContext, MainActivity.getDevs());

            if (RUtil.canRecord()) {
                if (mRecordListener != null) {
                    mRecordListener.onRecordOk();
                }
                if (!isSendStart) {
                    if (RecordControl.getRecordEnable()) {
                        isSendStart = true;
                        Log.i(TAG, "handleMessage: 检查到录像节点,开始录像.");
                        Debug.show(mContext, "检查到录像节点,开始录像.");
                        Debug.show(mContext, MainActivity.getDevs());
                        RecordControl.sendRecord(mContext, true);
                    }
                }
                isSendStop = false;
            } else {
                if (mRecordListener != null) {
                    mRecordListener.onRecordFail();
                }

                if (!isSendStop) {
                    isSendStop = true;
                    Log.i(TAG, "handleMessage: 无设备节点,停止录像.");
                    Debug.show(mContext, "无设备节点,停止录像.");
//                    RecordControl.sendRecord(mContext, false);
//                    RecordControl.killDev();
//                    try {
//                        Thread.sleep(800);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    if (BuildConfig.DEBUG) {
//                        DebugWindow.instance(mContext).addText("无设备节点,检查残余文件.");
//                    }
////                    H264Check.start();
                    EventBus.getDefault().post(new RecordFailedEvent());
                }
                isSendStart = false;
            }

            if (mRecordListener != null) {
                mRecordListener.onCheckEnd();
            }

            checkRecord();
        }
    }

    public void checkRecord() {
        this.removeMessages(CHECK);
        this.sendEmptyMessageDelayed(CHECK, delayMillis);
    }

    public interface IRecordListener {
        void onCheckStart();

        void onRecordOk();

        void onRecordFail();

        void onCheckEnd();
    }
}
