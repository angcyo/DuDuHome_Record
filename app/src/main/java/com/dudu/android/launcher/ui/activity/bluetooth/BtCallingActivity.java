package com.dudu.android.launcher.ui.activity.bluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dudu.aios.ui.base.BaseActivity;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.android.launcher.utils.LogUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**蓝牙相关*/
/**
 * @deprecated robi 2016-3-15
 * */
@Deprecated
public class BtCallingActivity extends BaseActivity implements OnClickListener {

    private Button mTerminateButton;

    private ImageButton mBackButton;

    private ImageButton mContactsButton;

    private TextView mNumberText;

    private TextView mTimeText;

    private Handler timerHandler = new TimerHandler();

    private boolean stopTimer = false;

    private long duration = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            String number = intent.getStringExtra(Constants.EXTRA_PHONE_NUMBER);
            if (number != null) {
                mNumberText.setText(number);
            }
        }
        new TimerThread().start();
    }

    private void initView() {
        mTerminateButton = (Button) findViewById(R.id.calling_terminate_button);
        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mNumberText = (TextView) findViewById(R.id.caller_name);
        mTimeText = (TextView) findViewById(R.id.call_time);
        mContactsButton = (ImageButton) findViewById(R.id.button_contacts);
    }


    private void initListener() {
        mTerminateButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        mContactsButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.calling_terminate_button:
                Intent intent = new Intent("wld.btphone.bluetooth.CALL_TERMINATION");
                sendBroadcast(intent);
                finish();
                break;
            case R.id.button_back:
                finish();
                break;
            case R.id.button_contacts:
                startActivity(new Intent(this, BtContactsActivity.class));
                break;
        }
    }

    @Override
    protected View getChildView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_blue_tooth_calling, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer = false;
    }

    private class TimerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            mTimeText.setText(getDateFormatTime(duration));
        }
    }

    private String getDateFormatTime(long duration) {
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        Date date = new Date(duration);
        return sdf.format(date);
    }

    private class TimerThread extends Thread {
        @Override
        public void run() {
            stopTimer = true;
            while (stopTimer) {
                try {
                    Thread.sleep(1000);
                    duration += 1000;
                    LogUtils.v("phone", "timer:" + duration);
                    timerHandler.sendEmptyMessage(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
