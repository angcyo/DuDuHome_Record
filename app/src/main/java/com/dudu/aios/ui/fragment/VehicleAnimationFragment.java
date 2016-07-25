package com.dudu.aios.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dudu.aios.ui.fragment.base.RBaseFragment;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.ui.view.ListViewScrollbarView;
import com.dudu.carChecking.AnimVideoView;
import com.dudu.carChecking.CarCheckingProxy;
import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.TextVerify;
import com.dudu.obd.ClearFaultResultEvent;
import com.dudu.persistence.driving.FaultCode;
import com.dudu.rest.model.driving.response.FaultCodeDetailMessage;
import com.dudu.rest.model.driving.response.GetCarBrandResponse;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.constant.TTSType;
import com.dudu.voice.semantic.engine.SemanticEngine;
import com.dudu.workflow.common.DataFlowFactory;
import com.dudu.workflow.common.RequestFactory;
import com.dudu.workflow.driving.DrivingFlow;
import com.dudu.workflow.obd.CarCheckType;
import com.dudu.workflow.obd.FaultCodeFlow;
import com.dudu.workflow.obd.VehicleConstants;
import com.dudu.workflow.tpms.TPMSFlow;
import com.dudu.workflow.tpms.TPMSInfo;
import com.dudu.workflow.tpms.TirePressureData;
import com.dudu.workflow.tpms.TpmsDataCallBack;
import com.dudu.workflow.tpms.TpmsDatasFlow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 故障清除
 * Created by robi 2016-03-14 17:25.
 */
public class VehicleAnimationFragment extends RBaseFragment implements View.OnClickListener {

    private LinearLayout animContainer;

    private ImageButton buttonBack;

    private ListView mFaultCodeListView;

    private ArrayList<FaultCodeDetailMessage> mFaultCodeData;

    private FaultCodeAdapter mFaultCodeAdapter;

    private Button mClearFaultCodeButton, mGoReplaceButton;

    private String[] category;

    private LinearLayout mClearingFaultCodeContainer;

    private TextView mClearingFaultCodeTextView;

    private LinearLayout mClearedFaultCodeContainer;

    private LinearLayout mFaultCodeDescribeContainer;

    private ListViewScrollbarView mListViewScrollbarView;

    private Logger logger = LoggerFactory.getLogger("VehicleAnimationFragment");

    private String[] categoryArrays;

    private List<AnimVideoView> animations;

    private String[] blueCategory;

    private boolean clearCodesOk = false;
    private Subscription mTpmsPairSub;
    private boolean mPairing;

    @Override
    protected int getContentView() {
        return R.layout.activity_vehicle_animation;
    }

    @Override
    protected void initViewData() {
        initListener();
        initListData();
        initData();
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
    }

    private void initListener() {
        buttonBack.setOnClickListener(this);
        mClearFaultCodeButton.setOnClickListener(this);
        mGoReplaceButton.setOnClickListener(this);
//        buttonBack.setEnabled(true);
        mFaultCodeListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                mListViewScrollbarView.setStartHeight((float) firstVisibleItem / totalItemCount);
            }
        });
    }

    @Override
    protected void initView(View rootView) {
        super.initView(rootView);
        animContainer = (LinearLayout) mViewHolder.v(R.id.vehicle_anim_container);
        buttonBack = (ImageButton) mViewHolder.v(R.id.vehicle_button_back);
        mFaultCodeListView = (ListView) mViewHolder.v(R.id.fault_code_listView);
        mClearFaultCodeButton = (Button) mViewHolder.v(R.id.button_clear_fault_code);
        mGoReplaceButton = (Button) mViewHolder.v(R.id.button_go_replace);
        mClearingFaultCodeContainer = (LinearLayout) mViewHolder.v(R.id.clearing_fault_code_container);
        mClearingFaultCodeTextView = (TextView) mViewHolder.v(R.id.clearing_fault_code_textview);
        mClearedFaultCodeContainer = (LinearLayout) mViewHolder.v(R.id.cleared_fault_code_container);
        mFaultCodeDescribeContainer = (LinearLayout) mViewHolder.v(R.id.fault_code_describe_container);
        mListViewScrollbarView = (ListViewScrollbarView) mViewHolder.v(R.id.fault_scroll_bar);
    }

    private void initData() {
        clearCodesOk = false;
        Bundle bundle = FragmentConstants.TEMP_ARGS;
        FragmentConstants.TEMP_ARGS = null;
        if (bundle == null) {
            return;
        }
        initAnimData(bundle);

        boolean clearCodes = bundle.getBoolean(VehicleConstants.CLEAR_CODES, false);
        if (!clearCodes) {
            showFaultCodeListView();
            loadFaultCode();
        } else {
            actionClearFaultCode();
        }
        broadcastFault(bundle);
    }

    private void initListData() {
        mFaultCodeData = new ArrayList<>();
        mFaultCodeAdapter = new FaultCodeAdapter(mBaseActivity, mFaultCodeData);
        mFaultCodeListView.setAdapter(mFaultCodeAdapter);
    }

    private void initAnimData(Bundle bundle) {
        category = bundle.getStringArray(VehicleConstants.VEHICLE);
        blueCategory = bundle.getStringArray(VehicleConstants.HAS_CLEARED_CODES);
        initAnimVideo(category);
    }

    private void broadcastFault(Bundle bundle) {
        String faultInfo = bundle.getString(VehicleConstants.VEHICLE_FAULT_INFO);
        VoiceManagerProxy.getInstance().stopUnderstanding();
        boolean askClearCodes = bundle.getBoolean(VehicleConstants.ASK_CLEAR_CODES);
        boolean codesTypesIs4 = bundle.getBoolean(VehicleConstants.CODES_TYPES_IS_4);
        if (askClearCodes) {
            if (codesTypesIs4) {
                VoiceManagerProxy.getInstance().startSpeaking("为您检测到" + faultInfo + "故障，是否清除故障码", TTSType.TTS_START_UNDERSTANDING, false);
            } else {
                VoiceManagerProxy.getInstance().startSpeaking("为您检测到" + faultInfo + "故障，请检查", TTSType.TTS_DO_NOTHING, false);
            }
        }
        SemanticEngine.getProcessor().switchSemanticType(SceneType.CAR_CHECKING);
    }

    private void initAnimVideo(String[] category) {
        animations = new ArrayList<>();
        categoryArrays = category;
        if (categoryArrays != null) {
            int categorySize = categoryArrays.length;
            if (categorySize > 0) {
                RelativeLayout.LayoutParams animContainerParams = null;
                LinearLayout animContainerOneRow = new LinearLayout(mBaseActivity);
                LinearLayout.LayoutParams animContainerOneRowParams = null;
                LinearLayout animContainerTwoRow = new LinearLayout(mBaseActivity);
                LinearLayout.LayoutParams animContainerTwoRowParams = null;
                LinearLayout.LayoutParams animParams = null;
                if (categorySize == 1) {
                    animContainerParams = new RelativeLayout.LayoutParams(320, 320);
                    animContainerOneRowParams = new LinearLayout.LayoutParams(320, 320);
                    animParams = new LinearLayout.LayoutParams(300, 300);
                    animParams.setMargins(5, 5, 5, 5);
                } else if (categorySize == 2) {
                    animContainerParams = new RelativeLayout.LayoutParams(500, 260);
                    animContainerOneRowParams = new LinearLayout.LayoutParams(500, 260);
                    animParams = new LinearLayout.LayoutParams(240, 240);
                    animParams.setMargins(5, 5, 5, 5);
                } else if (categorySize == 3) {
                    animContainerParams = new RelativeLayout.LayoutParams(420, 420);
                    animContainerOneRowParams = new LinearLayout.LayoutParams(420, 210);
                    animContainerTwoRowParams = new LinearLayout.LayoutParams(420, 210);
                    animParams = new LinearLayout.LayoutParams(200, 200);
                    animParams.setMargins(5, 5, 5, 5);
                } else if (categorySize == 4) {
                    animContainerParams = new RelativeLayout.LayoutParams(420, 420);
                    animContainerOneRowParams = new LinearLayout.LayoutParams(420, 210);
                    animContainerTwoRowParams = new LinearLayout.LayoutParams(420, 210);
                    animParams = new LinearLayout.LayoutParams(200, 200);
                    animParams.setMargins(5, 5, 5, 5);
                } else if (categorySize == 5) {
                    animContainerParams = new RelativeLayout.LayoutParams(630, 420);
                    animContainerOneRowParams = new LinearLayout.LayoutParams(630, 210);
                    animContainerTwoRowParams = new LinearLayout.LayoutParams(630, 210);
                    animParams = new LinearLayout.LayoutParams(200, 200);
                    animParams.setMargins(5, 5, 5, 5);
                }
                animContainer.setLayoutParams(animContainerParams);
                animContainerOneRowParams.setLayoutDirection(LinearLayout.HORIZONTAL);
                animContainerOneRow.setLayoutParams(animContainerOneRowParams);
                if (animContainerTwoRowParams != null) {
                    animContainerTwoRow.setLayoutParams(animContainerTwoRowParams);
                    animContainerTwoRowParams.setLayoutDirection(LinearLayout.HORIZONTAL);
                }
                for (int i = 0; i < categoryArrays.length; i++) {

                    AnimVideoView animVideoView = new AnimVideoView(getActivity());
                    if (blueCategory != null && blueCategory.length > 0) {
                        for (int j = 0; j < blueCategory.length; j++) {
                            if (blueCategory[j].equals(categoryArrays[i])) {
                                animVideoView.setType(VehicleConstants.VEHICLE_BLUE);
                            }
                        }
                    }
                    animVideoView.setCategory(categoryArrays[i]);
                    animations.add(animVideoView);
                    if (i == 0) {
                        animContainerOneRow.addView(animVideoView, animParams);
                    } else if (i == 1) {
                        animContainerOneRow.addView(animVideoView, animParams);
                    } else if (i == 2) {
                        animContainerTwoRow.addView(animVideoView, animParams);
                    } else if (i == 3) {
                        animContainerTwoRow.addView(animVideoView, animParams);
                    } else if (i == 4) {
                        animContainerOneRow.addView(animVideoView, animParams);
                    }
                }

                if (categorySize == 1 || categorySize == 2) {
                    animContainer.addView(animContainerOneRow);
                } else if (categorySize == 3 || categorySize == 4 || categorySize == 5) {
                    animContainer.addView(animContainerOneRow);
                    animContainer.addView(animContainerTwoRow);
                }
            }
        }
        Observable.timer(500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            startAnimVideo();
        }, throwable -> logger.error("mAnimationView.startAnim", throwable));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.vehicle_button_back:
//                buttonBack.setEnabled(false);
                VoiceManagerProxy.getInstance().stopSpeaking();
                VoiceManagerProxy.getInstance().onStop();
                replaceFragment(FragmentConstants.CAR_CHECKING);
                break;
            case R.id.button_clear_fault_code:
                actionClearFaultCode();
                break;
            case R.id.button_go_replace:
                goTo4s(false);
               /* blueCategory=new String[]{"eng","abs","gea"};
                showBlueAnim();*/
                break;
        }

    }

    private void goTo4s(boolean afterClear) {
        if (!CarCheckingProxy.getInstance().isCheckingFaults() && !CarCheckingProxy.getInstance().isClearingFault() && !clearCodesOk) {
            VoiceManagerProxy.getInstance().stopSpeaking();
            VoiceManagerProxy.getInstance().onStop();
            Bundle args = new Bundle();
            args.putStringArray(VehicleConstants.HAS_CLEARED_CODES, blueCategory);
            args.putBoolean(VehicleConstants.CLEAR_CODES, false);
            args.putBoolean(VehicleConstants.ASK_CLEAR_CODES, false);
            args.putBoolean(VehicleConstants.CODES_TYPES_IS_4, false);
            args.putBoolean(VehicleConstants.AFTER_CLEAR_CODES, afterClear);
            args.putStringArray(VehicleConstants.VEHICLE, category);
            args.putBoolean(VehicleConstants.FROM_FAULT_LIST, true);
            FragmentConstants.TEMP_ARGS = args;
            replaceFragment(FragmentConstants.REPAIR_FAULT_CODE_FRAGMENT);
            finish();
        }
    }

    private void showBlueAnim() {
        stopVehicleAnim();
        initAnimVideo(category);
    }

    private void stopVehicleAnim() {
        if (animations != null && animations.size() > 0) {
            for (AnimVideoView animation : animations) {
                if (animation != null) {
                    animation.stopPlay();
                }
            }
        }
        if (animContainer != null && animContainer.getChildCount() > 0) {
            animContainer.removeAllViews();
            animations = null;
        }
    }

    private void startAnimVideo() {
        if (animations != null && animations.size() > 0) {
            for (AnimVideoView animVideoView : animations) {
                animVideoView.startPlay();
            }
        }
    }

    private void actionClearFaultCode() {
        if (!CarCheckingProxy.getInstance().isCheckingFaults() && !CarCheckingProxy.getInstance().isClearingFault() && !clearCodesOk) {
            CarCheckType[] carCheckTypes = getCarCheckTypes();
            if (carCheckTypes.length > 0) {
                CarCheckingProxy.getInstance().clearFault(carCheckTypes);
                VoiceManagerProxy.getInstance().stopSpeaking();
                VoiceManagerProxy.getInstance().onStop();
                showClearingFaultCodeView();
            }
        }
    }

    private CarCheckType[] getCarCheckTypes() {
        List<CarCheckType> carCheckTypeList = new ArrayList<>();
        if (categoryArrays != null) {
            for (String category : categoryArrays) {
                CarCheckType carType = FaultCodeFlow.getCarCheckType(category);
                if (CarCheckType.WSB != carType) {
                    carCheckTypeList.add(carType);
                }
            }
        }
        return carCheckTypeList.toArray(new CarCheckType[carCheckTypeList.size()]);
    }

    private void showClearingFaultCodeView() {
        VoiceManagerProxy.getInstance().stopUnderstanding();
        VoiceManagerProxy.getInstance().startSpeaking("正在清除故障码", TTSType.TTS_DO_NOTHING, false);

        mClearingFaultCodeTextView.setText(R.string.clearing_fault_code);
        mFaultCodeDescribeContainer.setVisibility(View.GONE);
        mClearingFaultCodeContainer.setVisibility(View.VISIBLE);
        mClearedFaultCodeContainer.setVisibility(View.GONE);
    }

    private void showClearFaultCodeFailView(int warningText) {
        mClearingFaultCodeTextView.setText(warningText);
        mFaultCodeDescribeContainer.setVisibility(View.GONE);
        mClearingFaultCodeContainer.setVisibility(View.VISIBLE);
        mClearedFaultCodeContainer.setVisibility(View.GONE);
    }


    private void showClearedFaultCodeView() {
        mClearingFaultCodeContainer.setVisibility(View.GONE);
        mClearedFaultCodeContainer.setVisibility(View.VISIBLE);
        mFaultCodeDescribeContainer.setVisibility(View.GONE);
    }

    private void showFaultCodeListView() {
        mClearingFaultCodeContainer.setVisibility(View.GONE);
        mClearedFaultCodeContainer.setVisibility(View.GONE);
        mFaultCodeDescribeContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onShow() {
        super.onShow();
        logger.debug("VehicleAnimationFragment-onShow()");
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        logger.debug("VehicleAnimationFragment-onResume()");
    }

    @Override
    public void onHide() {
        super.onHide();
        logger.debug("VehicleAnimationFragment--onHide()");
        CarCheckingProxy.getInstance().cancelChecking();
        CarCheckingProxy.getInstance().cancelClearFaults();
        stopVehicleAnim();
        blueCategory = null;
    }

    private void loadFaultCode() {
        mFaultCodeData = new ArrayList<>();
        DataFlowFactory.getDrivingFlow().getAllFaultCodes()
                .zipWith(DataFlowFactory.getUserMessageFlow().obtainUserMessage()
                                .map(userMessage -> userMessage.getCarTypeName())
                                .map(carTypeName -> TextVerify.isEmpty(carTypeName) ? "ALL" : carTypeName)
                        , (faultCodeMessage, carTypeName1) -> {
                            logger.debug("getAllFaultCodes:" + faultCodeMessage);
                            List<FaultCodeDetailMessage> faultCodeDetailMessages = DrivingFlow.initEmptyFaultCodes(faultCodeMessage);
                            mFaultCodeData.addAll(faultCodeDetailMessages);
                            reflashData();
                            if (faultCodeMessage != null && faultCodeMessage.size() > 0) {
                                RequestFactory.getDrivingRequest().inquiryFault(faultCodeMessage, carTypeName1)
                                        .doOnNext(faultCodeDetailMessages1 -> logger.debug("loadFaultCode response:" + faultCodeDetailMessages1.toString()))
                                        .subscribeOn(Schedulers.newThread())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(
                                                faultCodeResponse -> mFaultCodeData = DrivingFlow.filterFaultCodeDetailMessage(faultCodeResponse.result, mFaultCodeData)
                                                , throwable -> {
                                                    logger.error("loadFaultCode inquiryFault", throwable);
                                                    reflashData();
                                                }
                                                , () -> {
                                                    reflashData();
                                                }
                                        );
                            }
                            return mFaultCodeData;
                        })
                .subscribe(faultCodeData -> logger.debug("loadFaultCode DataFlowFactory getFaultCodes faultCodeData.size{}" + faultCodeData.size()),
                        throwable -> logger.error("loadFaultCode DataFlowFactory getFaultCodes", throwable),
                        () -> logger.debug("loadFaultCode DataFlowFactory loadFaultCode onComplete"));
        TpmsDatasFlow.findAllTirePressureDatas(new TpmsDataCallBack() {

            @Override
            public void onDatas(List<TirePressureData> result) {
                mFaultCodeData.addAll(0, TpmsDatasFlow.tirePressureDataRealmsToFaultCodeDetailMessages(result));
                reflashData();
            }

            @Override
            public void onError(Exception error) {
                logger.error("loadFaultCode", error);
            }
        });
    }

    private void reflashData() {
        mListViewScrollbarView.setHeight((float) 4 / mFaultCodeData.size());
        mFaultCodeAdapter.setData(mFaultCodeData);
    }


    public void onEventMainThread(ClearFaultResultEvent event) {
        switch (event.getResult()) {
            case ClearFaultResultEvent.CLEAR_OK:
                clearCodesOk = true;
                compareClearedResultWithBefore();
                showClearedFaultCodeView();
                break;
            case ClearFaultResultEvent.CLEAR_ERROR:
                compareClearedResultWithBefore();
                showClearFaultCodeFailView(R.string.fault_code_clear_fail);
                break;
            case ClearFaultResultEvent.CLEAR_NOT_SUPPORT:
                compareClearedResultWithBefore();
                checkAuditStatus();
                break;
            case ClearFaultResultEvent.CLEAR_HAS_CODES:
                compareClearedResultWithBefore();
                showClearFaultCodeFailView(R.string.fault_code_clear_fail);
//                goTo4s(true);
                break;
            case ClearFaultResultEvent.START_CLEAR:
                actionClearFaultCode();
                break;
        }
    }

    private void checkAuditStatus() {
        RequestFactory.getDrivingRequest()
                .getCarBrand()
                .map(getCarBrandResponse -> getCarBrandResponse.result.audit_state)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(auditState -> {
                    switch (auditState) {
                        case GetCarBrandResponse.AUDIT_STATE_UNAUDITED:
                            showClearFaultCodeFailView(R.string.fault_code_clear_fail_upload_license);
                            break;
                        case GetCarBrandResponse.AUDIT_STATE_AUDITING:
                            showClearFaultCodeFailView(R.string.fault_code_clear_fail_wait_checking);
                            break;
                        case GetCarBrandResponse.AUDIT_STATE_AUDITED:
                            showClearFaultCodeFailView(R.string.fault_code_clear_fail);
                            break;
                        case GetCarBrandResponse.AUDIT_STATE_REJECT:
                            showClearFaultCodeFailView(R.string.fault_code_clear_fail_upload_license);
                            break;
                    }
                }, throwable -> {
                    logger.error("checkAuditStatus", throwable);
                    showClearFaultCodeFailView(R.string.fault_code_clear_fail);
                });
    }

    private void compareClearedResultWithBefore() {
        VoiceManagerProxy.getInstance().startSpeaking(
                CommonLib.getInstance().getContext().getString(R.string.clear_faults_fail), TTSType.TTS_DO_NOTHING, false);
        DataFlowFactory.getDrivingFlow()
                .getAllFaultCodes()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(faultCodes -> {
                    List<String> blueCategoryList = new ArrayList<String>();
                    for (String faultCodeString : categoryArrays) {
                        boolean faultCodeStringClearFailed = false;
                        for (FaultCode faultCode : faultCodes) {
                            if ((faultCode.getCarCheckType() == FaultCode.ABS && faultCodeString.equals(VehicleConstants.VEHICLE_ABS)) ||
                                    (faultCode.getCarCheckType() == FaultCode.ECM && faultCodeString.equals(VehicleConstants.VEHICLE_ENG)) ||
                                    (faultCode.getCarCheckType() == FaultCode.SRS && faultCodeString.equals(VehicleConstants.VEHICLE_SRS)) ||
                                    (faultCode.getCarCheckType() == FaultCode.TCM && faultCodeString.equals(VehicleConstants.VEHICLE_GEA))
                                    ) {
                                faultCodeStringClearFailed = true;
                            }
                        }
                        if (faultCodeString.equals(VehicleConstants.VEHICLE_WSB)) {
                            TpmsDatasFlow.findAllTirePressureDatas(new TpmsDataCallBack() {

                                @Override
                                public void onDatas(List<TirePressureData> result) {
                                    if (result.size() == 0) {
                                        blueCategoryList.add(faultCodeString);
                                    }
                                }

                                @Override
                                public void onError(Exception error) {
                                    logger.error("startNextChecking", error);
                                }
                            });
                        } else if (!faultCodeStringClearFailed) {
                            blueCategoryList.add(faultCodeString);
                        }
                    }
                    blueCategory = blueCategoryList.toArray(new String[blueCategoryList.size()]);
                    showBlueAnim();
                }, throwable -> logger.error("compareClearedResultWithBefore", throwable));
    }

    private void pairStart(String tpmsCode, TextView statusTextView, TextView descriptionTextView) {
        if (mPairing) {
            return;
        }
        mTpmsPairSub = TPMSFlow.TPMSPairStream()
                .timeout(180, TimeUnit.SECONDS)
                .filter(position -> position == TpmsDatasFlow.getTPMSInfoPOSITION(tpmsCode))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    logger.debug("TPMS pair: " + pair);
                    pairingComplete(statusTextView, descriptionTextView, pair);
                }, throwable -> {
                    logger.error("mTpmsPairSub", throwable);
                    pairingFail(throwable instanceof TimeoutException, statusTextView, tpmsCode);
                });
        VoiceManagerProxy.getInstance().startSpeaking(TpmsDatasFlow.getTireChinese(tpmsCode) + "开始对码，请在3分钟之内完成", TTSType.TTS_DO_NOTHING, false);
        TPMSInfo.POSITION tpmsPosition = TpmsDatasFlow.getTPMSInfoPOSITION(tpmsCode);
        TPMSFlow.TPMSPairStart(tpmsPosition);
        statusTextView.setText("正在对码");
        mPairing = true;
    }

    private void pairingComplete(TextView textView, TextView descriptionTextView, TPMSInfo.POSITION pair) {
        VoiceManagerProxy.getInstance().startSpeaking(TpmsDatasFlow.getTirePairingCompleteWarning(pair), TTSType.TTS_DO_NOTHING, false);
        descriptionTextView.setText(TpmsDatasFlow.getTireChinese(pair.value()) + "对码成功");
        textView.setText("");
        textView.setClickable(false);
        mPairing = false;
        mTpmsPairSub.unsubscribe();
    }

    private void pairingFail(boolean timout, TextView statusTextView, String tpmsCode) {
        if (timout) {
            VoiceManagerProxy.getInstance().startSpeaking(TpmsDatasFlow.getTirePairingTimoutWarning(tpmsCode), TTSType.TTS_DO_NOTHING, false);
        } else {
            VoiceManagerProxy.getInstance().startSpeaking("对码失败，请重试", TTSType.TTS_DO_NOTHING, false);
        }
        statusTextView.setText("点击对码");
        statusTextView.setClickable(true);
        mPairing = false;
        mTpmsPairSub.unsubscribe();
    }

    private class FaultCodeAdapter extends BaseAdapter {

        private Context context;

        private ArrayList<FaultCodeDetailMessage> data;

        private LayoutInflater inflater;

        public FaultCodeAdapter(Context context, ArrayList<FaultCodeDetailMessage> data) {
            this.context = context;
            this.data = data;
            inflater = LayoutInflater.from(context);

        }

        public void setData(ArrayList<FaultCodeDetailMessage> data) {
            this.data = (ArrayList<FaultCodeDetailMessage>) data.clone();
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
                convertView = inflater.inflate(R.layout.fault_code_item, parent, false);
                holder.tvFaultCode = (TextView) convertView.findViewById(R.id.text_fault_code);
                holder.tvFaultCodeDescribe = (TextView) convertView.findViewById(R.id.text_fault_code_describe);
                holder.tvFaultCodeStatus = (TextView) convertView.findViewById(R.id.text_fault_code_status);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            FaultCodeDetailMessage faultQueryResponse = data.get(position);
            holder.tvFaultCode.setText(faultQueryResponse.faultCode);
            holder.tvFaultCodeDescribe.setText(faultQueryResponse.faultInfo);
            if (TpmsDatasFlow.isTireFaultType(faultQueryResponse.faultCode)) {
                if (faultQueryResponse.dataIsEmpty) {
                    holder.tvFaultCodeStatus.setText("点击对码");
                    holder.tvFaultCodeStatus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pairStart(faultQueryResponse.faultCode, holder.tvFaultCodeStatus, holder.tvFaultCodeDescribe);
                        }
                    });
                    holder.tvFaultCodeStatus.setClickable(true);
                } else {
                    holder.tvFaultCodeStatus.setText("");
                }
            } else {
                holder.tvFaultCodeStatus.setText("待定");
                holder.tvFaultCodeStatus.setClickable(false);
            }
            return convertView;
        }

        class ViewHolder {
            TextView tvFaultCode;
            TextView tvFaultCodeDescribe;
            TextView tvFaultCodeStatus;
        }

    }

}
