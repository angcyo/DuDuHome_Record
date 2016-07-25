package com.dudu.aios.ui.voice;

import android.view.LayoutInflater;
import android.view.View;

import com.dudu.aios.ui.fragment.base.BaseFragment;
import com.dudu.android.launcher.R;

/**
 * Created by lxh on 2016/2/13.
 */
public class VoiceFragment extends BaseFragment {

    @Override
    public View getView() {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.voice_layout, null);

        return view;
    }

    @Override
    public void onHide() {
        super.onHide();
    }

    @Override
    public void onShow() {
        super.onShow();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
