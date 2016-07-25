package com.dudu.voice.semantic.chain;

import com.dudu.aios.ui.activity.MainRecordActivity;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.obd.ClearFaultResultEvent;
import com.dudu.voice.semantic.bean.FaultCmdBean;
import com.dudu.voice.semantic.bean.SemanticBean;
import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.constant.SemanticConstant;
import com.dudu.voice.semantic.engine.SemanticEngine;

import de.greenrobot.event.EventBus;

/**
 * Created by lxh on 2016-04-15 14:53.
 */
public class FaultCmdChain extends SemanticChain {

    public static final String CLEAR = "清除";

    public static final String NOT_CLEAR = "不清除";

    public static final String EXIT = "退出";

    @Override
    public boolean matchSemantic(String service) {
        return SemanticConstant.SERVICE_FAULT_CMD.equalsIgnoreCase(service);
    }

    @Override
    public boolean doSemantic(SemanticBean bean) {

        String action = ((FaultCmdBean) bean).getAction();

        switch (action) {
            case CLEAR:
                EventBus.getDefault().post(new ClearFaultResultEvent(ClearFaultResultEvent.START_CLEAR));
                break;
            case NOT_CLEAR:
            case EXIT:
                exit();
                break;
        }
        return true;
    }

    private void exit(){
        SemanticEngine.getProcessor().switchSemanticType(SceneType.HOME);
        SemanticEngine.getProcessor().clearSemanticStack();
        mVoiceManager.onStop();
        ((MainRecordActivity) ActivitiesManager.getInstance().getTopActivity()).replaceFragment(FragmentConstants.FRAGMENT_MAIN_PAGE);

    }
}
