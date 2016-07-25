package com.dudu.aios.ui.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dudu.aios.ui.fragment.base.RBaseFragment;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.aios.ui.vehicle.SearchAddress;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.LogUtils;
import com.dudu.commonlib.utils.TextVerify;
import com.dudu.event.ChooseEvent;
import com.dudu.map.NavigationProxy;
import com.dudu.navi.entity.Navigation;
import com.dudu.navi.entity.PoiResultInfo;
import com.dudu.navi.entity.Point;
import com.dudu.navi.vauleObject.NaviDriveMode;
import com.dudu.navi.vauleObject.NavigationType;
import com.dudu.rest.model.driving.response.FaultCodeDetailMessage;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.TTSType;
import com.dudu.workflow.common.DataFlowFactory;
import com.dudu.workflow.common.RequestFactory;
import com.dudu.workflow.driving.DrivingFlow;
import com.dudu.workflow.obd.VehicleConstants;
import com.dudu.workflow.tpms.TirePressureData;
import com.dudu.workflow.tpms.TpmsDataCallBack;
import com.dudu.workflow.tpms.TpmsDatasFlow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import hugo.weaving.DebugLog;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 汽车修理
 * Created by robi on 2016-03-14 17:38.
 */
public class RepairFaultCodeFragment extends RBaseFragment implements View.OnClickListener {

    private static final String TAG = "RepairFaultCodeFragment";
    private static final int EVERY_PAGE_COUNT = 3;
    private ImageButton mBackButton;
    private ListView mRepairShopList;
    private VehicleAdapter mVehicleAdapter;
    private View mReflashView;
    private TextView mReflashTextView;
    private ArrayList<PoiResultInfo> vehicleData;
    private SearchAddress address;
    private ListView mUnclearFaultCodeListView;
    private UnclearFaultCodeAdapter mUnclearFaultCodeAdapter;
    private ArrayList<FaultCodeDetailMessage> mUnclearFaultCodeData;
    private LinearLayout mCheckFaultCodeContainer;
    private LinearLayout mUnclearFaultCodeContainer;
    private TextView mCheckFaultCodeText;
    private Logger logger = LoggerFactory.getLogger("car.RepairFaultCodeFragment");
    private int pageIndex = 1;
    private int totalPage;

    @Override
    protected int getContentView() {
        return R.layout.activity_repair_fault_code;
    }

    @Override
    protected void initView(View rootView) {
        super.initView(rootView);
        mBackButton = (ImageButton) mViewHolder.v(R.id.button_back);
        mRepairShopList = (ListView) mViewHolder.v(R.id.repair_shop_listView);
        mReflashView = mViewHolder.v(R.id.layout_reflash);
        mReflashTextView = (TextView) mViewHolder.v(R.id.reflash_textview);

        mUnclearFaultCodeListView = (ListView) mViewHolder.v(R.id.unclear_fault_code_listView);
        mCheckFaultCodeContainer = (LinearLayout) mViewHolder.v(R.id.check_fault_code_container);
        mCheckFaultCodeText = (TextView) mViewHolder.v(R.id.text_check_fault_code);
        mUnclearFaultCodeContainer = (LinearLayout) mViewHolder.v(R.id.fault_code_describe_container);

        EventBus.getDefault().register(this);
        noticeClearFail();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                boolean fromFaultList = FragmentConstants.TEMP_ARGS.getBoolean(VehicleConstants.FROM_FAULT_LIST, true);
                if (fromFaultList) {
                    replaceFragment(FragmentConstants.VEHICLE_ANIMATION_FRAGMENT);
                } else {
                    FragmentConstants.TEMP_ARGS = null;
                    replaceFragment(FragmentConstants.CAR_CHECKING);
                }
                VoiceManagerProxy.getInstance().stopSpeaking();
                VoiceManagerProxy.getInstance().onStop();
                onHide();
                break;
            case R.id.text_check_fault_code:
                showFaultCodeListView();
                break;
            case R.id.layout_reflash:
                mReflashTextView.setText("正在搜索...");
                loadRepairShopData();
                break;
        }
    }

    @DebugLog
    @Override
    public void onResume() {
        super.onResume();
        LogUtils.v(TAG, "onResume()...");
        loadRepairShopData();
        loadUnclearFaultCodeData();
    }

    @Override
    protected void initEvent() {
        super.initEvent();
        mBackButton.setOnClickListener(this);
        mCheckFaultCodeText.setOnClickListener(this);
        mReflashView.setOnClickListener(this);
        mRepairShopList.setOnItemClickListener((parent, view, position, id) -> startNavi(position));
        mRepairShopList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 16) {
                    pageIndex = 7;
                    return;
                }
                pageIndex = (int) Math.floor((firstVisibleItem + 1) / EVERY_PAGE_COUNT) + 1;
            }
        });
    }

    @Override
    protected void initViewData() {
        LogUtils.v(TAG, "initViewData()");
        vehicleData = new ArrayList<>();
        mVehicleAdapter = new VehicleAdapter(mBaseActivity, vehicleData);
        mRepairShopList.setAdapter(mVehicleAdapter);
        loadRepairShopData();
        mUnclearFaultCodeData = new ArrayList<>();
        mUnclearFaultCodeAdapter = new UnclearFaultCodeAdapter(mBaseActivity, mUnclearFaultCodeData);
        mUnclearFaultCodeListView.setAdapter(mUnclearFaultCodeAdapter);
        loadUnclearFaultCodeData();
    }

    private void loadRepairShopData() {
        vehicleData = new ArrayList<>();
        address = new SearchAddress(mBaseActivity);
        address.search("汽车修理店");
        address.setOnGestureLockViewListener(new SearchAddress.OnObtainAddressListener() {
            @Override
            public void onAddress(List<PoiResultInfo> poiResultList) {
                LogUtils.v(TAG, "获取修理店列表的大小:" + poiResultList.size());
                if (poiResultList != null && poiResultList.size() != 0) {
                    vehicleData.addAll(poiResultList);
                    mVehicleAdapter.setData(vehicleData);
                    totalPage = (int) Math.floor(vehicleData.size() / EVERY_PAGE_COUNT) + 1;
                    mRepairShopList.setVisibility(View.VISIBLE);
                    mReflashView.setVisibility(View.GONE);
                } else {
                    mRepairShopList.setVisibility(View.GONE);
                    mReflashView.setVisibility(View.VISIBLE);
                    mReflashTextView.setText("点击刷新");
                }
            }
        });
    }

    private void loadUnclearFaultCodeData() {
        mUnclearFaultCodeData = new ArrayList<>();
        DataFlowFactory.getDrivingFlow().getAllFaultCodes()
                .zipWith(DataFlowFactory.getUserMessageFlow().obtainUserMessage()
                                .map(userMessage -> userMessage.getCarTypeName())
                                .map(carTypeName -> TextVerify.isEmpty(carTypeName) ? "ALL" : carTypeName)
                        , (faultCodeMessage, carTypeName1) -> {
                            List<FaultCodeDetailMessage> faultCodeDetailMessages = DrivingFlow.initEmptyFaultCodes(faultCodeMessage);
                            mUnclearFaultCodeData.addAll(faultCodeDetailMessages);
                            mUnclearFaultCodeAdapter.setData(mUnclearFaultCodeData);
                            RequestFactory.getDrivingRequest().inquiryFault(faultCodeMessage, carTypeName1)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            faultCodeResponse -> mUnclearFaultCodeData = DrivingFlow.filterFaultCodeDetailMessage(faultCodeResponse.result, mUnclearFaultCodeData)
                                            , throwable -> {
                                                logger.error("loadFaultCode inquiryFault", throwable);
                                                mUnclearFaultCodeAdapter.setData(mUnclearFaultCodeData);
                                            }
                                            , () -> {
                                                mUnclearFaultCodeAdapter.setData(mUnclearFaultCodeData);
                                            }
                                    );
                            return mUnclearFaultCodeData;
                        })
                .subscribe(faultCodeData -> logger.debug("loadUnclearFaultCodeData getFaultCodes faultCodeData.size{}" + faultCodeData.size()),
                        throwable -> logger.error("loadUnclearFaultCodeData getFaultCodes", throwable),
                        () -> logger.debug("loadUnclearFaultCodeData getFaultCodes onComplete"));
        TpmsDatasFlow.findAllTirePressureDatas(new TpmsDataCallBack() {

            @Override
            public void onDatas(List<TirePressureData> result) {
                mUnclearFaultCodeData.addAll(TpmsDatasFlow.tirePressureDataRealmsToFaultCodeDetailMessages(result));
                mUnclearFaultCodeAdapter.setData(mUnclearFaultCodeData);
            }

            @Override
            public void onError(Exception error) {
                logger.error("loadFaultCode", error);
            }
        });

    }

    private void showFaultCodeListView() {
        mUnclearFaultCodeContainer.setVisibility(View.VISIBLE);
        mCheckFaultCodeContainer.setVisibility(View.GONE);
    }

    private void appearFaultCodeListView() {
        mUnclearFaultCodeContainer.setVisibility(View.GONE);
        mCheckFaultCodeContainer.setVisibility(View.VISIBLE);
    }

    private void startNavi(int position) {
        Navigation navigation = new Navigation(new Point(vehicleData.get(position).getLatitude(),
                vehicleData.get(position).getLongitude()), NaviDriveMode.FASTESTTIME, NavigationType.NAVIGATION);
        NavigationProxy.getInstance().startNavigation(navigation);
        onHide();
    }

    @Override
    public void onHide() {
        super.onHide();
        LogUtils.v(TAG, "onHide()..");
        // mBaseActivity.showTitleColorTransparent(true);
        EventBus.getDefault().unregister(this);
        clearListView();
    }

    @Override
    public void onShow() {
        LogUtils.v(TAG, "onShow()..");
        EventBus.getDefault().register(this);
        loadRepairShopData();
        loadUnclearFaultCodeData();
        noticeClearFail();

    }

    private void clearListView() {
        appearFaultCodeListView();
        mUnclearFaultCodeData.clear();
        mUnclearFaultCodeAdapter.setData(mUnclearFaultCodeData);

        vehicleData.clear();
        mVehicleAdapter.setData(vehicleData);

    }

    private void noticeClearFail() {
        boolean afterClearCodes = FragmentConstants.TEMP_ARGS.getBoolean(VehicleConstants.AFTER_CLEAR_CODES, false);
        if (afterClearCodes) {
            VoiceManagerProxy.getInstance().clearMisUnderstandCount();
            VoiceManagerProxy.getInstance().startSpeaking(getString(R.string.fault_clear_fail_notice), TTSType.TTS_START_UNDERSTANDING, false);
        } else {
            showFaultCodeListView();
        }
        FragmentConstants.TEMP_ARGS.putBoolean(VehicleConstants.AFTER_CLEAR_CODES, false);
    }

    public void onEvent(ChooseEvent event) {
        switch (event.getChooseType()) {
            case CHOOSE_NUMBER:
                chooseListNavi(event.getPosition());
                break;
            case CHOOSE_PAGE:
                chooseListPage(event.getPosition());
                break;
            case NEXT_PAGE:
                nextPage();
                break;
            case PREVIOUS_PAGE:
                previousPage();
                break;
            case LAST_ONE:
                break;
            case LAST_PAGE:
                break;
        }

    }

    private void previousPage() {
        if (pageIndex == 1) {
            VoiceManagerProxy.getInstance().stopUnderstanding();
            VoiceManagerProxy.getInstance().startSpeaking("已经是第一页了", TTSType.TTS_START_UNDERSTANDING, false);
            return;
        }
        pageIndex--;
        mRepairShopList.setSelection(((pageIndex - 1)) * EVERY_PAGE_COUNT);
    }

    private void nextPage() {
        if (pageIndex >= totalPage) {
            VoiceManagerProxy.getInstance().stopUnderstanding();
            VoiceManagerProxy.getInstance().startSpeaking("已经是最后一页了", TTSType.TTS_START_UNDERSTANDING, false);
            return;
        }
        pageIndex++;
        mRepairShopList.setSelection((pageIndex - 1) * EVERY_PAGE_COUNT);
    }

    private void chooseListNavi(int number) {
        if (number < 0 | number > 20) {
            VoiceManagerProxy.getInstance().stopUnderstanding();
            VoiceManagerProxy.getInstance().startSpeaking("选择错误，请重新选择", TTSType.TTS_START_UNDERSTANDING, false);
            return;
        }
        startNavi(number - 1);
    }

    private void chooseListPage(int pageCount) {
        if (pageCount < 0 || pageCount > 7) {
            VoiceManagerProxy.getInstance().stopUnderstanding();
            VoiceManagerProxy.getInstance().startSpeaking("选择错误，请重新选择", TTSType.TTS_START_UNDERSTANDING, false);
            return;
        }
        mRepairShopList.setSelection((pageCount - 1) * EVERY_PAGE_COUNT);
    }

    private class VehicleAdapter extends BaseAdapter {
        private Context context;

        private ArrayList<PoiResultInfo> data;

        private LayoutInflater inflater;

        public VehicleAdapter(Context context, ArrayList<PoiResultInfo> data) {
            this.context = context;
            this.data = data;
            inflater = LayoutInflater.from(context);
        }

        public void setData(ArrayList<PoiResultInfo> data) {
            this.data = (ArrayList<PoiResultInfo>) data.clone();
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
                convertView = inflater.inflate(R.layout.repair_shop_item, parent, false);
                holder.tvName = (TextView) convertView.findViewById(R.id.repair_shop_name);
                holder.tvDistance = (TextView) convertView.findViewById(R.id.repair_shop_distance);
                holder.gradeContainer = (LinearLayout) convertView.findViewById(R.id.grade_container);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            PoiResultInfo vehicle = data.get(position);
            holder.tvName.setText((position + 1) + "." + vehicle.getAddressTitle());
            DecimalFormat df = new java.text.DecimalFormat("#.##");
            double distance = vehicle.getDistance();
            String unit = "M";
            if (distance >= 1000) {
                distance = distance / 1000;
                unit = "KM";
            }
            holder.tvDistance.setText(getResources().getString(R.string.distance_ch) + df.format(distance) + unit);
            for (int i = 1; i <= 5; i++) {
                ImageView imageView = new ImageView(context);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(5, 0, 5, 0);
                imageView.setLayoutParams(params);
                if (4 >= i) {
                    imageView.setImageResource(R.drawable.star_full);
                } else {
                    imageView.setImageResource(R.drawable.star_null);
                }
                holder.gradeContainer.addView(imageView);
            }
            convertView.setOnClickListener(v -> {
                Navigation navigation = new Navigation(new Point(data.get(position).getLatitude(), data.get(position).getLongitude()),
                        NaviDriveMode.SPEEDFIRST, NavigationType.NAVIGATION);
                NavigationProxy.getInstance().startNavigation(navigation);
            });
            return convertView;
        }

        private class ViewHolder {
            TextView tvName;
            TextView tvDistance;
            LinearLayout gradeContainer;
        }
    }

    private class UnclearFaultCodeAdapter extends BaseAdapter {

        private Context context;

        private ArrayList<FaultCodeDetailMessage> data;

        private LayoutInflater inflater;

        public UnclearFaultCodeAdapter(Context context, ArrayList<FaultCodeDetailMessage> data) {
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
                convertView = inflater.inflate(R.layout.unclear_fault_code_item, parent, false);
                holder.tvFaultCode = (TextView) convertView.findViewById(R.id.text_unclear_fault_code);
                holder.tvFaultCodeDescribe = (TextView) convertView.findViewById(R.id.text_unclear_fault_code_describe);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            FaultCodeDetailMessage response = data.get(position);
            holder.tvFaultCode.setText(response.faultCode);
            holder.tvFaultCodeDescribe.setText(response.faultInfo);
            return convertView;
        }

        class ViewHolder {
            TextView tvFaultCode;
            TextView tvFaultCodeDescribe;
        }
    }
}
