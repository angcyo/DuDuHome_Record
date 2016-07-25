package com.record.view;

import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;

import com.record.R;
import com.record.base.RBaseDialogFragment;

/**
 * Created by robi on 2016-06-12 10:45.
 */
public class TipDialogFragment extends RBaseDialogFragment {

    private String content;

    @Override
    protected int getContentView() {
        return R.layout.fragment_sdcard;
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
    protected boolean canCanceledOnOutside() {
        return false;
    }

    @Override
    protected boolean canCancelable() {
        return false;
    }

    @Override
    protected int getGravity() {
        return Gravity.CENTER;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mViewHolder.tV(R.id.contentView).setText(content);
    }

    public void setContent(String content) {
        this.content = content;
    }
}
