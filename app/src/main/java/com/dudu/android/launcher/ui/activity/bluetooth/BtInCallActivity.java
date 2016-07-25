package com.dudu.android.launcher.ui.activity.bluetooth;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.content.ContentResolver;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dudu.aios.ui.base.BaseActivity;
import com.dudu.aios.ui.bt.Contact;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.android.launcher.utils.LogUtils;
import com.dudu.android.launcher.utils.cache.AsyncTask;

import java.util.ArrayList;

import java.util.List;

/**
 * Created by Administrator on 2016/1/19.
 */
@Deprecated
public class BtInCallActivity extends BaseActivity implements View.OnClickListener {

    private Button mAcceptButton, mDropButton;

    private ImageButton mBackButton;

    private TextView mCallerName, mCallerNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initListener();
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        String number = "";
        if (intent != null) {
            String iNumber = intent.getStringExtra(Constants.EXTRA_PHONE_NUMBER);
            String name = intent.getStringExtra(Constants.EXTRA_CONTACT_NAME);
            number = getPhoneNumber(iNumber);
            LogUtils.v("phone", "name:" + name + " number:" + number);
            mCallerNumber.setText(number);
            mCallerName.setText(name);
        }

        //chad add
        //通过电话号码查找通讯录对应的人名
        new LoadBtTask().execute(number);
    }

    private String getPhoneNumber(String iNumber) {
        String number = "";
        if(null!=iNumber && !TextUtils.isEmpty(iNumber)){

            //1开头的中国大陆手机号
            if(iNumber.startsWith("1")&&iNumber.length()==11){

                number = iNumber.substring(0, 3) + " " + iNumber.substring(3, 7) + " " + iNumber.substring(7, 11);
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
        mAcceptButton = (Button) findViewById(R.id.button_accept);
        mDropButton = (Button) findViewById(R.id.button_drop);
        mBackButton = (ImageButton) findViewById(R.id.button_back);
        mCallerName = (TextView) findViewById(R.id.caller_name);
        mCallerNumber = (TextView) findViewById(R.id.caller_number);
    }

    @Override
    protected View getChildView() {
        return LayoutInflater.from(this).inflate(R.layout.activity_blue_tooth_caller, null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_accept:
                acceptPhone();
                break;
            case R.id.button_drop:
                rejectPhone();
                finish();
                break;
            case R.id.button_back:
                break;
        }
    }

    private void rejectPhone() {
        Intent intent = new Intent("wld.btphone.bluetooth.CALL_REJECT");
        sendBroadcast(intent);
    }

    private void acceptPhone() {
        Intent intent = new Intent("wld.btphone.bluetooth.CALL_ACCEPT");
        sendBroadcast(intent);
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
        ContentResolver resolver = this.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{ContactsContract.Contacts.DISPLAY_NAME}, null, null, null);
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }
}
