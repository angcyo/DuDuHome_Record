package com.dudu.aios.ui.base;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.dudu.android.launcher.R;
import com.dudu.android.launcher.exception.CrashHandler;

public class RCrashActivity extends Activity {

    private int clickCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rsen_base_crash_layout);
        final TextView textView = (TextView) findViewById(R.id.text);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCount++;
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    String msg = extras.getString("msg");
                    textView.setText(msg);
                    copyErrorToClipboard(msg);
                }
                if (clickCount == 4) {
                    restartApp();
                }
            }
        });

    }

    private void restartApp() {
        CrashHandler.restartApplicationWithIntent(this,
                new Intent(this, CrashHandler.getLauncherActivity(this)));
    }

    @SuppressWarnings("deprecation")
    private void copyErrorToClipboard(String msg) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(msg, msg);
            clipboard.setPrimaryClip(clip);
        } else {
            //noinspection deprecation
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setText(msg);
        }
    }
}
