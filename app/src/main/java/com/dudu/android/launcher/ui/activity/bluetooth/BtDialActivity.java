package com.dudu.android.launcher.ui.activity.bluetooth;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.dudu.aios.ui.base.BaseActivity;
import com.dudu.aios.ui.base.T;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.ui.view.DigitsEditText;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.android.launcher.utils.LogUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;

/**
 * 蓝牙电话
 * @deprecated robi 2016-3-15
 * */
@Deprecated
public class BtDialActivity extends BaseActivity implements
        View.OnClickListener, TextWatcher {

    private EditText mDigits;

    private Button mDialButton;

    private ImageButton mBackButton, mContactsButton, mDeleteButton;

    private Handler handler = new DeleteDigitHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();

        T.show(this, this.getClass().getSimpleName());
    }

    @Override
    protected View getChildView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_blue_tooth_dial, null);
    }

    private void initView() {
        mDigits = (DigitsEditText) findViewById(R.id.dial_digits);
        mDialButton = (Button) findViewById(R.id.button_dial);
        mBackButton = (ImageButton) findViewById(R.id.back_button);
        mDeleteButton = (ImageButton) findViewById(R.id.delete_button);
        mContactsButton = (ImageButton) findViewById(R.id.button_contacts);
    }


    public void initListener() {
        mDigits.setOnClickListener(this);
        mDigits.addTextChangedListener(this);
        mDigits.setCursorVisible(false);
        mDialButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        mDeleteButton.setOnClickListener(this);
        mContactsButton.setOnClickListener(this);
        final Subscription[] subscriber = new Subscription[1];
        mDeleteButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case KeyEvent.ACTION_DOWN:
                        subscriber[0] = Observable.timer(100, 100, TimeUnit.MILLISECONDS)
                                .subscribe(aLong -> {
                                    handler.sendEmptyMessage(0);
                                }, throwable -> Log.e("BtDialActivity", "onTouch: ", throwable));
                        break;
                    case KeyEvent.ACTION_UP:
                        if (subscriber.length > 0 && subscriber[0] != null) {
                            subscriber[0].unsubscribe();
                        }
                        break;
                }
                return true;
            }
        });
    }


    public void onDialButtonClick(View view) {
        if (mDeleteButton.getVisibility() == View.INVISIBLE) {
            mDeleteButton.setVisibility(View.VISIBLE);
        }
        handleDialButtonClick((String) view.getTag());
        LogUtils.v("keyboard", "--" + view.getTag());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_dial:
                doDial();
                break;
            case R.id.back_button:
                finish();
                break;
            case R.id.delete_button:
                removeSelectedDigit();
                break;
            case R.id.button_contacts:
                startActivity(new Intent(this, BtContactsActivity.class));
                finish();
                break;
        }
    }

    private void handleDialButtonClick(String digit) {
        final int length = mDigits.length();
        final int start = mDigits.getSelectionStart();
        final int end = mDigits.getSelectionEnd();
        if (length == start && length == end) {
            mDigits.setCursorVisible(false);
        }

        if (start < end) {
            mDigits.getEditableText().replace(start, end, digit);
        } else {
            mDigits.getEditableText().insert(mDigits.getSelectionEnd(), digit);
        }
    }

    private void removeSelectedDigit() {
        final int length = mDigits.length();
        final int start = mDigits.getSelectionStart();
        final int end = mDigits.getSelectionEnd();
        if (start < end) {
            mDigits.getEditableText().replace(start, end, "");
        } else {
            if (mDigits.isCursorVisible()) {
                if (end > 0) {
                    mDigits.getEditableText().replace(end - 1, end, "");
                }
            } else {
                if (length > 1) {
                    mDigits.getEditableText().replace(length - 1, length, "");
                } else {
                    mDigits.getEditableText().clear();
                }
            }
            String digitString = mDigits.getText().toString();
            if (digitString.length() > 0) {
                if (digitString.substring(digitString.length() - 1, digitString.length()).equals(" ")) {
                    removeSelectedDigit();
                }
            }
        }

        if (isDigitsEmpty()) {
            mDeleteButton.setVisibility(View.INVISIBLE);
        }
    }

    private void doDial() {
        String dialString = mDigits.getText().toString();
        if (TextUtils.isEmpty(dialString)) {
            return;
        }

        Intent intent = new Intent(Constants.BLUETOOTH_DIAL);
        String number = dialString.replace(" ", "");
        intent.putExtra("dial_number", number);
        sendBroadcast(intent);
        startActivity(new Intent(this, BtOutCallActivity.class).putExtra(Constants.EXTRA_PHONE_NUMBER, dialString));
    }

    public boolean isDigitsEmpty() {
        return mDigits.length() == 0;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s == null || s.length() == 0) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i != 3 && i != 8 && s.charAt(i) == ' ') {
                continue;
            } else {
                sb.append(s.charAt(i));
                if ((sb.length() == 4 || sb.length() == 9) && sb.charAt(sb.length() - 1) != ' ') {
                    sb.insert(sb.length() - 1, ' ');
                }
            }
        }
        if (!sb.toString().equals(s.toString())) {
            int index = start + 1;
            if (sb.charAt(start) == ' ') {
                if (before == 0) {
                    index++;
                } else {
                    index--;
                }
            } else {
                if (before == 1) {
                    index--;
                }
            }
            mDigits.setText(sb.toString());
            mDigits.setSelection(index);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private class DeleteDigitHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            removeSelectedDigit();
        }
    }
}
