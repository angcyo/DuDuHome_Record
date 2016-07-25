package com.dudu.android.launcher.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import com.dudu.android.launcher.R;

public class MonitorActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);
    }

    public void onButton(View view) {
        Toast.makeText(MonitorActivity.this, "Ok", Toast.LENGTH_SHORT).show();
    }
}
