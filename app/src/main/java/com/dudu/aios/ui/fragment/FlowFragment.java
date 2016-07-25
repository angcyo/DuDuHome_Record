package com.dudu.aios.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dudu.aios.ui.dialog.FlowPasswordSetDialog;
import com.dudu.aios.ui.fragment.base.BaseFragment;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.aios.ui.view.FlowCompletedView;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.ui.activity.DebugActivity;
import com.dudu.android.launcher.ui.dialog.IPConfigDialog;
import com.dudu.android.launcher.utils.CarStatusUtils;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.android.launcher.utils.SharedPreferencesUtils;
import com.dudu.android.launcher.utils.Utils;
import com.dudu.android.launcher.utils.WifiApAdmin;
import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;
import com.dudu.commonlib.utils.afinal.FinalBitmap;
import com.dudu.monitor.active.ActiveContants;
import com.dudu.monitor.flow.constants.FlowConstants;
import com.dudu.workflow.push.model.PushParams;
import com.dudu.workflow.push.model.ReceiverPushData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;

import de.greenrobot.event.EventBus;

public class FlowFragment extends BaseFragment implements View.OnClickListener {

    private static String WIFI_AP_STATE_CHANGE = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    private ImageButton btnBack;
    private FlowCompletedView flowCompletedView;
    private TextView tvFlowPercent, mUsedFlowView, mRemainingFlowView;
    private TextView tvOpenFlowPrompt, tvCloseFlowPrompt;
    private LinearLayout closeFlowContainer, openFlowContainer, passwordSetContainer;
    private FlowPasswordSetDialog flowPasswordSetDialog;
    private float mTotalFlow = 0;
    private float remainingFlow = 0;
    private DecimalFormat mDecimalFormat = new DecimalFormat("0.00");
    private ImageView tencentPayImage;
    private ImageView aliPayImage;
    private FinalBitmap finalBitmap;
    private Logger logger = LoggerFactory.getLogger("FlowFragment");
    private boolean setPasswordOk = false;

    private String wifiApName;

    private String wifiApPassword;
    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WIFI_AP_STATE_CHANGE.equals(action)) {
                //便携式热点的状态为：10---正在关闭；11---已关闭；12---正在开启；13---已开启
                int state = intent.getIntExtra("wifi_state", 0);
                switch (state) {
                    case WifiApAdmin.WIFI_AP_CLOSING:
                        logger.debug("广播热点的状态:正在关闭热点--" + state);
                        if (setPasswordOk) {
                            return;
                        }
                        showClosingFlowView();
                        break;
                    case WifiApAdmin.WIFI_AP_CLOSED:
                        logger.debug("广播热点的状态:关闭热点--" + state);
                        if (setPasswordOk) {
                            return;
                        }
                        showOpenFlowView();
                        WifiApAdmin.saveLocalSaveFlowState(getActivity(), false);
                        break;
                    case WifiApAdmin.WIFI_AP_OPENING:
                        logger.debug("广播热点的状态:正在打开热点--" + state);
                        showOpeningFlowView();
                        break;
                    case WifiApAdmin.WIFI_AP_OPENED:
                        logger.debug("广播热点的状态:打开热点--" + state);
                        showCloseFlowView();
                        WifiApAdmin.saveLocalSaveFlowState(getActivity(), true);
                        WifiApAdmin.saveSsidAndPassword(getActivity());
                        if (wifiApName != null && wifiApPassword != null && setPasswordOk) {
                            logger.debug("设置热点的用户名：" + wifiApName + "  密码:" + wifiApPassword);
                            //l  WifiApAdmin.saveSsidAndPassword(getActivity(), wifiApName, wifiApPassword);
                            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.set_wifi_name_password_success), Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }
    };

    @Override
    public View getView() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_flow, null);
        initFragmentView(view);
        initClickListener();
        initFlowData();
        finalBitmap = new FinalBitmap(this.getContext());

        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
        return view;
    }

    private void initFlowData() {

        getActivity().registerReceiver(wifiReceiver, new IntentFilter(WIFI_AP_STATE_CHANGE));

        initFlowSwitchState();

//        reFreshFlow();
    }

    /**
     * 初始化本地保存的热点开关的状态
     */
    private void initFlowSwitchState() {
        if (WifiApAdmin.obtainLocalSaveFlowState(getActivity())) {
            logger.debug("获取本地热点的开关状态为：---打开");
            showCloseFlowView();
        } else {
            logger.debug("获取本地热点的开关状态为：---关闭");
            showOpenFlowView();
        }
    }

    private void initClickListener() {
        btnBack.setOnClickListener(this);
        closeFlowContainer.setOnClickListener(this);
        openFlowContainer.setOnClickListener(this);
        passwordSetContainer.setOnClickListener(this);
        flowCompletedView.setOnLongClickListener(v -> {
            if (Utils.isDemoVersion(getActivity())) {
                startActivity(new Intent(getActivity(), DebugActivity.class));
            } else {
                new IPConfigDialog().showDialog(getActivity());
            }

            return true;
        });
    }

    private void initFragmentView(View view) {
        btnBack = (ImageButton) view.findViewById(R.id.button_back);
        flowCompletedView = (FlowCompletedView) view.findViewById(R.id.flowCompletedView);
        tvFlowPercent = (TextView) view.findViewById(R.id.tv_flow_percent);
        closeFlowContainer = (LinearLayout) view.findViewById(R.id.close_flow_container);
        openFlowContainer = (LinearLayout) view.findViewById(R.id.open_flow_container);
        passwordSetContainer = (LinearLayout) view.findViewById(R.id.passwordSet_container);
        tvOpenFlowPrompt = (TextView) view.findViewById(R.id.open_flow_prompt);
        tvCloseFlowPrompt = (TextView) view.findViewById(R.id.close_flow_prompt);
        mUsedFlowView = (TextView) view.findViewById(R.id.used_text);
        mRemainingFlowView = (TextView) view.findViewById(R.id.remaining_flow_text);

        tencentPayImage = (ImageView) view.findViewById(R.id.tencentPayImage);
        aliPayImage = (ImageView) view.findViewById(R.id.aliPayImage);
    }

    @Override
    public void onClick(View v) {
        setPasswordOk = false;
        switch (v.getId()) {
            case R.id.button_back:
                replaceFragment(FragmentConstants.FRAGMENT_MAIN_PAGE);
                break;
            case R.id.close_flow_container:
                CarStatusUtils.saveWifiIsAvailable(false);
                actionCloseFlow();
                break;
            case R.id.open_flow_container:
                CarStatusUtils.saveWifiIsAvailable(true);
                actionOpenFlow();
                break;
            case R.id.passwordSet_container:
                passwordSetContainer.setEnabled(false);
                actionPasswordSet();
                break;
        }
    }

    private void actionOpenFlow() {
        WifiApAdmin.initWifiApState(getActivity());
    }

    private void actionCloseFlow() {
        WifiApAdmin.closeWifiAp(getActivity());
        showClosingFlowView();
    }

    private void showOpenFlowView() {
        tvOpenFlowPrompt.setText(getResources().getString(R.string.open_flow));
        openFlowContainer.setVisibility(View.VISIBLE);
        closeFlowContainer.setVisibility(View.GONE);
        openFlowContainer.setEnabled(true);
    }

    private void showCloseFlowView() {
        tvCloseFlowPrompt.setText(getResources().getString(R.string.close_flow));
        openFlowContainer.setVisibility(View.GONE);
        closeFlowContainer.setVisibility(View.VISIBLE);
        closeFlowContainer.setEnabled(true);
    }

    private void showOpeningFlowView() {
        tvOpenFlowPrompt.setText(getResources().getString(R.string.opening_flow));
        openFlowContainer.setEnabled(false);
    }

    private void showClosingFlowView() {
        tvCloseFlowPrompt.setText(getResources().getString(R.string.closing_flow));
        closeFlowContainer.setEnabled(false);
    }

    @Override
    public void onShow() {
        super.onShow();
        logger.debug("fragment is onShow()");
        initFlowSwitchState();
        loadPayImage();
        reFreshFlow();
    }

    @Override
    public void onResume() {
        super.onResume();
        logger.debug("fragment is onResume()");
        reFreshFlow();
        loadPayImage();
    }

    @Override
    public void onHide() {
        super.onHide();
        setPasswordOk = false;
        logger.debug("fragment is onHide()");
        if (flowPasswordSetDialog != null && flowPasswordSetDialog.isShowing()) {
            flowPasswordSetDialog.dismiss();
            passwordSetContainer.setEnabled(true);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(wifiReceiver);
    }

    private void actionPasswordSet() {
        showPasswordSetDialog();
    }

    private void showPasswordSetDialog() {
        if (flowPasswordSetDialog == null) {
            flowPasswordSetDialog = new FlowPasswordSetDialog(getActivity());
        }
        flowPasswordSetDialog.show();
        flowPasswordSetDialog.setOnCancelOnListener(new FlowPasswordSetDialog.OnCancelOnListener() {
            @Override
            public void actionCancel() {
                passwordSetContainer.setEnabled(true);
            }

            @Override
            public void actionOk(String ssid, String password) {
                if (WifiApAdmin.obtainLocalSaveFlowState(getActivity())) {
                    WifiApAdmin.uploadWifiApConfiguration(getActivity(), ssid, password);
                    showOpeningFlowView();
                } else {
                    WifiApAdmin.saveSsidAndPassword(getActivity(), ssid, password);
                    WifiApAdmin.saveWifiApConfiguration(getActivity(), ssid, password);
                }
                passwordSetContainer.setEnabled(true);
                setPasswordOk = true;
                wifiApName = ssid;
                wifiApPassword = password;
            }
        });
    }

    private void loadPayImage() {
        String tencentPayUri = SharedPreferencesUtil.getStringValue(CommonLib.getInstance().getContext(), ActiveContants.TENCENT_PAY_KEY, null);
        LoggerFactory.getLogger("monitor.ActiveDevice").info("微信支付地址：{}", tencentPayUri);
        if (tencentPayUri != null) {
            finalBitmap.display(tencentPayImage, tencentPayUri, 243, 243);
        }

        String aliPayUri = SharedPreferencesUtil.getStringValue(CommonLib.getInstance().getContext(), ActiveContants.ALIPAY_PAY_KEY, null);
        LoggerFactory.getLogger("monitor.ActiveDevice").info("支付宝支付地址：{}", aliPayUri);
        if (aliPayUri != null) {
            finalBitmap.display(aliPayImage, aliPayUri, 243, 243);
        }
    }


    private void reFreshFlow() {
        LoggerFactory.getLogger("ui.flowFragment").debug("更新流量显示");
        remainingFlow = Float.parseFloat(SharedPreferencesUtils.getStringValue(getActivity(), Constants.KEY_REMAINING_FLOW, FlowConstants.DEFAULT_FLOW_VALUE)) / 1024;

        mTotalFlow = Float.parseFloat(SharedPreferencesUtils.getStringValue(getActivity(), Constants.KEY_MONTH_MAX_VALUE, FlowConstants.DEFAULT_FLOW_VALUE)) / 1024;

        float usedFlow = mTotalFlow - remainingFlow;//使用流量改用差值
        if (usedFlow < 0) {
            usedFlow = 0;
        }
//        mUsedFlowView.setText(getString(R.string.used_flow, mDecimalFormat.format(usedFlow)));
        mUsedFlowView.setText(String.format(getString(R.string.used_flow), usedFlow));

        if (remainingFlow <= 0) {
            mRemainingFlowView.setText(String.format(getString(R.string.remaining_flow), 0));
        } else {
            mRemainingFlowView.setText(String.format(getString(R.string.remaining_flow), remainingFlow));
        }

        int progress;
        if (usedFlow < 0) {
            progress = 100;
        } else {
            progress = Math.round(((usedFlow) * 100 / mTotalFlow));
        }

        if (progress > 100) {
            flowCompletedView.setProgress(100);
            tvFlowPercent.setText(100 + "%");
        } else {
            if (progress >= 95) {
//                WifiApAdmin.closeWifiAp(mContext);
            }
            flowCompletedView.setProgress(progress);
            tvFlowPercent.setText(progress + "%");
        }
    }

    public void onEventMainThread(ReceiverPushData data) {
        if (data != null && data.result != null && PushParams.TRAFFIC_RECHARGE.equals(data.result.method)) {
            LoggerFactory.getLogger("workFlow.webSocket").info("收到流量充值推送：总流量：{}，剩余流量：{}", data.result.monthMaxValue, data.result.remainingFlow);
            SharedPreferencesUtil.putStringValue(CommonLib.getInstance().getContext(), Constants.KEY_MONTH_MAX_VALUE, data.result.monthMaxValue);
            SharedPreferencesUtil.putStringValue(CommonLib.getInstance().getContext(), Constants.KEY_REMAINING_FLOW, data.result.remainingFlow);
            reFreshFlow();
        }
    }
}
