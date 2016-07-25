package com.dudu.voice.semantic.scene;

import com.dudu.voice.semantic.chain.DefaultChain;
import com.dudu.voice.semantic.chain.map.WhetherDefaultChain;
import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.constant.SemanticConstant;

/**
 * Created by lxh on 2015/12/1.
 */
public class WhetherScene extends SemanticScene {


    @Override
    public void initChains() {
        mChainMap.put(SemanticConstant.SERVICE_VOLUME, mChainFactory.generateVolumeChain());
        mChainMap.put(SemanticConstant.SERVICE_CMD, mChainFactory.generateCmdChain());
        mChainMap.put(SemanticConstant.SERVICE_BRIGHTNESS, mChainFactory.getBrightnessChain());
        setType(SceneType.COMMON_WHETHER);

    }

    @Override
    public DefaultChain getDefaultChain() {
        return new WhetherDefaultChain();
    }
}
