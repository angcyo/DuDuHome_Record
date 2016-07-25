package com.dudu.map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.dudu.aios.ui.map.AddressSearchActivity;
import com.dudu.aios.ui.map.GaodeMapActivity;
import com.dudu.aios.ui.map.MapDialog;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.android.launcher.utils.CommonAddressUtil;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.event.ChooseEvent;
import com.dudu.event.MapResultShow;
import com.dudu.monitor.event.CarStatus;
import com.dudu.monitor.repo.location.LocationManage;
import com.dudu.navi.NavigationManager;
import com.dudu.navi.Util.NaviUtils;
import com.dudu.navi.entity.Navigation;
import com.dudu.navi.entity.PoiResultInfo;
import com.dudu.navi.entity.Point;
import com.dudu.navi.event.NaviEvent;
import com.dudu.navi.vauleObject.NaviDriveMode;
import com.dudu.navi.vauleObject.NavigationType;
import com.dudu.navi.vauleObject.SearchType;
import com.dudu.voice.FloatWindowUtils;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.constant.TTSType;
import com.dudu.voice.semantic.engine.SemanticEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;

/**
 * Created by lxh on 2015/11/26.
 */
public class NavigationProxy {

    public static final int OPEN_MANUAL = 1;
    public static final int OPEN_VOICE = 2;
    public static final int OPEN_MAP = 3;
    private static final int REMOVE_WINDOW_TIME = 6 * 1000;
    private static NavigationProxy mInstance;
    public Point endPoint = null;
    public Subscription naviSubscription = null;
    private Context context;
    private NavigationManager navigationManager;
    private VoiceManagerProxy voiceManager;
    private int chooseStep;
    private boolean isManual = false;
    private Handler handler;
    private MapDialog waitingDialog = null;
    private String msg;
    private long mLastClickTime = 0;
    private Runnable removeWindowRunnable = () -> FloatWindowUtils.removeFloatWindow();
    private boolean needNotify = true;
    private boolean isShowList = false;
    private boolean isStartNewNavi = false;


    public boolean isNeedRefresh() {
        return needRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {

        this.needRefresh = needRefresh;
    }

    private boolean needRefresh = true;

    private Logger logger = LoggerFactory.getLogger("gps.NavigationProxy");

    public boolean isStartNewNavi() {
        return isStartNewNavi;
    }

    public void setStartNewNavi(boolean startNewNavi) {
        isStartNewNavi = startNewNavi;
    }

    private View.OnClickListener cancel = v -> {

        disMissProgressDialog();
        needNotify = false;
        voiceManager.stopSpeaking();
        voiceManager.stopUnderstanding();

    };

    public NavigationProxy() {
        context = LauncherApplication.getContext();

        navigationManager = NavigationManager.getInstance(context);

        EventBus.getDefault().register(this);

        handler = new Handler();

        voiceManager = VoiceManagerProxy.getInstance();

    }

    public static NavigationProxy getInstance() {
        if (mInstance == null) {
            mInstance = new NavigationProxy();
        }
        return mInstance;
    }

    public boolean isShowList() {

        return isShowList;
    }

    public void setShowList(boolean showList) {
        isShowList = showList;
    }

    public boolean isManual() {
        return isManual;
    }


    public int getChooseStep() {
        return chooseStep;
    }

    public void setChooseStep(int chooseStep) {
        this.chooseStep = chooseStep;
    }

    public void setIsManual(boolean isManual) {
        this.isManual = isManual;
    }

    public boolean openNavi(int openType) {
        if (checkFastClick()) {
            return false;
        }
        switch (NaviUtils.getOpenMode(context)) {
            case INSIDE:
                return (openType == OPEN_MAP) ? openMapActivity() : openActivity(openType);
            case OUTSIDE:
                openGaode();
                break;
        }
        return true;
    }

    private boolean checkFastClick() {
        long now = System.currentTimeMillis();
        if (now - mLastClickTime < 3000) {
            return true;
        }
        mLastClickTime = now;
        return false;
    }

    public void closeMap() {
        ActivitiesManager.getInstance().closeTargetActivity(
                GaodeMapActivity.class);
    }

    private boolean openMapActivity() {
        intentActivity(GaodeMapActivity.class);
        return true;
    }

    private boolean openActivity(int openType) {

        if (navigationManager.isNavigatining()) {
            FloatWindowUtils.removeFloatWindow();
            openGaode();
        } else {
            if (!isMapActivity()) {
                FloatWindowUtils.removeFloatWindow();
                intentActivity(GaodeMapActivity.class);
                if (openType == OPEN_VOICE) {
                    Observable.timer(1, TimeUnit.SECONDS).subscribe(aLong -> {
                        voiceManager.startSpeaking(context.getString(R.string.openNavi_notice),
                                TTSType.TTS_START_UNDERSTANDING, true);
                        SemanticEngine.getProcessor().switchSemanticType(SceneType.NAVIGATION);
                    }, throwable -> Log.e("NavigationProxy", "openActivity", throwable));

                }
            } else {
                return false;
            }
        }

        return true;
    }

    private Activity getTopActivity() {
        return ActivitiesManager.getInstance().getTopActivity();
    }

    private boolean isMapActivity() {
        return (getTopActivity() != null && getTopActivity() instanceof GaodeMapActivity);
    }

    public void existNavi() {
        navigationManager.existNavigation();
        GaodeMapAppUtil.exitGapdeApp();
    }

    public void searchControl(String keyword, SearchType type) {
        if (navigationManager.getSearchType() == SearchType.SEARCH_COMMONADDRESS)
            type = SearchType.SEARCH_COMMONPLACE;
        navigationManager.setSearchType(type);
        navigationManager.setKeyword(keyword);
        if (type == SearchType.SEARCH_COMMONADDRESS) {
            return;
        }
        doSearch();
    }

    public void doSearch() {
        FloatWindowUtils.removeFloatWindow();
        naviSubscription = null;
        if (!isShowList && !isMapActivity()) {
            intentActivity(GaodeMapActivity.class);
        }
        switch (navigationManager.getSearchType()) {
            case SEARCH_DEFAULT:
                return;
            case SEARCH_NEARBY:
            case SEARCH_NEAREST:
            case SEARCH_PLACE:
            case SEARCH_COMMONPLACE:
                handler.postDelayed(() -> searchHint(), 500);
                break;
        }

    }

    private void searchHint() {
        msg = "正在搜索" + navigationManager.getKeyword();
        boolean isShow = false;
        if (TextUtils.isEmpty(navigationManager.getKeyword())) {
            voiceManager.stopUnderstanding();
            voiceManager.startSpeaking("关键字有误，请重新输入！",
                    TTSType.TTS_START_UNDERSTANDING, true);
            return;
        }

        if (LocationManage.getInstance().getCurrentLocation() == null) {
            navigationManager.setSearchType(SearchType.SEARCH_DEFAULT);
            msg = "暂未获取到您的当前位置，不能搜索，请稍后再试";
            voiceManager.startSpeaking(msg, TTSType.TTS_DO_NOTHING, true);
            removeWindow();
            return;
        }
        needNotify = true;
        if (Constants.CURRENT_POI.equals(navigationManager.getKeyword())) {
            navigationManager.setSearchType(SearchType.SEARCH_CUR_LOCATION);
            msg = "正在获取您的当前位置";
            isShow = true;
        }
        if (!isManual) {
            voiceManager.startSpeaking(msg, TTSType.TTS_DO_NOTHING, isShow);
        }
        handler.postDelayed(() -> {
            needRefresh = true;
            showProgressDialog(context.getString(R.string.searching));
            navigationManager.search();
        }, 1500);
    }

    public void onEventMainThread(NaviEvent.NaviVoiceBroadcast event) {
        if (isManual)
            return;
        navigationManager.getLog().debug("NaviVoiceBroadcast stopUnderstanding");
        voiceManager.clearMisUnderstandCount();
        voiceManager.stopUnderstanding();
        removeCallback();
        voiceManager.startSpeaking(event.getNaviVoice(), TTSType.TTS_START_UNDERSTANDING, true);
    }

    public void onEventMainThread(NaviEvent.SearchResult event) {
        removeCallback();

        switch (event.getType()) {
            case SUCCESS:
                handlerPoiResultSuccess();
                break;
            case FAIL:
                handleResultFail(event.getInfo());
                break;
        }

        disMissProgressDialog();
    }


    private void handleResultFail(String text) {
        navigationManager.setSearchType(SearchType.SEARCH_DEFAULT);
        if (needNotify) {
            voiceManager.clearMisUnderstandCount();
            voiceManager.stopUnderstanding();
            voiceManager.startSpeaking(text, TTSType.TTS_START_UNDERSTANDING, true);
        }
    }

    public void onEvent(NaviEvent.ChangeSemanticType event) {
        SceneType type = null;
        switch (event) {
            case MAP_CHOISE:
                type = SceneType.MAP_CHOISE;
                break;
            case NORMAL:
                type = SceneType.HOME;
                break;
            case NAVIGATION:
                type = SceneType.NAVIGATION;
                break;
        }
        SemanticEngine.getProcessor().switchSemanticType(type);
    }

    public void onEventMainThread(NavigationType event) {

        try {
            disMissProgressDialog();
        } catch (Exception e) {

        } finally {

            removeCallback();
            naviSubscription = null;
            switch (event) {
                case CALCULATEERROR:
                    if (!needNotify)
                        return;
                    navigationManager.setSearchType(SearchType.SEARCH_DEFAULT);
                    voiceManager.startSpeaking("路径规划出错，请稍后再试", TTSType.TTS_DO_NOTHING, true);
                    removeWindow();
                    return;
                case NAVIGATION_END:
                    navigationManager.setNavigationType(NavigationType.DEFAULT);
                    intentActivity(GaodeMapActivity.class);
                    break;
            }
            navigationManager.setSearchType(SearchType.SEARCH_DEFAULT);

        }

    }

    public void handlerPoiResultSuccess() {
        endPoint = null;
        if (!needNotify) {
            return;
        }
        switch (navigationManager.getSearchType()) {
            case SEARCH_CUR_LOCATION:
                handler.postDelayed(() -> voiceManager.startSpeaking(navigationManager.getCurlocationDesc(),
                        TTSType.TTS_START_UNDERSTANDING, true), 200);
                navigationManager.setSearchType(SearchType.SEARCH_DEFAULT);
                break;

            case SEARCH_NEAREST:
                SemanticEngine.getProcessor().switchSemanticType(
                        SceneType.MAP_CHOISE);
                this.endPoint = new Point(navigationManager.getPoiResultList().get(0).getLatitude(),
                        navigationManager.getPoiResultList().get(0).getLongitude());
                EventBus.getDefault().post(MapResultShow.STRATEGY);
                navigationManager.setSearchType(SearchType.SEARCH_DEFAULT);
                return;
            default:
                SemanticEngine.getProcessor().switchSemanticType(
                        SceneType.MAP_CHOISE);
                EventBus.getDefault().post(MapResultShow.ADDRESS);
                break;

        }

    }

    private void initWaitingDialog(String message) {
        if (waitingDialog != null && waitingDialog.isShowing()) {
            waitingDialog.dismiss();
            waitingDialog = null;
        }
        waitingDialog = new MapDialog(ActivitiesManager.getInstance().getTopActivity(), message, cancel);
        Window dialogWindow = waitingDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.x = 10; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width = 306;
        lp.height = 218;
        dialogWindow.setAttributes(lp);
    }

    /**
     * 显示进度框
     */
    public void showProgressDialog(String message) {
        try {
            initWaitingDialog(message);
            waitingDialog.show();
            switch (navigationManager.getSearchType()) {
                case SEARCH_CUR_LOCATION:
                    return;
            }
        } catch (Exception e) {

        }

        FloatWindowUtils.removeFloatWindow();
    }

    public void disMissProgressDialog() {
        if (waitingDialog != null && waitingDialog.isShowing()) {
            waitingDialog.dismiss();
            waitingDialog = null;

        }
    }

    /**
     * 添加常用地
     *
     * @param choosePoint
     */
    public void addCommonAddress(final PoiResultInfo choosePoint) {
        final String addType = navigationManager.getCommonAddressType().getName();
        CommonAddressUtil.setCommonAddress(addType, context, choosePoint.getAddressTitle());
        CommonAddressUtil.setCommonLocation(addType,
                context, choosePoint.getLatitude(), choosePoint.getLongitude());

        voiceManager.stopUnderstanding();

        handler.postDelayed(() -> voiceManager.startSpeaking("添加" + choosePoint.getAddressTitle() + "为 " + addType + " 地址成功,是否要开始导航",
                TTSType.TTS_START_UNDERSTANDING, true), 200);
        navigationManager.setSearchType(SearchType.SEARCH_DEFAULT);
        SemanticEngine.getProcessor().switchSemanticType(SceneType.COMMON_NAVI);
    }

    public void onCommonAdrNavi() {
        Navigation navigation = new Navigation(endPoint, NaviDriveMode.SPEEDFIRST, NavigationType.NAVIGATION);
        startNavigation(navigation);
    }

    public void onNextPage() {
        EventBus.getDefault().post(new ChooseEvent(ChooseEvent.ChooseType.NEXT_PAGE, 0));
    }

    public void onPreviousPage() {
        EventBus.getDefault().post(new ChooseEvent(ChooseEvent.ChooseType.PREVIOUS_PAGE, 0));
    }

    public void onChoosePage(int page) {
        EventBus.getDefault().post(new ChooseEvent(ChooseEvent.ChooseType.CHOOSE_PAGE, page));
    }

    public void onChooseNumber(int position) {

        if (chooseStep == 1) {
            EventBus.getDefault().post(new ChooseEvent(ChooseEvent.ChooseType.CHOOSE_NUMBER, position));
        } else if (chooseStep == 2) {
            EventBus.getDefault().post(new ChooseEvent(ChooseEvent.ChooseType.STRATEGY_NUMBER, position));
        }
    }

    public void onLastPage() {
        EventBus.getDefault().post(new ChooseEvent(ChooseEvent.ChooseType.LAST_PAGE, 0));

    }

    public void onLastOne() {
        if (chooseStep == 1) {
            EventBus.getDefault().post(new ChooseEvent(ChooseEvent.ChooseType.CHOOSE_NUMBER, navigationManager.getPoiResultList().size()));
        } else if (chooseStep == 2) {
            EventBus.getDefault().post(new ChooseEvent(ChooseEvent.ChooseType.STRATEGY_NUMBER, 6));
        }
    }

    public void startNavigation(Navigation navigation) {
        if (naviSubscription != null)
            return;
        naviSubscription = Observable.just(navigation)
                .subscribe(navigation1 -> {

                    FloatWindowUtils.removeFloatWindow();
                    VoiceManagerProxy.getInstance().stopSpeaking();
                    VoiceManagerProxy.getInstance().onStop();
                    SemanticEngine.getProcessor().switchSemanticType(SceneType.HOME);

                    isManual = false;

                    GaodeMapAppUtil.startNavi(navigation1);
                    ActivitiesManager.getInstance().closeTargetActivity(GaodeMapActivity.class);
                    ActivitiesManager.getInstance().closeTargetActivity(AddressSearchActivity.class);
                    if (navigationManager.isNavigatining())
                        isStartNewNavi = true;
                }, throwable -> logger.error("startNavigation", throwable));
        Observable.timer(1, TimeUnit.SECONDS)
                .subscribe(aLong -> {
                    naviSubscription = null;
                }, throwable -> logger.error("startNavigation", throwable));
    }


    public void removeCallback() {
        if (handler != null && removeWindowRunnable != null) {
            handler.removeCallbacks(removeWindowRunnable);
        }
    }

    public void removeWindow() {
        disMissProgressDialog();
        if (navigationManager.getNavigationType() != NavigationType.DEFAULT) {
            navigationManager.setIsNavigatining(true);
        }
        if (navigationManager.getSearchType() == SearchType.SEARCH_DEFAULT) {
            handler.postDelayed(removeWindowRunnable, REMOVE_WINDOW_TIME);
        }
    }

    private void intentActivity(Class intentClass) {
        Intent standIntent = new Intent(context, intentClass);
        standIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(standIntent);
        mLastClickTime = 0;
    }

    public void onEvent(CarStatus event) {
        switch (event) {
            case OFFLINE:
                existNavi();
                break;
        }
    }

    public void openGaode() {
        GaodeMapAppUtil.openGaode();
        SemanticEngine.getProcessor().switchSemanticType(SceneType.HOME);
    }
}
