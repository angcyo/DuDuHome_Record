package com.dudu.aios.ui.map.observable;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.dudu.aios.ui.map.MapDbHelper;
import com.dudu.aios.ui.map.MyLinearLayoutManager;
import com.dudu.aios.ui.map.adapter.AddressResultAdapter;
import com.dudu.aios.ui.map.adapter.RouteStrategyAdapter;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.databinding.ActivityAddressSearchBinding;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.event.ChooseEvent;
import com.dudu.map.NavigationProxy;
import com.dudu.navi.NavigationManager;
import com.dudu.navi.entity.Navigation;
import com.dudu.navi.entity.PoiResultInfo;
import com.dudu.navi.entity.Point;
import com.dudu.navi.vauleObject.NavigationType;
import com.dudu.navi.vauleObject.SearchType;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.constant.TTSType;
import com.dudu.voice.semantic.engine.SemanticEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;

/**
 * Created by Robi on 2016-03-18 11:55.
 */
public class AddressResultObservable {

    public final ObservableBoolean isManual = new ObservableBoolean();

    public final ObservableField<String> resultCount = new ObservableField<>();

    public final ObservableBoolean showAddressList = new ObservableBoolean();

    public final ObservableBoolean showStrategy = new ObservableBoolean();

    private ActivityAddressSearchBinding binding;

    private NavigationProxy navigationProxy;

    private NavigationManager navigationManager;

    private Context mContext;

    private AddressResultAdapter addressResultAdapter;

    private Subscription chooseStrategyMethodSub = null;

    private Subscription chooseAddressSub = null;

    private ArrayList<MapListItemObservable> mapList;

    private Logger logger = LoggerFactory.getLogger("lbs.navi");

    private int pageIndex;

    private String playText;

    public AddressResultObservable(ActivityAddressSearchBinding binding) {

        this.binding = binding;
        navigationProxy = NavigationProxy.getInstance();
        navigationManager = NavigationManager.getInstance(binding.getRoot().getContext());
        mContext = binding.getRoot().getContext();
    }

    public void setDefault() {

        if (!navigationProxy.isNeedRefresh())
            return;

        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);

        chooseAddressSub = null;
        chooseStrategyMethodSub = null;

        isManual.set(navigationProxy.isManual());
        showAddressList.set(true);
        showStrategy.set(false);

        navigationProxy.setChooseStep(1);

        mapList = getMapList();

        resultCount.set(String.format(mContext.getResources().getString(R.string.navigation_address_count), mapList.size()));

        addressResultAdapter = new AddressResultAdapter(mapList, (view, position) -> {
            navigationProxy.endPoint = new Point(mapList.get(position).lat.get(), mapList.get(position).lon.get());
            VoiceManagerProxy.getInstance().stopSpeaking();
            VoiceManagerProxy.getInstance().onStop();
            chooseAddress(mapList.get(position).poiResult.get(), position);
        });

        navigationProxy.setShowList(true);
        binding.addressListView.setLayoutManager(new MyLinearLayoutManager(mContext));
        binding.addressListView.setAdapter(addressResultAdapter);
        showAddress();
    }

    private void showAddress() {

        if (mapList.size() > 1) {
            playText = String.format(binding.getRoot().getContext().getString(R.string.plaseChoose_place), mapList.size(), navigationManager.getKeyword());
        } else {
            playText = String.format(binding.getRoot().getContext().getString(R.string.plaseChoose_place_one), navigationManager.getKeyword());
        }
        if (!navigationProxy.isManual()) {
            VoiceManagerProxy.getInstance().clearMisUnderstandCount();
            VoiceManagerProxy.getInstance().stopUnderstanding();
            VoiceManagerProxy.getInstance().startSpeaking(playText, TTSType.TTS_START_UNDERSTANDING, false);
            SemanticEngine.getProcessor().switchSemanticType(SceneType.MAP_CHOISE);
        }

        navigationProxy.setNeedRefresh(false);
    }

    private ArrayList<MapListItemObservable> getMapList() {

        ArrayList<MapListItemObservable> list = new ArrayList<>();
        for (int i = 0; i < navigationManager.getPoiResultList().size(); i++) {
            PoiResultInfo poiResultInfo = navigationManager.getPoiResultList().get(i);
            MapListItemObservable mapListItemObservable = new MapListItemObservable(poiResultInfo, i + 1 + ".", !navigationProxy.isManual());
            list.add(mapListItemObservable);
        }
        return list;
    }

    public void chooseAddress(PoiResultInfo result, int position) {
        if (chooseAddressSub != null)
            return;
        chooseAddressSub = Observable.just(result).subscribe(poiResultInfo -> {

            if (position >= mapList.size() && !navigationProxy.isManual()) {
                VoiceManagerProxy.getInstance().startSpeaking(binding.getRoot().getContext().getString(R.string.choose_error),
                        TTSType.TTS_START_UNDERSTANDING, false);
                return;
            }
            navigationProxy.endPoint = new Point(poiResultInfo.getLatitude(), poiResultInfo.getLongitude());
            if (navigationManager.getSearchType() == SearchType.SEARCH_COMMONPLACE) {
                navigationProxy.addCommonAddress(poiResultInfo);
                return;
            }
            showStrategy();
            MapDbHelper.getDbHelper().saveHistory(mapList.get(position));
        }, throwable -> logger.error("chooseAddress", throwable));
    }

    private void showStrategy() {

        showAddressList.set(false);
        showStrategy.set(true);
        resultCount.set(String.format(mContext.getResources().getString(R.string.navigation_address_count), 6));

        if (!navigationProxy.isManual()) {
            SemanticEngine.getProcessor().switchSemanticType(SceneType.MAP_CHOISE);
            VoiceManagerProxy.getInstance().stopUnderstanding();
            VoiceManagerProxy.getInstance().clearMisUnderstandCount();
            VoiceManagerProxy.getInstance().startSpeaking(binding.getRoot().getContext().getString(R.string.plaseChoose_strategy),
                    TTSType.TTS_START_UNDERSTANDING, false);
        }

        navigationProxy.setChooseStep(2);

        binding.strategyChooseList.setAdapter(new RouteStrategyAdapter(mContext, navigationManager.getDriveModeList()));

        binding.strategyChooseList.setOnItemClickListener((parent, view, position, id) -> {

            VoiceManagerProxy.getInstance().stopSpeaking();
            VoiceManagerProxy.getInstance().onStop();
            navigationProxy.startNavigation(new Navigation(navigationProxy.endPoint,
                    navigationManager.getDriveModeList().get(position), NavigationType.NAVIGATION));
        });

    }

    public void chooseDriveMode(int position) {

        if (position >= 6) {
            VoiceManagerProxy.getInstance().startSpeaking(binding.getRoot().getContext().getString(R.string.choose_error),
                    TTSType.TTS_START_UNDERSTANDING, false);
            return;
        }

        if (navigationManager.getPoiResultList().isEmpty())
            return;

        if (chooseStrategyMethodSub != null) {
            return;
        }

        chooseStrategyMethodSub = Observable.just(position).subscribe(integer -> {

            navigationProxy.startNavigation(new Navigation(navigationProxy.endPoint,
                    navigationManager.getDriveModeList().get(position), NavigationType.NAVIGATION));
            VoiceManagerProxy.getInstance().onStop();
        }, throwable -> logger.error("chooseDriveMode", throwable));


    }


    public void onEventMainThread(ChooseEvent event) {

        MyLinearLayoutManager lm = (MyLinearLayoutManager) binding.addressListView.getLayoutManager();
        pageIndex = (int) Math.floor(lm.findFirstVisibleItemPosition() / Constants.ADDRESS_VIEW_COUNT);
        switch (event.getChooseType()) {

            case CHOOSE_NUMBER:
                chooseAddress(mapList.get(event.getPosition() - 1).poiResult.get(), event.getPosition() - 1);
                break;
            case STRATEGY_NUMBER:
                chooseDriveMode(event.getPosition() - 1);
                break;
            case NEXT_PAGE:
                nextPage(lm);
                break;
            case PREVIOUS_PAGE:
                previousPage(lm);
                break;
            case CHOOSE_PAGE:
                choosePage(event.getPosition());
                break;
            case LAST_PAGE:
                binding.addressListView.scrollToPosition(mapList.size() - 1);
                break;
        }

    }

    private void nextPage(MyLinearLayoutManager lm) {

        if (lm.findLastVisibleItemPosition() == mapList.size() - 1) {
            VoiceManagerProxy.getInstance().stopUnderstanding();
            VoiceManagerProxy.getInstance().startSpeaking("已经是最后一页", TTSType.TTS_START_UNDERSTANDING, false);
            return;
        }

        pageIndex++;
        int index = pageIndex * MapObservable.ADDRESS_VIEW_COUNT + 3;
        if (index > mapList.size() - 1) {
            index = mapList.size() - 1;
        }
        binding.addressListView.scrollToPosition(index);
    }

    private void previousPage(MyLinearLayoutManager lm) {
        if (lm.findFirstVisibleItemPosition() == 0) {
            VoiceManagerProxy.getInstance().stopUnderstanding();
            VoiceManagerProxy.getInstance().startSpeaking("已经是第一页", TTSType.TTS_START_UNDERSTANDING, false);
            return;
        }
        if (pageIndex >= 1)
            pageIndex--;
        binding.addressListView.scrollToPosition(pageIndex * MapObservable.ADDRESS_VIEW_COUNT);
    }

    private void choosePage(int page) {
        logger.debug("choosePage pageIndex:{}", pageIndex);

        int page_i = page * MapObservable.ADDRESS_VIEW_COUNT - 1;


        if (page - 1 < pageIndex) {
            page_i = page_i - 3;
        }

        if (page == 1) {
            page_i = 0;
        }

        if (page > 5 || page < 1 || page_i > mapList.size()) {
            VoiceManagerProxy.getInstance().stopUnderstanding();
            VoiceManagerProxy.getInstance().startSpeaking("选择错误，请重新选择", TTSType.TTS_START_UNDERSTANDING, false);
            return;
        }
        logger.debug("choosePage page_i:{}", page_i);

        binding.addressListView.scrollToPosition(0);

        binding.addressListView.scrollToPosition(page_i);
    }

    public void release() {
        EventBus.getDefault().unregister(this);
        navigationProxy.setShowList(false);
    }


}

