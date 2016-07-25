package com.dudu.aios.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dudu.aios.ui.fragment.base.BaseFragment;
import com.dudu.aios.ui.robbery.RobberyConstant;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.LogUtils;
import com.dudu.commonlib.utils.TextVerify;
import com.dudu.persistence.UserMessage.UserMessage;
import com.dudu.workflow.common.DataFlowFactory;
import com.dudu.workflow.obd.VehicleConstants;
import com.dudu.workflow.push.model.PushParams;
import com.dudu.workflow.push.model.ReceiverPushData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;
import rx.functions.Action1;

;

public class VehiclePasswordSetFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "VehiclePasswordSetFragment";

    private Logger logger= LoggerFactory.getLogger("SocketClient");

    private Button btnGesture;

    private Button btnZero, btnOne, btnTwo, btnThree, btnFour, btnFive, btnSix, btnSeven, btnEight, btnNine;

    private int passwordDigit = 0;

    private String password = "";

    private LinearLayout dynamicPasswordContainer;

    private Handler handler = new MyHandle();

    private String category = "";

    private String localPassword;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LogUtils.v(TAG, "onCreateView..........");
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.guard_password_set_layout, container, false);
        initView(view);
        initListener();
        initData();
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
        return view;
    }

    private void initData() {
        addParams();
    }

    private void initView(View view) {
        btnGesture = (Button) view.findViewById(R.id.button_gesture_unlock);

        dynamicPasswordContainer = (LinearLayout) view.findViewById(R.id.dynamic_password_container);

        btnZero = (Button) view.findViewById(R.id.button_zero);
        btnOne = (Button) view.findViewById(R.id.button_one);
        btnTwo = (Button) view.findViewById(R.id.button_two);
        btnThree = (Button) view.findViewById(R.id.button_three);
        btnFour = (Button) view.findViewById(R.id.button_four);
        btnFive = (Button) view.findViewById(R.id.button_five);
        btnSix = (Button) view.findViewById(R.id.button_six);
        btnSeven = (Button) view.findViewById(R.id.button_seven);
        btnEight = (Button) view.findViewById(R.id.button_eight);
        btnNine = (Button) view.findViewById(R.id.button_nine);
    }

    private void initListener() {
        btnGesture.setOnClickListener(this);

        btnZero.setOnClickListener(this);
        btnOne.setOnClickListener(this);
        btnTwo.setOnClickListener(this);
        btnThree.setOnClickListener(this);
        btnFour.setOnClickListener(this);
        btnFive.setOnClickListener(this);
        btnSix.setOnClickListener(this);
        btnSeven.setOnClickListener(this);
        btnEight.setOnClickListener(this);
        btnNine.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_one:
                break;
            case R.id.button_two:
                break;
            case R.id.button_three:
                break;
            case R.id.button_four:
                break;
            case R.id.button_five:
                break;
            case R.id.button_six:
                break;
            case R.id.button_seven:
                break;
            case R.id.button_eight:
                break;
            case R.id.button_nine:
                break;
            case R.id.button_zero:
                break;
            case R.id.button_gesture_unlock:
                Bundle bundle = new Bundle();
                bundle.putString(RobberyConstant.CATEGORY_CONSTANT, category);
                FragmentConstants.TEMP_ARGS = bundle;
//                fragment.setArguments(bundle);
//                getFragmentManager().beginTransaction().replace(R.id.vehicle_right_layout, fragment).commit();
                replaceFragment(GestureFragment.class, R.id.vehicle_right_layout);
//                getFragmentManager().beginTransaction().replace(R.id.vehicle_right_layout, new GestureFragment()).commit();
                return;
        }
        handleDialButtonClick(v);
    }

    private void handleDialButtonClick(View v) {
        password += v.getTag();
        passwordDigit++;
        stePassword(passwordDigit);
        if (passwordDigit == 4) {
            passwordDigit = 0;

            if (password.equals(localPassword)) {
                Bundle bundle = new Bundle();
                bundle.putString(VehicleConstants.UNLOCK_GUARD_PASS, "1");
                if (RobberyConstant.GUARD_CONSTANT.equals(category)) {
                    //防盗
                    FragmentConstants.TEMP_ARGS = bundle;
                    replaceFragment(GuardFragment.class, R.id.vehicle_right_layout);
                } else if (RobberyConstant.ROBBERY_CONSTANT.equals(category)) {
                    //防劫
                    FragmentConstants.TEMP_ARGS = bundle;
                    replaceFragment(RobberyMainFragment.class, R.id.vehicle_right_layout);
                }
            } else {
                //错误
                handler.sendEmptyMessageDelayed(0, 1000);
            }
            password = "";
        }
    }

    private void stePassword(int step) {
        dynamicPasswordContainer.removeAllViews();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < 4; i++) {
            ImageView imageView = new ImageView(getActivity());
            if (step > i) {
                imageView.setImageResource(R.drawable.password_unlock_point_full);
            } else {
                imageView.setImageResource(R.drawable.password_unlock_point_null);
            }
            layoutParams.setMargins(7, 7, 7, 7);
            imageView.setLayoutParams(layoutParams);
            dynamicPasswordContainer.addView(imageView);
        }
    }

    class MyHandle extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            stePassword(0);
        }
    }

    @Override
    public void onShow() {
        addParams();
    }

    private void addParams() {
        stePassword(0);
        Bundle bundle = FragmentConstants.TEMP_ARGS;
        if (bundle != null) {
            category = bundle.getString(RobberyConstant.CATEGORY_CONSTANT);
        }
        obtainLocalDigitPassword();
    }

    private void obtainLocalDigitPassword() {
        DataFlowFactory.getUserMessageFlow().obtainUserMessage().subscribe(new Action1<UserMessage>() {
            @Override
            public void call(UserMessage userMessage) {
                localPassword = userMessage.getDigitPassword();
                LogUtils.v(TAG, "获取本地的数字密码成功--" + localPassword);
                boolean gesturePasswordSwitchState = userMessage.isGesturePasswordSwitchState();
                LogUtils.v(TAG, "获取本地手势密码的开关状态：" + (gesturePasswordSwitchState ? "开启" : "关闭"));
                if (gesturePasswordSwitchState) {
                    btnGesture.setVisibility(View.VISIBLE);
                } else {
                    btnGesture.setVisibility(View.GONE);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                LogUtils.v(TAG, "获取本地的数字密码失败" + throwable.toString());
            }
        });
    }

    public void onEventMainThread(ReceiverPushData receiverData) {
        logger.debug("数字密码的页面接受到修改UI状态的命令");
        if (receiverData != null && receiverData.result != null) {
            String method = receiverData.result.method;
            if (receiverData.resultCode == 0 && method != null) {
                if (method.equals(PushParams.GUARD_SET_PASSWORD)) {
                    String digitPassword = receiverData.result.protectThiefPassword;
                    LogUtils.v(TAG, "数字密码：" + digitPassword);
                    if (!TextVerify.isEmpty(digitPassword)) {
                        localPassword = digitPassword;
                    }
                }
            }

        }
    }

}
