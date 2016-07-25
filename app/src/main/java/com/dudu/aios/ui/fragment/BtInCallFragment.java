package com.dudu.aios.ui.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dudu.aios.ui.activity.MainRecordActivity;
import com.dudu.aios.ui.fragment.base.RBaseFragment;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.BtPhoneUtils;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.android.launcher.utils.LogUtils;
import com.dudu.android.launcher.utils.cache.AsyncTask;
import com.dudu.voip.VoipSDKCoreHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2016/5/11.
 */
public class BtInCallFragment extends RBaseFragment implements View.OnClickListener {
    private Button mAcceptButton, mDropButton;

    private ImageButton mBackButton;

    private TextView mCallerName, mCallerNumber;
    private Logger logger = LoggerFactory.getLogger("phone.BtInCallFragment");
    @Override
    protected int getContentView() {
        return R.layout.activity_blue_tooth_caller;
    }

    @Override
    protected void initViewData() {
        initView();
        initListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        logger.debug("onResume()");
        initData();
    }

    @Override
    public void onPause() {
        super.onPause();
        //停止手势功能
        stopGesture();
    }

    @Override
    public void onShow() {
        super.onShow();
        logger.debug("onShow()");
        initData();
    }

    @Override
    public void onHide() {
        super.onHide();
        //停止手势功能
        stopGesture();
    }

    private void initData() {
        logger.debug("BtPhoneUtils.btCallState:"+BtPhoneUtils.btCallState +
        ",VoipSDKCoreHelper.getInstance().eccall_state:"+VoipSDKCoreHelper.getInstance().eccall_state);

        //启动手势功能
        startGesture();
        if (FragmentConstants.TEMP_ARGS != null && BtPhoneUtils.btCallState==BtPhoneUtils.CALL_STATE_INCOMING) {
            String number;

            String iNumber = FragmentConstants.TEMP_ARGS.getString(Constants.EXTRA_PHONE_NUMBER, "");

            number = getPhoneNumber(iNumber);
            logger.debug(" number:" + number);
            mCallerNumber.setText(number);

            //通过电话号码查找通讯录对应的人名
            new LoadBtTask().execute(number);
        }

        //如果网络电话正在通话中
        if(VoipSDKCoreHelper.getInstance().eccall_state == VoipSDKCoreHelper.ERROR_ECCALL_ANSWERED){

            //延时进入网络电话通话界面
            myHandler.sendMessageDelayed(new Message(),1000);
            return;
        }

    }

    private String getPhoneNumber(String iNumber) {
        String number = "";
        if(!TextUtils.isEmpty(iNumber)){

            //1开头的中国大陆手机号
            if(iNumber.startsWith("1")&&iNumber.length()==11){

                number = iNumber.substring(0, 3) + "-" + iNumber.substring(3, 7) + "-" + iNumber.substring(7, 11);
            }else{
                //其他号码因为格式多样不做处理
                number = iNumber;
            }
        }

        return number;
    }

    private void initListener() {
        mAcceptButton.setOnClickListener(this);
        mDropButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
    }

    private void initView() {
        mAcceptButton = (Button) mViewHolder.v(R.id.button_accept);
        mDropButton = (Button) mViewHolder.v(R.id.button_drop);
        mBackButton = (ImageButton) mViewHolder.v(R.id.button_back);
        mCallerName = (TextView) mViewHolder.v(R.id.caller_name);
        mCallerNumber = (TextView) mViewHolder.v(R.id.caller_number);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_accept:
                acceptPhone();
                break;
            case R.id.button_drop:
                rejectPhone();
                break;
            case R.id.button_back:
                break;
        }
    }
    private void rejectPhone() {
        Intent intent = new Intent("wld.btphone.bluetooth.CALL_REJECT");
        mBaseActivity.sendBroadcast(intent);
    }

    private void acceptPhone() {
        Intent intent = new Intent("wld.btphone.bluetooth.CALL_ACCEPT");
        mBaseActivity.sendBroadcast(intent);
    }

    private void holdPhone(){
        Intent intent = new Intent("wld.btphone.bluetooth.CALL_HOLD");
        mBaseActivity.sendBroadcast(intent);
    }
    private void startGesture(){
        Intent intentEnableGesture = new Intent();
        intentEnableGesture.setAction("com.sensortek.broadcast.enable");
        mBaseActivity.sendBroadcast(intentEnableGesture);
    }
    private void stopGesture(){
        Intent intentEnableGesture = new Intent();
        intentEnableGesture.setAction("com.sensortek.broadcast.disable");
        mBaseActivity.sendBroadcast(intentEnableGesture);
    }
    /**
     * 通过电话号码查找通讯录对应的人名
     */
    class LoadBtTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String name = "";
            try{

                name = queryContactNameByNumber(params[0]);
            }catch (Exception e){
                e.printStackTrace();
            }
            return name;
        }

        @Override
        protected void onPostExecute(String name) {
            mCallerName.setText(name);
        }
    }

    /**
     * 查询指定电话的联系人姓名
     * */
    private String queryContactNameByNumber(final String phoneNum) throws Exception {
        if(null==phoneNum || "".equals(phoneNum)){
            return "";
        }
        String name = "";
        Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + phoneNum);
        ContentResolver resolver = mBaseActivity.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Contacts.DISPLAY_NAME}, null, null, null);
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
            logger.debug("name:"+name+",phoneNum:"+phoneNum);
        }
        cursor.close();
        return name;
    }

    public void dispatchKeyEvent(KeyEvent event) {
        int code = event.getKeyCode();
        logger.debug("keyEvent getKeyCode:"+code);
        if(code==92){
            //接听
            acceptPhone();
        }else if(code==93){
            //挂断
            rejectPhone();
        }
    }

    private Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //挂断蓝牙电话
            rejectPhone();
            replaceFragment(FragmentConstants.VOIP_CALLING_FRAGMENT);
        }
    };
}
