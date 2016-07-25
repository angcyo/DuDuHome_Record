package com.record.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;

import com.record.R;
import com.record.base.RBaseDialogFragment;
import com.record.mode.SettingDialogMode;

/**
 * Created by robi on 2016-06-03 17:21.
 */
public class SettingDialogFragment extends RBaseDialogFragment {

    SettingDialogMode mSettingDialogMode;

    @Override
    protected int getContentView() {
        return R.layout.fragment_setting;
    }

    /**
     * 是否变暗
     */
    @Override
    protected boolean isDimEnabled() {
        return true;
    }


    @Override
    protected void initView(Bundle savedInstanceState) {
        mSettingDialogMode = new SettingDialogMode(this, mViewHolder);
        mSettingDialogMode.initView();
    }

    @Override
    protected int getWindowWidth() {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

    @Override
    protected boolean canTouchOnOutside() {
        return false;
    }

    @Override
    protected int getGravity() {
        return Gravity.CENTER;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mSettingDialogMode.onDismiss();
    }

}
