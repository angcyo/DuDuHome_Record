package com.dudu.voice.semantic.chain.map;

import android.app.Activity;

import com.dudu.aios.ui.activity.MainRecordActivity;
import com.dudu.aios.ui.base.BaseFragmentManagerActivity;
import com.dudu.event.ChooseEvent;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.map.NavigationProxy;
import com.dudu.voice.semantic.bean.SemanticBean;
import com.dudu.voice.semantic.bean.map.ChoosePageBean;
import com.dudu.voice.semantic.chain.SemanticChain;
import com.dudu.voice.semantic.constant.SemanticConstant;

import de.greenrobot.event.EventBus;

/**
 * Created by lxh on 2016-04-14 16:42.
 */
public class MapChoosePageChain extends SemanticChain {

    public static final String NEXT_PAGE = "下一页";
    public static final String LAST_PAGE = "上一页";

    @Override
    public boolean matchSemantic(String service) {
        return SemanticConstant.SERVICE_CHOOSE_PAGE.equalsIgnoreCase(service);
    }

    @Override
    public boolean doSemantic(SemanticBean bean) {

        String action = ((ChoosePageBean) bean).getAction();
        mVoiceManager.startUnderstanding();
        switch (action) {
            case NEXT_PAGE:
                return nextPage();
            case LAST_PAGE:
                return lastPage();
        }
        return true;
    }

    private boolean isCarChecking() {

        Activity topActivity = ActivitiesManager.getInstance().getTopActivity();

        if (topActivity instanceof MainRecordActivity
                && ((BaseFragmentManagerActivity) topActivity).getCurrentStackTag().equals(FragmentConstants.REPAIR_FAULT_CODE_FRAGMENT)) {
            return true;
        }
        return false;

    }

    private boolean nextPage() {
        if (isCarChecking()) {
            EventBus.getDefault().post(new ChooseEvent(ChooseEvent.ChooseType.NEXT_PAGE, 0));
            return true;
        }

        if (NavigationProxy.getInstance().isShowList()) {
            NavigationProxy.getInstance().onNextPage();
            return true;
        }
        return false;
    }

    private boolean lastPage() {
        if (isCarChecking()) {
            EventBus.getDefault().post(new ChooseEvent(ChooseEvent.ChooseType.PREVIOUS_PAGE, 0));
            return true;
        }
        if (NavigationProxy.getInstance().isShowList()) {
            NavigationProxy.getInstance().onPreviousPage();
            return true;
        }
        return false;
    }
}
