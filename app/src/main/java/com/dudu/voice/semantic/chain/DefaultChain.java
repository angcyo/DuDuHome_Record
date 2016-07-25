package com.dudu.voice.semantic.chain;

import android.text.TextUtils;

import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.android.launcher.utils.NetworkUtils;
import com.dudu.map.NavigationProxy;
import com.dudu.navi.vauleObject.SearchType;
import com.dudu.voice.semantic.bean.SemanticBean;

/**
 * Created by 赵圣琪 on 2015/10/30.
 */
public class DefaultChain extends SemanticChain {

    @Override
    public boolean matchSemantic(String service) {
        return true;
    }

    @Override
    public boolean doSemantic(SemanticBean semantic) {
        return isPlaceLocation(semantic);
    }


    private boolean isPlaceLocation(SemanticBean semantic) {
        if (semantic != null
                && !TextUtils.isEmpty(semantic.getText())
                && semantic.getText().contains("的位置")) {
            String place = semantic.getText().split("的位置")[0];
            NavigationProxy.getInstance().searchControl(place, SearchType.SEARCH_PLACE);
            return true;
        }
        if (!NetworkUtils.isNetworkConnected(LauncherApplication.getContext())) {
            mVoiceManager.startSpeaking(Constants.NETWORK_UNAVAILABLE);
        } else {
            mVoiceManager.startSpeaking(Constants.UNDERSTAND_MISUNDERSTAND);
        }
        return false;
    }
}