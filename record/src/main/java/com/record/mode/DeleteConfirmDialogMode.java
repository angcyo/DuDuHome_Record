package com.record.mode;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.record.R;
import com.record.base.RBaseViewHolder;

/**
 * Created by robi on 2016-06-03 21:04.
 */
public class DeleteConfirmDialogMode {

    private RBaseViewHolder mViewHolder;
    private FragmentActivity mActivity;

    private DialogFragment mDialogFragment;
    private String title, content;

    private IDeleteListener mDeleteListener;

    public DeleteConfirmDialogMode() {
    }

    public DeleteConfirmDialogMode(DialogFragment fragment, RBaseViewHolder viewHolder) {
        mViewHolder = viewHolder;
        mActivity = fragment.getActivity();
        mDialogFragment = fragment;
    }

    public void initView() {
        initCloseView();
        initCancelView();
        initConfirmView();

        mViewHolder.tV(R.id.titleView).setText(title);
        mViewHolder.tV(R.id.contentView).setText(content);
    }

    public void initView(DialogFragment fragment, RBaseViewHolder viewHolder) {
        mViewHolder = viewHolder;
        mActivity = fragment.getActivity();
        mDialogFragment = fragment;

        initView();
    }

    public void setDeleteListener(IDeleteListener deleteListener) {
        mDeleteListener = deleteListener;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    private void initConfirmView() {
        mViewHolder.v(R.id.confirmView).setOnClickListener(v -> {
            mDialogFragment.dismiss();
            if (mDeleteListener != null) {
                mDeleteListener.onConfirm();
            }
        });
    }

    private void initCancelView() {
        mViewHolder.v(R.id.cancelView).setOnClickListener(v -> mDialogFragment.dismiss());

    }

    private void initCloseView() {
        mViewHolder.v(R.id.closeView).setOnClickListener(v -> mDialogFragment.dismiss());
    }

    public interface IDeleteListener {
        void onConfirm();
    }
}
