package com.dudu.voice.semantic.chain;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.dudu.aios.ui.activity.MainRecordActivity;
import com.dudu.aios.ui.bt.Contact;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.android.launcher.utils.BtPhoneUtils;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.commonlib.CommonLib;
import com.dudu.voice.FloatWindowUtils;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.bean.PhoneBean;
import com.dudu.voice.semantic.bean.SemanticBean;
import com.dudu.voice.semantic.constant.SemanticConstant;
import com.dudu.voice.semantic.constant.TTSType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 赵圣琪 on 2016/1/6.
 */
public class PhoneChain extends SemanticChain {

    private Bundle bundle;

    public static final String CALL_STR = "呼叫";

    @Override
    public boolean matchSemantic(String service) {
        return SemanticConstant.SERVICE_PHONE.equals(service);
    }

    @Override
    public boolean doSemantic(SemanticBean semantic) {


        PhoneBean bean = (PhoneBean) semantic;
        String action = bean.getAction();


        String contactName = bean.getContactName();
        String phoneNumber = bean.getPhoneNumber();

        if (TextUtils.isEmpty(action) || (bean.getConf() < 0.65)
                || (TextUtils.isEmpty(contactName) && TextUtils.isEmpty(phoneNumber))) {

            return false;
        }


        bundle = new Bundle();

        if (action.equals(SemanticConstant.DOMAIN_PHONE) || action.equals(CALL_STR)) {
            if (BtPhoneUtils.connectionState != BtPhoneUtils.STATE_CONNECTED) {
                VoiceManagerProxy.getInstance().startSpeaking(
                        CommonLib.getInstance().getContext().getString(R.string.bt_noti_connect_waiting), TTSType.TTS_START_UNDERSTANDING, true);
                return true;
            }

        }

        if (!TextUtils.isEmpty(contactName) && TextUtils.isEmpty(phoneNumber) && TextUtils.isEmpty(bean.getOperator())) {
            List<Contact> contacts = BtPhoneUtils.obtainContactsByName(CommonLib.getInstance().getContext(), contactName);
            if (contacts.isEmpty()) {
                return false;
            }
            outCallWithContactName(contactName, contacts);
        }

        toMainActivity();

        if (!TextUtils.isEmpty(phoneNumber)) {
            bundle.putString(Constants.EXTRA_PHONE_NUMBER, phoneNumber);
        }

        if (!TextUtils.isEmpty(bean.getOperator())) {
            handleOperator(bean.getOperator());
        }

        FloatWindowUtils.removeFloatWindow();
        FragmentConstants.TEMP_ARGS = bundle;
        BtPhoneUtils.btCallOutSource = BtPhoneUtils.BTCALL_OUT_SOURCE_VOIC;//语音呼叫号码拨出电话
        MainRecordActivity.appActivity.replaceFragment(FragmentConstants.BT_OUT_CALL);
        return true;
    }

    private void toMainActivity() {
        if (!(ActivitiesManager.getInstance().getTopActivity() instanceof MainRecordActivity) || LauncherApplication.getContext().isReceivingOrder()) {

            Intent intent = new Intent();
            intent.setClass(mContext, MainRecordActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }


    private void outCallWithContactName(String contactName, List<Contact> contacts) {

        if (contacts.size() == 1) {
            String number2 = contacts.get(0).getNumber().replace("-", "");
            bundle.putString(Constants.EXTRA_PHONE_NUMBER, number2);
        } else {
            ArrayList<String> numberList = new ArrayList<>();
            for (Contact c : contacts) {
                numberList.add(c.getNumber());
            }
            bundle.putStringArrayList(Constants.EXTRA_PHONE_NUMBER, numberList);

            mVoiceManager.startSpeaking(contactName + "有" + contacts.size() + "个号码，选择第几个拨打", TTSType.TTS_START_UNDERSTANDING, false);
        }
        bundle.putString(Constants.EXTRA_CONTACT_NAME, contactName);
    }

    private void handleOperator(String operator) {

        if (operator.contains("移动")) {
            bundle.putString(Constants.EXTRA_PHONE_NUMBER, "10086");

        } else if (operator.contains("联通")) {
            bundle.putString(Constants.EXTRA_PHONE_NUMBER, "10010");

        } else if (operator.contains("电信")) {
            bundle.putString(Constants.EXTRA_PHONE_NUMBER, "10000");

        }

    }
}
