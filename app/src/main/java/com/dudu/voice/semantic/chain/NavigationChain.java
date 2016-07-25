package com.dudu.voice.semantic.chain;

import android.app.Activity;
import android.os.Bundle;

import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.map.GaodeMapAppUtil;
import com.dudu.navi.NavigationManager;
import com.dudu.voice.semantic.bean.SemanticBean;
import com.dudu.voice.semantic.bean.map.NavigationBean;
import com.dudu.voice.semantic.constant.SemanticConstant;

/**
 * Created by Administrator on 2015/10/28.
 */
public class NavigationChain extends SemanticChain {

    private Activity mActivity = null;

    private Bundle mBundle = null;

    @Override
    public boolean matchSemantic(String service) {
        return service.equalsIgnoreCase(SemanticConstant.SERVICE_NAVI);
    }

    @Override
    public boolean doSemantic(SemanticBean semantic) {
        String operation = ((NavigationBean) semantic).getOperation();

        if (NavigationManager.getInstance(LauncherApplication.getContext()).isNavigatining()) {
            mActivity = ActivitiesManager
                    .getInstance().getTopActivity();
            switch (operation) {
                case "打开":
                    openTraffic();
                    break;
                case "退出":
                case "关闭":
                    closeTraffic();
                    break;
                default:
                    return false;
            }

            return true;
        }

        return false;
    }

    private void openTraffic() {

        GaodeMapAppUtil.openNaviBroadcast();
    }

    // 关闭路况
    private void closeTraffic() {

        GaodeMapAppUtil.closeNaviBroadcast();
    }

}
