package com.dudu.aios.ui.fragment.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dudu.aios.ui.activity.MainRecordActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hugo.weaving.DebugLog;

public abstract class BaseFragment extends BaseManagerFragment {

    protected Logger logger = LoggerFactory.getLogger("init.baseFragment");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return getView();
    }

    @Override
    public void onResume() {
        super.onResume();
        changeTitleColorWhenShow();
        changTitleShowWhenShow();
    }

    @Override
    public void onShow() {
        super.onShow();
        changeTitleColorWhenShow();
        changTitleShowWhenShow();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onHide() {
        super.onHide();
    }

    @Override
    public void onAdd() {
        super.onAdd();
        changeTitleColorWhenShow();
        changTitleShowWhenShow();
    }

    /**
     * 显示时更改标题栏颜色，根据配置决定是否设置为透明
     */
    private void changeTitleColorWhenShow() {

        MainRecordActivity.appActivity.showTitleColorTransparent();
    }

    private void changTitleShowWhenShow() {
        MainRecordActivity.appActivity.showTitle(true);
    }

    private void showFragment(Class<?> cls) {
        Intent intent = new Intent(getActivity(), cls);
        startFragment(intent);
    }

    protected void showFragment(Class<?> cls, @IdRes int id) {
        Intent intent = new Intent(getActivity(), cls);
        startFragment(intent, id);
    }

    protected void initFragment(Class<?> cls, @IdRes int id) {
        Intent intent = new Intent(getActivity(), cls);
        initFragment(intent, id);
    }

    protected void replaceFragment(Class<?> cls, @IdRes int id) {
        BaseFragment parentFragment = (BaseFragment) getParentFragment();
        Intent intent = new Intent(getActivity(), cls);
        parentFragment.startFragment(intent, id);
    }

    @DebugLog
    public void replaceFragment(String name) {
        MainRecordActivity activity = (MainRecordActivity) getActivity();
        if (activity != null)
            activity.replaceFragment(name);
    }

}
