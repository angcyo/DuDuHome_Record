package com.dudu.aios.ui.fragment;

import android.view.View;
import android.widget.ImageButton;

import com.dudu.aios.ui.fragment.base.RBaseFragment;
import com.dudu.android.launcher.R;

public class RequestNetworkFragment extends RBaseFragment implements View.OnClickListener {

    private ImageButton mBackButton;

    @Override
    protected int getContentView() {
        return R.layout.fragment_request_network;
    }

    @Override
    protected void initViewData() {
        iniView();
        initListener();
    }

    private void initListener() {
        mBackButton.setOnClickListener(this);
    }

    private void iniView() {
        mBackButton = (ImageButton) mViewHolder.v(R.id.button_back);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                mBaseActivity.showMain();
                break;
        }
    }
}
