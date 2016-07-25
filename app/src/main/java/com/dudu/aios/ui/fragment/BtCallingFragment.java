package com.dudu.aios.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dudu.aios.ui.fragment.base.RBaseFragment;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.broadcast.BtCallReceiver;
import com.dudu.android.launcher.utils.BtPhoneUtils;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.android.launcher.utils.LogUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by robi on 2016-03-15 12:06.
 */
public class BtCallingFragment extends RBaseFragment implements View.OnClickListener {
    private Button mTerminateButton;

    private LinearLayout mLinearLayoutDialKeyboard;
    private LinearLayout mLinearLayoutContacts;
    private ImageButton mBackButton;

    private ImageButton mDialKeyboardButton;
    private ImageButton mContactsButton;

    private TextView mNumberText;

    private TextView mTimeText;

    private Handler timerHandler = new TimerHandler();

    private boolean stopTimer = false;

    private TimerThread mTimerThread = null;
    private String mPhoneNumber = "";
    private long startTime = 0;
    private long duration = 0;
    private long hourTime = 3600*1000;//一个小时的微秒值
    private Logger logger = LoggerFactory.getLogger("phone.BtCallingFragment");
    @Override
    protected int getContentView() {
        return R.layout.activity_blue_tooth_calling;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_BLUETOOTH_PHONE_CONNECT);
        filter.addAction(Constants.ACTION_BLUETOOTH_PHONE_END);
        mBaseActivity.registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void initView(View rootView) {
        super.initView(rootView);
        mLinearLayoutDialKeyboard = (LinearLayout) mViewHolder.v(R.id.linearLayout_dial_keyboard);
        mLinearLayoutContacts = (LinearLayout) mViewHolder.v(R.id.linearLayout_contacts);
        mTerminateButton = (Button) mViewHolder.v(R.id.calling_terminate_button);
        mBackButton = (ImageButton) mViewHolder.v(R.id.button_back);
        mNumberText = (TextView) mViewHolder.v(R.id.caller_name);
        mTimeText = (TextView) mViewHolder.v(R.id.call_time);
        mContactsButton = (ImageButton) mViewHolder.v(R.id.button_contacts);
        mDialKeyboardButton = (ImageButton) mViewHolder.v(R.id.button_dial_keyboard);
    }

    @Override
    protected void initViewData() {
        mLinearLayoutDialKeyboard.setOnClickListener(this);
        mLinearLayoutContacts.setOnClickListener(this);
        mTerminateButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        mContactsButton.setOnClickListener(this);
        mDialKeyboardButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.calling_terminate_button:
                terminateCall();
                break;
            case R.id.button_back:
                mBaseActivity.showMain();
                break;
            case R.id.button_dial_keyboard:
            case R.id.linearLayout_dial_keyboard:
                break;
            case R.id.button_contacts:
            case R.id.linearLayout_contacts:
//                startActivity(new Intent(this, BtContactsActivity.class));
//                replaceFragment(FragmentConstants.BT_CONTACTS);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        logger.debug("onResume");
        setPhoneNumberAndTimer();
    }

    @Override
    public void onShow() {
        super.onShow();
        logger.debug("onShow");
        setPhoneNumberAndTimer();
    }

    @Override
    public void onHide() {
        super.onHide();
        logger.debug("onHide");
        //停止手势功能
        Intent intentEnableGesture = new Intent();
        intentEnableGesture.setAction("com.sensortek.broadcast.disable");
        mBaseActivity.sendBroadcast(intentEnableGesture);

        stopTimer = false;
        mTimerThread = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        logger.debug("onPause");
        stopTimer = false;
        mTimerThread = null;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTimer = false;
        mTimerThread = null;
        logger.debug("onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBaseActivity.unregisterReceiver(broadcastReceiver);
    }

    //挂断电话
    private void terminateCall(){
        Intent intent = new Intent(Constants.BLUETOOTH_CALL_TERMINATION);
        mBaseActivity.sendBroadcast(intent);
        stopTimer = false;
        mTimerThread = null;
        mBaseActivity.replaceFragment(FragmentConstants.BT_DIAL);
    }
    public void onDialButtonClick(View view) {
        String number = (String) view.getTag();
        if (TextUtils.isEmpty(number)){
            return;
        }

        if(BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_ACTIVE) {
            byte[] dtmf = number.getBytes();
            if(dtmf.length>0){
                Intent intent = new Intent(Constants.BLUETOOTH_DIALDTMF);
                intent.putExtra(Constants.DIAL_DTMF, dtmf[0]);
                mBaseActivity.sendBroadcast(intent);
            }
        }
        logger.trace("keyboard", "--" + view.getTag());
    }
    /**
     * 设置界面的通话号码和通话时间
     */
    private void setPhoneNumberAndTimer(){

        //启动手势功能
        Intent intentEnableGesture = new Intent();
        intentEnableGesture.setAction("com.sensortek.broadcast.enable");
        mBaseActivity.sendBroadcast(intentEnableGesture);

        Bundle arguments = getArguments();

        if (arguments != null) {
            mPhoneNumber = arguments.getString(Constants.EXTRA_PHONE_NUMBER);
        }
        if(null!=FragmentConstants.TEMP_ARGS){
            mPhoneNumber = FragmentConstants.TEMP_ARGS.getString(Constants.EXTRA_PHONE_NUMBER);
        }

        if (mPhoneNumber != null) {
            mNumberText.setText(mPhoneNumber);
        }

        if(BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_ACTIVE){
            if(null==mTimerThread){
                mTimerThread = new TimerThread();
                mTimerThread.start();
            }else{
                logger.debug("mTimerThread->"+mTimerThread.getId());
                setStartTime();
            }
        }else {

            stopTimer = false;
            mTimerThread = null;
        }
    }

    private class TimerHandler extends Handler {
        @Override
        public synchronized void handleMessage(Message msg) {
            mTimeText.setText(getDateFormatTime(duration));
        }
    }

    private synchronized String getDateFormatTime(long duration) {
        SimpleDateFormat sdf;
        if(duration<hourTime){
            sdf = new SimpleDateFormat("mm:ss");
        }else {
            //如果通话时间长于一个小时
            sdf = new SimpleDateFormat("HH:mm:ss");
        }
        Date date = new Date(duration);
        logger.trace("duration:"+duration);
        return sdf.format(date);
    }

    private void setStartTime(){
        if(TextUtils.isEmpty(mPhoneNumber)){
            startTime = System.currentTimeMillis();
        }else{
            if(null!=BtCallReceiver.phoneStartTimeList && BtCallReceiver.phoneStartTimeList.size()>0){
                logger.debug("BtCallReceiver.phoneStartTimeList->"+BtCallReceiver.phoneStartTimeList + ",mPhoneNumber:"+mPhoneNumber);

                Iterator iter = BtCallReceiver.phoneStartTimeList.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String key = (String)entry.getKey();
                    if(mPhoneNumber.equals(key)){
                        startTime = (Long)entry.getValue();
                    }
                }
            }else{
                startTime = System.currentTimeMillis();
            }

            if(startTime==0){
                startTime = System.currentTimeMillis();
            }
        }
    }
    private class TimerThread extends Thread {

        @Override
        public void run() {
            setStartTime();
            stopTimer = true;
            while (stopTimer) {
                try {
                    duration = System.currentTimeMillis() - startTime;
                    logger.debug("timer:" + getDateFormatTime(duration));
                    timerHandler.sendEmptyMessage(0);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取电话号码
            String phoneNumber = intent.getStringExtra("HFP_NUMBER");
            String action = intent.getAction();
            logger.debug("BtCallingFragment action:"+ action + ", phoneNumber:"+phoneNumber);

            if(Constants.ACTION_BLUETOOTH_PHONE_CONNECT.equals(action)){

                if(!TextUtils.isEmpty(phoneNumber)){
                    FragmentConstants.TEMP_ARGS.putString(Constants.EXTRA_PHONE_NUMBER, phoneNumber);

                    if(null!=mNumberText){
                        mNumberText.setText(phoneNumber);
                    }
                }
                setPhoneNumberAndTimer();
            }else if(Constants.ACTION_BLUETOOTH_PHONE_END.equals(action)){

            }
        }
    };

    public void dispatchKeyEvent(KeyEvent event) {
        int code = event.getKeyCode();
        logger.debug("keyEvent getKeyCode:"+code);

        if(code==92){
            //挂断
            terminateCall();
//            acceptPhone();
        }else if(code==93){
            //挂断
            terminateCall();
//            rejectPhone();

        }
    }
}
