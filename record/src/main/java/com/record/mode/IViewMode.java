package com.record.mode;

import android.support.v4.app.FragmentActivity;

import com.record.base.RBaseViewHolder;

/**
 * Created by robi on 2016-07-01 22:04.
 */
public abstract class IViewMode {
    protected RBaseViewHolder mViewHolder = null;
    protected FragmentActivity mFragmentActivity = null;

    public abstract void initView(FragmentActivity fragmentActivity, RBaseViewHolder viewHolder);
}
