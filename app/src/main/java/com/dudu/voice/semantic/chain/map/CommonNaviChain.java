package com.dudu.voice.semantic.chain.map;

import com.dudu.aios.ui.map.AddressSearchActivity;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.map.NavigationProxy;
import com.dudu.voice.semantic.bean.SemanticBean;
import com.dudu.voice.semantic.bean.map.CommonNaviBean;
import com.dudu.voice.semantic.chain.SemanticChain;
import com.dudu.voice.semantic.constant.SemanticConstant;

/**
 * Created by lxh on 2016-05-27 18:10.
 */
public class CommonNaviChain extends SemanticChain {

    private static final String YES = "是";

    private static final String NO = "否";

    @Override
    public boolean matchSemantic(String service) {
        return SemanticConstant.SERVICE_COMMON_NAVI.equalsIgnoreCase(service);
    }

    @Override
    public boolean doSemantic(SemanticBean bean) {

        String action = ((CommonNaviBean) bean).getAction();
        switch (action) {

            case YES:
                NavigationProxy.getInstance().onCommonAdrNavi();
                break;
            case NO:
                ActivitiesManager.getInstance().closeTargetActivity(AddressSearchActivity.class);
                break;
        }

        return true;
    }
}
