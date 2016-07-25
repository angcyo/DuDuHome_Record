package com.dudu.aios.ui.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.dudu.aios.ui.fragment.base.BaseFragment;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.DeviceIDUtil;
import com.dudu.android.launcher.utils.cache.BitmapUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceBindFragment extends BaseFragment implements View.OnClickListener {

    private Logger logger;

    private View view;

    private TextView mIMEICodeText;

    private ImageButton mBackButton;

    private ImageView mAppDownloadDimensionCode;

    private ImageView mDeviceBindDimensionCode;


    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.device_with_app_bind, null);
        iniView();
        initListener();
        initData();
        return view;
    }

    private void initData() {
        logger = LoggerFactory.getLogger("DeviceBindFragment");
        mIMEICodeText.setText(DeviceIDUtil.getIMEI(getActivity()));
      //  Bitmap downloadBitmap = BitmapUtils.generateTwoDimensionCode("http://www.dudu.tm", 250, 250);
        Bitmap deviceBindBitmap = BitmapUtils.generateTwoDimensionCode(DeviceIDUtil.getIMEI(getActivity()), 250, 250);
        logger.debug("设备的IMEI号：" + DeviceIDUtil.getIMEI(getActivity()));
       /* if (downloadBitmap != null) {
            mAppDownloadDimensionCode.setImageBitmap(downloadBitmap);
        }*/
        if (deviceBindBitmap != null) {
            mDeviceBindDimensionCode.setImageBitmap(deviceBindBitmap);
        }
    }

    private void initListener() {
        mBackButton.setOnClickListener(this);
    }

    private void iniView() {
        mIMEICodeText = (TextView) view.findViewById(R.id.text_IMEI_code);
        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        mAppDownloadDimensionCode = (ImageView) view.findViewById(R.id.app_download_two_dimension_code);
        mDeviceBindDimensionCode = (ImageView) view.findViewById(R.id.device_bind_two_dimension_code);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                replaceFragment(FragmentConstants.FRAGMENT_MAIN_PAGE);
                break;
        }
    }
}
