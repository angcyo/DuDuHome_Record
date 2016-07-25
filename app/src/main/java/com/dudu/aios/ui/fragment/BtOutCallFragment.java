package com.dudu.aios.ui.fragment;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dudu.aios.ui.base.BaseActivity;
import com.dudu.aios.ui.fragment.base.RBaseFragment;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.BtPhoneUtils;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.android.launcher.utils.cache.AsyncTask;
import com.dudu.voice.FloatWindowUtils;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.TTSType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by Robi on 2016-03-10 18:36.
 * Edit Chad on 2016-04-08 11:30
 */
public class BtOutCallFragment extends RBaseFragment implements View.OnClickListener {

    private Logger logger = LoggerFactory.getLogger("phone.BtOutCallFragment");

    private Button mTerminateButton;

    private TextView mContactNameView, mContactsNumberView;

    @Override
    protected int getContentView() {
        return R.layout.activity_blue_tooth_dialing;
    }

    @Override
    protected void initView(View rootView) {
        mTerminateButton = (Button) mViewHolder.v(R.id.button_drop);
        mContactNameView = (TextView) mViewHolder.v(R.id.caller_name);
        mContactsNumberView = (TextView) mViewHolder.v(R.id.caller_number);
    }

    @Override
    protected void initViewData() {
        logger.trace("initViewData()");
        mTerminateButton.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        logger.debug("onResume()");
        dial();
    }

    @Override
    public void onPause() {
        super.onPause();
        BtPhoneUtils.btCallOutSource = BtPhoneUtils.BTCALL_OUT_SOURCE_DEFAULT;
        myHandler.removeMessages(2);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        logger.debug("onHiddenChanged() hidden:"+hidden);
        if (hidden) {
            mContactNameView.setText("");
            mContactsNumberView.setText("");

            myHandler.removeMessages(2);
            BtPhoneUtils.btCallOutSource = BtPhoneUtils.BTCALL_OUT_SOURCE_DEFAULT;
        } else {
            dial();
        }
    }

    //拨号
    private void dial() {
        if (FragmentConstants.TEMP_ARGS != null  &&
                BtPhoneUtils.btCallOutSource != BtPhoneUtils.BTCALL_OUT_SOURCE_DEFAULT &&
                this.isVisible()) {
            String name = FragmentConstants.TEMP_ARGS.getString(Constants.EXTRA_CONTACT_NAME);
            String number = FragmentConstants.TEMP_ARGS.getString(Constants.EXTRA_PHONE_NUMBER);
            logger.debug("name:"+name + ",number:"+number);
            if (!TextUtils.isEmpty(name)) {
                mContactNameView.setText(name);
            }
            try{
                ArrayList<String> numberList = FragmentConstants.TEMP_ARGS.getStringArrayList(Constants.EXTRA_PHONE_NUMBER);

                if (TextUtils.isEmpty(number) && (null == numberList || numberList.size() == 0)) {
                    VoiceManagerProxy.getInstance().startSpeaking(
                            "请您输入电话号码", TTSType.TTS_DO_NOTHING, false);
                    replaceFragment(FragmentConstants.BT_DIAL);
                    return;
                } else if (!TextUtils.isEmpty(number)) {
                    mContactsNumberView.setText(number);
                }
                //判断是否有多个号码，如果有多个号码则弹出提示界面，等待用户选择
                if (null != numberList && numberList.size() > 0) {
                    replaceFragment(FragmentConstants.BT_DIAL_SELECT_NUMBER);
                    return;
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            if (BtPhoneUtils.connectionState != BtPhoneUtils.STATE_CONNECTED) {
                VoiceManagerProxy.getInstance().startSpeaking(
                        mBaseActivity.getString(R.string.bt_noti_connect_waiting), TTSType.TTS_DO_NOTHING, false);
                replaceFragment(FragmentConstants.BT_DIAL);
                return;
            }

            logger.debug("BtPhoneUtils.btCallState:"+BtPhoneUtils.btCallState +
                    ",BtPhoneUtils.btCallOutSource:"+BtPhoneUtils.btCallOutSource);
            //如果不在通话中
            if (BtPhoneUtils.btCallState != BtPhoneUtils.CALL_STATE_ACTIVE&&
                    BtPhoneUtils.btCallState != BtPhoneUtils.CALL_STATE_DIALING&&
                    BtPhoneUtils.btCallState != BtPhoneUtils.CALL_STATE_INCOMING) {
                String phoneNum = number.replace("-", "").replace(" ","");
                //查找通讯录该号码的姓名并更新UI
                new LoadBtTask().execute(phoneNum);

                if (BtPhoneUtils.btCallOutSource==BtPhoneUtils.BTCALL_OUT_SOURCE_KEYBOARD ||
                        BtPhoneUtils.btCallOutSource==BtPhoneUtils.BTCALL_OUT_SOURCE_VOIC) {
                    FloatWindowUtils.removeFloatWindow();
                    //如果不是蓝牙设备拨出的电话
                    Intent intent = new Intent(Constants.BLUETOOTH_DIAL);
                    intent.putExtra(Constants.DIAL_NUMBER, phoneNum);
                    mBaseActivity.sendBroadcast(intent);
                    BtPhoneUtils.btCallOutSource = BtPhoneUtils.BTCALL_OUT_SOURCE_DEFAULT;
                    // 延时判断当前是否在通话中，否则关闭当前页面
                    // 修复当手机无SIM卡时拨号不关闭拨出界面的问题
                    Message msg = new Message();
                    msg.what = 2;
                    myHandler.sendMessageDelayed(msg, 30000);
                }

            } else {
                logger.trace("还在通话中，不能再次拨号");
            }

        } else {
            logger.debug("电话号码为空 BaseActivity.lastFragment:"+BaseActivity.lastFragment);
            replaceFragment(BaseActivity.lastFragment);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_drop:
                callTermination();
                replaceFragment(FragmentConstants.BT_DIAL);
                break;
        }
    }

    Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            logger.debug("msg.what:"+msg.what);
            if(msg.what==1){
                callTermination();
            }else if(msg.what==2){
                if(BtPhoneUtils.btCallState!=BtPhoneUtils.CALL_STATE_ACTIVE &&
                        (BtPhoneUtils.btCallState!=BtPhoneUtils.CALL_STATE_DIALING ||
                        BtPhoneUtils.btCallState==BtPhoneUtils.CALL_STATE_TERMINATED)){
                    replaceFragment(FragmentConstants.BT_DIAL);
                    callTermination();
                }
            }
        }
    };
    /**
     * 发出挂断电话广播
     */
    private void callTermination() {
        BtPhoneUtils.btCallOutSource = BtPhoneUtils.BTCALL_OUT_SOURCE_DEFAULT;
        Intent intent = new Intent("wld.btphone.bluetooth.CALL_TERMINATION");
        mBaseActivity.sendBroadcast(intent);
    }

    class LoadBtTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String name = "";
            try {
                //根据联系人号码查询姓名
                name = BtPhoneUtils.queryContactNameByNumber(mBaseActivity, params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return name;
        }

        @Override
        protected void onPostExecute(String name) {
            if (null != mContactNameView) {

                mContactNameView.setText(name);
            }
        }
    }

}
