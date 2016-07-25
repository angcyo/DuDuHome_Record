package com.dudu.android.launcher.ui.activity.bluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dudu.aios.ui.base.BaseActivity;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.Constants;

/**
 * @deprecated robi 2016-3-15
 * */
@Deprecated
public class BtOutCallActivity extends BaseActivity implements View.OnClickListener {

    private Button mTerminateButton;

    private TextView mContactNameView, mContactsNumberView;

    @Override
    protected View getChildView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_blue_tooth_dialing, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();
        initData();
    }


    private void initView() {
        mTerminateButton = (Button) findViewById(R.id.button_drop);
        mContactNameView = (TextView) findViewById(R.id.caller_name);
        mContactsNumberView = (TextView) findViewById(R.id.caller_number);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String number = mContactsNumberView.getText().toString().replace(" ", "");
        Intent intent = new Intent(Constants.BLUETOOTH_DIAL);

        intent.putExtra(Constants.DIAL_NUMBER, number);
        this.sendBroadcast(intent);
    }

    private void initListener() {
        mTerminateButton.setOnClickListener(this);
    }


    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra(Constants.EXTRA_CONTACT_NAME);
            String number = intent.getStringExtra(Constants.EXTRA_PHONE_NUMBER);
            if (!TextUtils.isEmpty(name)) {
                mContactNameView.setText(name);
            }
            if (!TextUtils.isEmpty(number)) {
                mContactsNumberView.setText(number);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_drop:
                Intent intent = new Intent("wld.btphone.bluetooth.CALL_TERMINATION");
                sendBroadcast(intent);
                finish();
                break;
        }
    }


}
