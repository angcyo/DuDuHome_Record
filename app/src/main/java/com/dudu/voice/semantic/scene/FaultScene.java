package com.dudu.voice.semantic.scene;

import com.dudu.voice.semantic.chain.DefaultChain;
import com.dudu.voice.semantic.chain.FaultDefaultChain;
import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.constant.SemanticConstant;

/**
 * Created by lxh on 2016/2/16.
 */
public class FaultScene extends SemanticScene{
    @Override
    public void initChains() {

        mChainMap.put(SemanticConstant.SERVICE_VOLUME, mChainFactory.generateVolumeChain());
        mChainMap.put(SemanticConstant.SERVICE_CMD, mChainFactory.generateCmdChain());
        mChainMap.put(SemanticConstant.SERVICE_FAULT_CMD,mChainFactory.getFaultCmdChain());
        mChainMap.put(SemanticConstant.SERVICE_CHOOSE_CMD,mChainFactory.getChooseCmdChain());
        mChainMap.put(SemanticConstant.SERVICE_CHOOSE_PAGE,mChainFactory.getChoosePageChain());
        mChainMap.put(SemanticConstant.SERVICE_BRIGHTNESS,mChainFactory.getBrightnessChain());
        setType(SceneType.CAR_CHECKING);

    }

    @Override
    public DefaultChain getDefaultChain() {
        return new FaultDefaultChain();
    }
}
