package com.record.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;

import com.record.R;
import com.record.base.RBaseDialogFragment;
import com.record.mode.MenuDialogMode;

/**
 * Created by robi on 2016-06-03 17:21.
 */
public class MenuDialogFragment extends RBaseDialogFragment {

    MenuDialogMode mMenuDialogMode;

    @Override
    protected int getContentView() {
        return R.layout.fragment_menu;
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
        mMenuDialogMode = new MenuDialogMode(this, mViewHolder);
        mMenuDialogMode.initView();
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
        mMenuDialogMode.onDismiss();
    }

}
