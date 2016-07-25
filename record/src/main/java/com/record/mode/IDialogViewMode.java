package com.record.mode;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.record.base.RBaseViewHolder;

/**
 * Created by robi on 2016-07-01 22:23.
 */
public abstract class IDialogViewMode extends IViewMode {

    DialogFragment mDialogFragment;

    @Override
    public void initView(FragmentActivity fragmentActivity, RBaseViewHolder viewHolder) {
        mFragmentActivity = fragmentActivity;
        mViewHolder = viewHolder;
    }

    public void initView(DialogFragment dialogFragment, RBaseViewHolder viewHolder) {
        mFragmentActivity = dialogFragment.getActivity();
        mDialogFragment = dialogFragment;
        mViewHolder = viewHolder;
    }
}
