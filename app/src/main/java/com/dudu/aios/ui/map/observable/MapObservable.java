package com.dudu.aios.ui.map.observable;

import android.content.Context;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.text.TextUtils;
import android.view.View;

import com.dudu.aios.ui.map.AddressSearchActivity;
import com.dudu.aios.ui.map.MapDbHelper;
import com.dudu.aios.ui.map.adapter.MapListAdapter;
import com.dudu.aios.ui.voice.VoiceEvent;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.databinding.GaodeMapLayoutBinding;
import com.dudu.event.MapResultShow;
import com.dudu.map.NavigationProxy;
import com.dudu.navi.NavigationManager;
import com.dudu.navi.entity.Navigation;
import com.dudu.navi.entity.Point;
import com.dudu.navi.vauleObject.NaviDriveMode;
import com.dudu.navi.vauleObject.NavigationType;
import com.dudu.navi.vauleObject.SearchType;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.engine.SemanticEngine;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by lxh on 2016/2/13.
 */
public class MapObservable {

    public static final int ADDRESS_VIEW_COUNT = 4;

    public final ObservableBoolean showList = new ObservableBoolean();
    public final ObservableBoolean showBottomButton = new ObservableBoolean();
    public final ObservableField<String> mapListTitle = new ObservableField<>();
    public final ObservableBoolean showEdt = new ObservableBoolean();
    public final ObservableInt historyCount = new ObservableInt();
    public final ObservableBoolean showDelete = new ObservableBoolean();
    public ArrayList<MapListItemObservable> mapList;
    private GaodeMapLayoutBinding binding;
    private Context mContext;
    private MapListAdapter mapListAdapter;


    private NavigationManager navigationManager;

    private NavigationProxy navigationProxy;

    private MapDbHelper mapDbHelper;


    public MapObservable(GaodeMapLayoutBinding binding) {

        this.binding = binding;
        this.mContext = binding.getRoot().getContext();
    }

    private static boolean containsEmoji(String str) {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (isEmojiCharacter(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEmojiCharacter(char codePoint) {
        return !((codePoint == 0x0) ||
                (codePoint == 0x9) ||
                (codePoint == 0xA) ||
                (codePoint == 0xD) ||
                ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
                ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF)));
    }

    public void init() {

        showEdt.set(false);
        showList.set(false);
        showBottomButton.set(true);
        showDelete.set(false);

        EventBus.getDefault().register(this);

        mapList = new ArrayList<>();

        navigationManager = NavigationManager.getInstance(binding.getRoot().getContext());
        navigationProxy = NavigationProxy.getInstance();

        initData();
    }

    private void initData() {
        mapDbHelper = MapDbHelper.getDbHelper();

        mapList = mapDbHelper.getHistory();

        if (!mapList.isEmpty()) {
            mapListTitle.set(binding.getRoot().getContext().getString(R.string.naviHistory));
            mapListAdapter = new MapListAdapter(mapList, (view, postion) -> historyNavi(postion));
            binding.mapListView.setAdapter(mapListAdapter);
        }


    }

    private void historyNavi(int position) {
        if (position > mapList.size())
            return;
        Point point = new Point(mapList.get(position).lat.get(), mapList.get(position).lon.get());
        Navigation navigation = new Navigation(point, NaviDriveMode.SPEEDFIRST, NavigationType.NAVIGATION);
        navigationProxy.startNavigation(navigation);

    }

    public void mapSearchBtn(View view) {
        this.showEdt.set(showEdt.get() ? false : true);

        if (!mapList.isEmpty()) {
            showList.set(showEdt.get());
        }
    }

    public void mapSearchEdt(View view) {

        if (!mapList.isEmpty()) {
            showList.set(true);
        }
    }

    public void deleteEdt(View view) {

        binding.mapSearchEdt.setText("");
        showList.set(false);
        showBottomButton.set(true);
    }

    public void searchManual(View view) {
        navigationProxy.setIsManual(true);
        if (TextUtils.isEmpty(binding.mapSearchEdt.getText().toString()))
            return;
        if (containsEmoji(binding.mapSearchEdt.getText().toString())) {
            VoiceManagerProxy.getInstance().startSpeaking(mContext.getString(R.string.notice_searchKeyword));
            return;
        }

        navigationManager.setSearchType(SearchType.SEARCH_PLACE);
        navigationManager.setKeyword(binding.mapSearchEdt.getText().toString());
        navigationProxy.doSearch();

        showList.set(false);
        showEdt.set(false);
    }

    public void onEventMainThread(MapResultShow event) {

        showEdt.set(navigationProxy.isManual() ? true : false);
        mapList.clear();

        Intent intent = new Intent(mContext, AddressSearchActivity.class);
        mContext.startActivity(intent);
    }


    public void release() {
        EventBus.getDefault().unregister(this);
        displayList();
        VoiceManagerProxy.getInstance().onStop();
    }


    public void displayList() {
        showList.set(false);
        showBottomButton.set(true);
        navigationProxy.setIsManual(false);
        navigationProxy.disMissProgressDialog();
        navigationProxy.removeCallback();
        navigationProxy.naviSubscription = null;

        navigationManager.setSearchType(SearchType.SEARCH_DEFAULT);

        SemanticEngine.getProcessor().switchSemanticType(SceneType.HOME);

        VoiceManagerProxy.getInstance().onStop();
    }


    public void onEvent(VoiceEvent event) {

        switch (event) {
            case DISMISS_WINDOW:
                showBottomButton.set(true);
                break;
            case SHOW_MESSAGE:
                showBottomButton.set(false);
                break;
        }
    }
}
