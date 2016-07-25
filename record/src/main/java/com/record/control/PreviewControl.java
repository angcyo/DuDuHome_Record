package com.record.control;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.record.util.RUtil;

/**
 * Created by robi on 2016-06-12 09:11.
 */
public class PreviewControl implements Handler.Callback {

    public static final long delayMillis = 2000;
    Context mContext;
    private Handler mainHandler;
    private IPreviewListener mPreviewListener;
    private boolean isSend = false;
    private Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            boolean need = true;

            if (mPreviewListener != null) {
                mPreviewListener.onCheckStart();
            }

            if (RUtil.canPreview()) {
                if (mPreviewListener != null) {
                    mPreviewListener.onPreviewOk();
                }
            } else {
                if (mPreviewListener != null) {
                    mPreviewListener.onPreviewFail();
                }
            }

            if (!RUtil.canRecord()) {
                if (mPreviewListener != null) {
                    mPreviewListener.onRecordFail();
                }
            } else {
                if (mPreviewListener != null) {
                    mPreviewListener.onRecordOk();
                }
            }

            if (mPreviewListener != null) {
                mPreviewListener.onCheckEnd();
            }

            if (need) {
                checkPreview();
            }

//            // TODO: 2016-07-20  // TODO: 2016-07-25 a
//            if (RUtil.failFlag()) {
//                System.exit(1);
//                android.os.Process.killProcess(android.os.Process.myPid());
//            }
        }
    };

    public PreviewControl(Context context) {
        mainHandler = new Handler(Looper.getMainLooper(), this);
        mContext = context.getApplicationContext();
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    public void setPreviewListener(IPreviewListener previewListener) {
        mPreviewListener = previewListener;
    }

    public void checkPreview() {
        mainHandler.removeCallbacks(checkRunnable);
        mainHandler.postDelayed(checkRunnable, delayMillis);
    }

    public void exitCheck() {
        mainHandler.removeCallbacks(checkRunnable);
    }

    public void exit() {
        mainHandler.removeCallbacksAndMessages(null);
        mainHandler = null;
    }

    public interface IPreviewListener {
        void onCheckStart();

        void onPreviewOk();

        void onPreviewFail();

        void onRecordOk();

        void onRecordFail();

        void onCheckEnd();
    }
}
