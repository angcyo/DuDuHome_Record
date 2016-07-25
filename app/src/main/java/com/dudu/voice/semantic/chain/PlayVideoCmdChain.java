package com.dudu.voice.semantic.chain;

import com.dudu.aios.ui.base.BaseActivity;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.utils.ChoiseUtil;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.event.ChooseEvent;
import com.dudu.voice.FloatWindowUtils;
import com.dudu.voice.semantic.bean.PlayVideoBean;
import com.dudu.voice.semantic.bean.SemanticBean;
import com.dudu.voice.semantic.constant.SemanticConstant;
import com.dudu.voice.semantic.constant.TTSType;

import de.greenrobot.event.EventBus;

/**
 * Created by lxh on 2016-05-15 19:29.
 */
public class PlayVideoCmdChain extends SemanticChain {
    @Override
    public boolean matchSemantic(String service) {
        return SemanticConstant.SERVICE_PLAY_VIDEO.equalsIgnoreCase(service);
    }

    @Override
    public boolean doSemantic(SemanticBean bean) {

        return handlePlayVideo(bean);
    }


    private boolean handlePlayVideo(SemanticBean bean) {


        if (!BaseActivity.lastFragment.equals(FragmentConstants.FRAGMENT_VIDEO_LIST))
            return false;
        FloatWindowUtils.removeFloatWindow();
        PlayVideoBean mapChooseBean = (PlayVideoBean) bean;
        int number = ChoiseUtil.getChoiseSize(mapChooseBean.getChoose_number());
        if (number == 0) {
            mVoiceManager.startSpeaking(Constants.MAP_CHOISE_ERROR, TTSType.TTS_START_UNDERSTANDING, false);
            return true;
        }
        EventBus.getDefault().post(new ChooseEvent(ChooseEvent.ChooseType.PLAY_VIDEO, number));

        return true;
    }
}
