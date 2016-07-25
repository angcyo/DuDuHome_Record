package com.dudu.aios.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dudu.aios.ui.fragment.base.BaseFragment;
import com.dudu.aios.ui.robbery.RobberyConstant;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.aios.ui.view.GestureLockViewGroup;
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

/**
 * Created by Administrator on 2016/2/19.
 */
public class GestureFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "GestureFragment";

    private Logger logger = LoggerFactory.getLogger("SocketClient");

    private TextView tvDrawPrompt;

    private Button btPasswordSet;

    private GestureLockViewGroup gestureLockViewGroup;

    private Handler handler = new MyHandle();

    private String category = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_gesture, container, false);
        initView(view);
        initListener();
        initData();
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
        return view;
    }

    private void initData() {
        LogUtils.v(TAG, "initData()...");

        Bundle bundle = getArguments();
        if (bundle != null) {
            category = bundle.getString(RobberyConstant.CATEGORY_CONSTANT);

        }
        //设置的手势密码
        DataFlowFactory.getUserMessageFlow().obtainUserMessage().subscribe(new Action1<UserMessage>() {
            @Override
            public void call(UserMessage userMessage) {
                gestureLockViewGroup.setAnswer(userMessage.getGesturePassword());
                LogUtils.v(TAG, "本地手势密码获取成功---" + userMessage.getGesturePassword());
                LogUtils.v(TAG, "获取本地数字密码的开关状态：" + userMessage.isDigitPasswordSwitchState());
                boolean digitPasswordSwitchState = userMessage.isDigitPasswordSwitchState();
                LogUtils.v(TAG, "获取本地数字密码的开关状态：" + (digitPasswordSwitchState ? "开启" : "关闭"));
                if (digitPasswordSwitchState) {
                    btPasswordSet.setVisibility(View.VISIBLE);
                } else {
                    btPasswordSet.setVisibility(View.GONE);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                LogUtils.v(TAG, "本地手势密码获取失败" + throwable.toString());
            }
        });
        gestureLockViewGroup.setOnGestureLockViewListener(new GestureLockViewGroup.OnGestureLockViewListener() {
            @Override
            public void onBlockSelected(int cId) {

            }

            @Override
            public void onGestureEvent(boolean matched) {
                if (matched) {
                    //绘制成功
                    tvDrawPrompt.setText(getResources().getString(R.string.draw_success));
                    tvDrawPrompt.setTextColor(getResources().getColor(R.color.blue));
                    tvDrawPrompt.setText("");
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
                    //绘制失败
                    tvDrawPrompt.setText(getResources().getString(R.string.draw_fault));
                    tvDrawPrompt.setTextColor(getResources().getColor(R.color.red_mistake));
                    handler.sendEmptyMessageDelayed(0, 1000);
                }

            }

            @Override
            public void onUnmatchedExceedBoundary() {
                gestureLockViewGroup.setUnMatchExceedBoundary(5);
            }
        });
    }

    private void initListener() {
        btPasswordSet.setOnClickListener(this);
    }

    private void initView(View view) {
        LogUtils.v(TAG, "initView()..");
        tvDrawPrompt = (TextView) view.findViewById(R.id.text_draw_prompt);
        btPasswordSet = (Button) view.findViewById(R.id.button_passwordSet);
        gestureLockViewGroup = (GestureLockViewGroup) view.findViewById(R.id.id_gestureLockViewGroup);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_passwordSet:
//                VehiclePasswordSetFragment fragment = new VehiclePasswordSetFragment();
                Bundle bundle = new Bundle();
                bundle.putString(RobberyConstant.CATEGORY_CONSTANT, category);
                FragmentConstants.TEMP_ARGS = bundle;
//                fragment.setArguments(bundle);
//                getFragmentManager().beginTransaction().replace(R.id.vehicle_right_layout, fragment).commit();
                replaceFragment(VehiclePasswordSetFragment.class, R.id.vehicle_right_layout);
                break;
        }
    }

    class MyHandle extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tvDrawPrompt.setText("");
        }
    }

    @Override
    public void onAdd() {
        LogUtils.v(TAG, "onAdd()...." + "category:" + FragmentConstants.TEMP_ARGS.getString(RobberyConstant.CATEGORY_CONSTANT));
        addParams();
        //initData();
    }

    @Override
    public void onShow() {
        LogUtils.v(TAG, "onShow()...." + "category:" + FragmentConstants.TEMP_ARGS.getString(RobberyConstant.CATEGORY_CONSTANT));
        addParams();
        initData();
    }

    private void addParams() {
        Bundle bundle = FragmentConstants.TEMP_ARGS;
        if (bundle != null) {
            category = bundle.getString(RobberyConstant.CATEGORY_CONSTANT);
        }
    }

    public void onEventMainThread(ReceiverPushData receiverData) {
        logger.debug("手势的页面接受到修改UI状态的命令");
        if (receiverData != null && receiverData.result != null) {
            String method = receiverData.result.method;
            if (receiverData.resultCode == 0 && method != null) {
                if (method.equals(PushParams.GUARD_SET_PASSWORD)) {
                    String gesturePassword = receiverData.result.protectThiefSignalPassword;
                    LogUtils.v(TAG, "手势密码：" + gesturePassword);
                    if (!TextVerify.isEmpty(gesturePassword)) {
                        gestureLockViewGroup.setAnswer(gesturePassword);
                    }
                }
            }

        }
    }


}
