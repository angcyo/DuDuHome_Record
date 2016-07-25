package com.record.state;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void getValue(View view) {
        T.show(this, RecordControl.getSaveRecordState(this) + "");
    }

    public void setFalse(View view) {
        sendRecordState(StateService.STOP_RECORD);
    }

    public void setTrue(View view) {
        sendRecordState(StateService.START_RECORD);
    }

    private void sendRecordState(String state) {
        InfoBean infoBean = new InfoBean();
        infoBean.record_state = state;
        Intent intent = new Intent("com.record.info", Uri.parse("info:" + infoBean.toString()));
        startService(intent);
    }
}
