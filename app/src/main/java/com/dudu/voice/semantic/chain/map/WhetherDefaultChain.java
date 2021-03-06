package com.dudu.voice.semantic.chain.map;

import com.dudu.android.launcher.utils.Constants;
import com.dudu.navi.NavigationManager;
import com.dudu.navi.vauleObject.SearchType;
import com.dudu.voice.FloatWindowUtils;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.bean.SemanticBean;
import com.dudu.voice.semantic.chain.DefaultChain;
import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.constant.TTSType;
import com.dudu.voice.semantic.engine.SemanticEngine;

/**
 * Created by lxh on 2015/12/29.
 */

public class WhetherDefaultChain extends DefaultChain {

    public static final String YES = "是";
    public static final String NO = "否";
    public static final String YES_TWO = "设置";
    public static final String YES_THREE = "添加";
    public static final String NO_TWO = "不设置";
    public static final String NO_THREE = "不添加";
    private String text;

    @Override
    public boolean doSemantic(SemanticBean semantic) {
        if (semantic != null && semantic.getText() != null) {
            text = semantic.getText();
            return whether();
        } else {
            mVoiceManager.startSpeaking(Constants.UNDERSTAND_MISUNDERSTAND);
            return true;
        }
    }

    @Override
    public boolean matchSemantic(String service) {
        return true;
    }

    private boolean whether() {
        switch (text) {
            case YES:
            case YES_TWO:
            case YES_THREE:
                NavigationManager.getInstance(mContext).setSearchType(SearchType.SEARCH_COMMONADDRESS);
                VoiceManagerProxy.getInstance().startSpeaking("您好，请说出您要添加的地址", TTSType.TTS_START_UNDERSTANDING, true);
                SemanticEngine.getProcessor().switchSemanticType(SceneType.NAVIGATION);
                break;
            case NO:
            case NO_TWO:
            case NO_THREE:
                FloatWindowUtils.removeFloatWindow();
                break;
            default:
                mVoiceManager.startSpeaking(Constants.UNDERSTAND_MISUNDERSTAND, TTSType.TTS_START_UNDERSTANDING, false);
        }
        return true;
    }
}
