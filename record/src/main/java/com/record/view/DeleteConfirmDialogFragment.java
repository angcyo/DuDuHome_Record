package com.record.view;

import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;

import com.record.R;
import com.record.base.RBaseDialogFragment;
import com.record.mode.DeleteConfirmDialogMode;

/**
 * Created by robi on 2016-06-12 10:45.
 */
public class DeleteConfirmDialogFragment extends RBaseDialogFragment {

    DeleteConfirmDialogMode mDeleteConfirmDialogMode;

    public DeleteConfirmDialogFragment() {
        mDeleteConfirmDialogMode = new DeleteConfirmDialogMode();
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_delete_confirm;
    }

    public void setDialogTitle(String title) {
        mDeleteConfirmDialogMode.setTitle(title);
    }

    public void setDialogContent(String content) {
        mDeleteConfirmDialogMode.setContent(content);
    }

    public void setDialogListener(DeleteConfirmDialogMode.IDeleteListener listener) {
        mDeleteConfirmDialogMode.setDeleteListener(listener);
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
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        mDeleteConfirmDialogMode.initView(this, mViewHolder);
    }
}
