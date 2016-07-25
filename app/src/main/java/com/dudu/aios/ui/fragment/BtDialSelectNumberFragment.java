package com.dudu.aios.ui.fragment;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dudu.aios.ui.fragment.base.RBaseFragment;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.BtPhoneUtils;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.constant.TTSType;
import com.dudu.voice.semantic.engine.SemanticEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * 多个号码选择界面
 * Created by Chad on 2016-04-22 11:45.
 */
public class BtDialSelectNumberFragment extends RBaseFragment implements
        View.OnClickListener, AdapterView.OnItemClickListener{

    private ListView mListView;
    private TextView mContactNameView;
    private Button mDialButton;

    private ImageButton mBackButton, mContactsButton, mDeleteButton;
    private LinearLayout mLinearLayoutContacts;
    private MyAdapter numberListAdapter;
    private Logger logger = LoggerFactory.getLogger("phone.BtDialSelectNumberFragment");
    private ArrayList<String> phonesNumberList = new ArrayList<>();


    @Override
    protected int getContentView() {
        return R.layout.activity_blue_tooth_dial_select_number;
    }

    @Override
    protected void initView(View rootView) {
        mListView = (ListView) mViewHolder.v(R.id.listView_phoneNumber);
        mContactNameView = (TextView) mViewHolder.v(R.id.caller_name);
        mDialButton = (Button) mViewHolder.v(R.id.button_dial);
        mBackButton = (ImageButton) mViewHolder.v(R.id.back_button);
        mDeleteButton = (ImageButton) mViewHolder.v(R.id.delete_button);
        mContactsButton = (ImageButton) mViewHolder.v(R.id.button_contacts);
        mLinearLayoutContacts = (LinearLayout) mViewHolder.v(R.id.linearLayout_contacts);

        mViewHolder.v(R.id.button_dial_keyboard).setSelected(true);
    }

    @Override
    protected void initViewData() {
//        mDialButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
//        mDeleteButton.setOnClickListener(this);
        mContactsButton.setOnClickListener(this);
        mLinearLayoutContacts.setOnClickListener(this);

        numberListAdapter = new MyAdapter(mBaseActivity, phonesNumberList);
        mListView.setAdapter(numberListAdapter);
        mListView.setDivider(new ColorDrawable(Color.WHITE));
        mListView.setDividerHeight(1);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_dial:
//                doDial();
                break;
            case R.id.back_button:
                replaceFragment(FragmentConstants.BT_DIAL);
                break;
            case R.id.delete_button:
//                removeSelectedDigit();
                break;
            case R.id.button_contacts:
            case R.id.linearLayout_contacts:
                replaceFragment(FragmentConstants.BT_CONTACTS);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setData();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onShow() {
        super.onShow();
        setData();
    }

    private void setData(){
        SemanticEngine.getProcessor().switchSemanticType(SceneType.BTCALL);
        String name = FragmentConstants.TEMP_ARGS.getString(Constants.EXTRA_CONTACT_NAME);
        ArrayList<String> numberList = FragmentConstants.TEMP_ARGS.getStringArrayList(Constants.EXTRA_PHONE_NUMBER);
        if(!TextUtils.isEmpty(name)){
            mContactNameView.setText(name);
        }
        if( null!=numberList && numberList.size()>0){
            numberListAdapter.setData(numberList);
        }
    }


    private void doDial(String dialString) {

        //拨号前先判断蓝牙是否处于连接状态
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        if(null==adapter){
            VoiceManagerProxy.getInstance().startSpeaking(
                    "无法获取到您的设备蓝牙服务", TTSType.TTS_DO_NOTHING, false);

            return;
        }
        if(!adapter.isEnabled()){
            adapter.enable();
            VoiceManagerProxy.getInstance().startSpeaking(
                    "已经帮您启动了蓝牙服务", TTSType.TTS_DO_NOTHING, false);
            return;
        }

        if(BtPhoneUtils.connectionState == BtPhoneUtils.STATE_CONNECTED){

            if (TextUtils.isEmpty(dialString)) {
                return;
            }

            //拨出电话的广播在BtOutCallFragment中发出，
            //在BtCallReceiver中接收电话接通广播后显示通话中界面
            BtPhoneUtils.btCallOutSource = BtPhoneUtils.BTCALL_OUT_SOURCE_VOIC;
            Bundle bundle = new Bundle();
            bundle.putString(Constants.EXTRA_PHONE_NUMBER, dialString);
            FragmentConstants.TEMP_ARGS = bundle;
            replaceFragment(FragmentConstants.BT_OUT_CALL);
        }else{
            VoiceManagerProxy.getInstance().startSpeaking(
                    mBaseActivity.getString(R.string.bt_noti_connect_waiting), TTSType.TTS_DO_NOTHING, false);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String number = (String)numberListAdapter.getItem(position);
        doDial(number);
    }

    class MyAdapter extends BaseAdapter{
        private Context context;
        private LayoutInflater inflater;
        private ArrayList<String> data;
        public MyAdapter(Context context, ArrayList<String> data) {
            this.context = context;
            this.data = data;
            inflater = LayoutInflater.from(context);
        }

        public void setData(ArrayList<String> data) {
            this.data = (ArrayList<String>) data.clone();
            notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.bt_phone_number_item, parent, false);
                holder.tvNo = (TextView) convertView.findViewById(R.id.textview_no);
                holder.tvContactsNumber = (TextView) convertView.findViewById(R.id.contacts_number);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvNo.setText("号码" + (position + 1));
            holder.tvContactsNumber.setText(data.get(position));
            return convertView;
        }
        class ViewHolder {
            TextView tvNo;
            TextView tvContactsNumber;
        }
    }
}
