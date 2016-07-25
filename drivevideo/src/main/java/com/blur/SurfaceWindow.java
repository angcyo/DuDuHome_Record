package com.blur;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.File.KeyConstants;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;

/**
 * Created by robi on 2016-04-28 20:56.
 */
public class SurfaceWindow {

    private static Dialog mDialog;
    private static SurfaceHolder mSurfaceHolder;

    public static void hideWindowDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public static boolean isShow() {
        return mDialog != null;
    }

    public static void showWindowDialog(Context context) {
        if (mDialog != null || context == null) {
            return;
        }

        SurfaceView surfaceView = new SurfaceView(context);
        mDialog = new Dialog(context.getApplicationContext());
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        Window window = mDialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.type = WindowManager.LayoutParams.TYPE_TOAST;
        attributes.gravity = Gravity.LEFT | Gravity.TOP;
        attributes.width = 1;
        attributes.height = 1;

        window.setAttributes(attributes);
        mDialog.setContentView(surfaceView, new FrameLayout.LayoutParams(1, 1));
        mDialog.show();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mSurfaceHolder = holder;
//                RecorderThread.restartMediaRecorder(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                mSurfaceHolder = holder;
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mSurfaceHolder = null;
//                RecorderThread.stopMediaRecorder(false);
            }
        });
    }

    public static SurfaceHolder getSurfaceHolder() {
        return mSurfaceHolder;
    }
}
