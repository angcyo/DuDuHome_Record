package com.dudu.aios.ui.fragment;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.dudu.aios.ui.base.BaseActivity;
import com.dudu.aios.ui.fragment.base.RBaseFragment;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.ui.view.DigitsEditText;
import com.dudu.android.launcher.utils.BtPhoneUtils;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.android.launcher.utils.LogUtils;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.TTSType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Subscription;

/**
 * 拨号界面
 * Created by Robi on 2016-03-10 17:35.
 */
public class BtDialFragment extends RBaseFragment implements
        View.OnClickListener, TextWatcher {

    private EditText mDigits;

    private Button mDialButton, mDeleteButton;

    private ImageButton mBackButton, mContactsButton;
    private LinearLayout mLinearLayoutContacts;
    private Handler handler;
    private Logger logger = LoggerFactory.getLogger("phone.BtDialFragment");
    private AudioManager mAudioMgr;
    private DeleteNumberRunnable deleteRunnable;

    @Override
    protected int getContentView() {
        return R.layout.activity_blue_tooth_dial;
    }

    @Override
    protected void initView(View rootView) {
        mDigits = (DigitsEditText) mViewHolder.v(R.id.dial_digits);
        mDialButton = (Button) mViewHolder.v(R.id.button_dial);
        mBackButton = (ImageButton) mViewHolder.v(R.id.back_button);
        mDeleteButton = (Button) mViewHolder.v(R.id.delete_button);
        mContactsButton = (ImageButton) mViewHolder.v(R.id.button_contacts);
        mLinearLayoutContacts = (LinearLayout) mViewHolder.v(R.id.linearLayout_contacts);

        mViewHolder.v(R.id.button_dial_keyboard).setSelected(true);

        mAudioMgr = (AudioManager) mBaseActivity.getSystemService(mBaseActivity.AUDIO_SERVICE);

        expandViewTouchDelegate(mDeleteButton, 30, 30, 30, 30);

        deleteRunnable = new DeleteNumberRunnable();

        handler = new DeleteDigitHandler();
    }

    @Override
    protected void initViewData() {
        mDigits.setClickable(false);
        mDigits.setEnabled(false);
        mDigits.setOnClickListener(this);
        mDigits.addTextChangedListener(this);
        mDigits.setCursorVisible(false);
        mDialButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        // mDeleteButton.setOnClickListener(this);
        mContactsButton.setOnClickListener(this);
        mLinearLayoutContacts.setOnClickListener(this);
        final Subscription[] subscriber = new Subscription[1];
        mDeleteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        mDeleteButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    logger.debug("touch delete");
                    handler.post(deleteRunnable);
                    mAudioMgr.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT);
                   /* subscriber[0] = Observable.timer(100, 100, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .subscribe(aLong -> {
                                logger.debug("touch delete timer----------");
                                mAudioMgr.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT);
                                handler.sendEmptyMessage(0);
                            }, throwable -> logger.error("Observable.timer", throwable));*/

                    break;
                case KeyEvent.ACTION_UP:
                    logger.debug("touch delete up");
                    /*if (subscriber.length > 0 && subscriber[0] != null) {
                        subscriber[0].unsubscribe();
                    }*/
                    handler.removeCallbacks(deleteRunnable);
                    break;
            }
            return true;
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_dial:
                doDial();
                break;
            case R.id.back_button:
                mBaseActivity.showMain();
                break;
           /* case R.id.delete_button:
                logger.debug("clk delete");
                removeSelectedDigit();
                break;*/
            case R.id.button_contacts:
            case R.id.linearLayout_contacts:
                replaceFragment(FragmentConstants.BT_CONTACTS);
                break;
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

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onShow() {
        super.onShow();

        if (FragmentConstants.BT_OUT_CALL.equals(BaseActivity.lastSecondFragment) ||
                FragmentConstants.BT_OUT_CALL.equals(BaseActivity.lastFragment) ||
                FragmentConstants.BT_CALLING.equals(BaseActivity.lastSecondFragment) ||
                FragmentConstants.BT_CALLING.equals(BaseActivity.lastFragment) ||
                FragmentConstants.FRAGMENT_MAIN_PAGE.equals(BaseActivity.lastSecondFragment)) {
            if (null != mDigits) {
                mDigits.setText("");
            }
        }
    }

    /**
     * 设置蓝牙可见
     */
    private void setBluetoothDiscoverable() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
        this.startActivity(intent);
    }

    private void doDial() {

        //拨号前先判断蓝牙是否处于连接状态
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if (null == adapter) {
            VoiceManagerProxy.getInstance().startSpeaking(
                    getString(R.string.bt_noti_disenable), TTSType.TTS_DO_NOTHING, false);
            return;
        }
        if (!adapter.isEnabled()) {
            adapter.enable();
            VoiceManagerProxy.getInstance().startSpeaking(
                    getString(R.string.bt_noti_connect_waiting), TTSType.TTS_DO_NOTHING, false);
            return;
        }

        if (BtPhoneUtils.connectionState == BtPhoneUtils.STATE_CONNECTED) {

            String dialString = mDigits.getText().toString().replace(" ","").replace("-","");
            if (TextUtils.isEmpty(dialString)) {
                return;
            }
            //蓝牙电话拨号界面拨出电话
            BtPhoneUtils.btCallOutSource = BtPhoneUtils.BTCALL_OUT_SOURCE_KEYBOARD;
            //拨出电话的广播在BtOutCallFragment中发出，
            //在BtCallReceiver中接收电话接通广播后显示通话中界面
            Bundle bundle = new Bundle();
            bundle.putString(Constants.EXTRA_PHONE_NUMBER, dialString);
            FragmentConstants.TEMP_ARGS = bundle;
            replaceFragment(FragmentConstants.BT_OUT_CALL);
        } else {
            VoiceManagerProxy.getInstance().startSpeaking(
                    mBaseActivity.getString(R.string.bt_noti_connect_waiting), TTSType.TTS_DO_NOTHING, false);
        }
    }

    public boolean isDigitsEmpty() {
        return mDigits.length() == 0;
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

    public void onDialButtonClick(View view) {
        if (mDeleteButton.getVisibility() == View.INVISIBLE) {
            mDeleteButton.setVisibility(View.VISIBLE);
        }
        handleDialButtonClick((String) view.getTag());
        LogUtils.v("keyboard", "--" + view.getTag());
    }

    private class DeleteDigitHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            removeSelectedDigit();
        }
    }

    /**
     * 扩大View的触摸和点击响应范围,最大不超过其父View范围
     *
     * @param view
     * @param top
     * @param bottom
     * @param left
     * @param right
     */
    public static void expandViewTouchDelegate(final View view, final int top,
                                               final int bottom, final int left, final int right) {

        ((View) view.getParent()).post(new Runnable() {
            @Override
            public void run() {
                Rect bounds = new Rect();
                view.setEnabled(true);
                view.getHitRect(bounds);

                bounds.top -= top;
                bounds.bottom += bottom;
                bounds.left -= left;
                bounds.right += right;

                TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

                if (View.class.isInstance(view.getParent())) {
                    ((View) view.getParent()).setTouchDelegate(touchDelegate);
                }
            }
        });
    }

    class DeleteNumberRunnable implements Runnable {

        @Override
        public void run() {
            try {
                logger.debug("removeSelectedDigit()");
                removeSelectedDigit();
                Thread.sleep(100);
                handler.post(deleteRunnable);
            } catch (InterruptedException e) {

            }

        }
    }
}
