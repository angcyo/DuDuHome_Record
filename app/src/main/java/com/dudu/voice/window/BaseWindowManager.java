package com.dudu.voice.window;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.dudu.android.launcher.LauncherApplication;
import com.dudu.voice.VoiceManagerProxy;

/**
 * Created by Administrator on 2016/1/4.
 */
public abstract class BaseWindowManager implements FloatWindowManager {

    protected WindowManager mWindowManager;

    protected WindowManager.LayoutParams mLayoutParams;

    protected View mFloatWindowView;

    protected Context mContext;

    protected boolean mShowFloatWindow;

    protected VoiceManagerProxy mVoiceManager;

    public BaseWindowManager() {
        mContext = LauncherApplication.getContext();

        mVoiceManager = VoiceManagerProxy.getInstance();

        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        mFloatWindowView = LayoutInflater.from(mContext).inflate(getFloatWindowLayout(), null);

        initWindow();
    }

    public boolean isShowFloatWindow() {
        return mShowFloatWindow;
    }

    public abstract void initWindow();

    public abstract int getFloatWindowLayout();

    protected void addFloatView() {
        if (mShowFloatWindow) {
            return;
        }
        try {
            mWindowManager.addView(mFloatWindowView, mLayoutParams);
            mShowFloatWindow = true;
        } catch (Exception e) {
            mShowFloatWindow = false;
        }

    }


    protected void removeFloatView() {


        mVoiceManager.onStop();

        if (!mShowFloatWindow) {
            return;
        }

        if (mWindowManager != null && mFloatWindowView != null)
            mWindowManager.removeView(mFloatWindowView);

        mShowFloatWindow = false;
    }

}
