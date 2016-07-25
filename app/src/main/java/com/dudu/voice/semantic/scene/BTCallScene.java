package com.dudu.voice.semantic.scene;

import com.dudu.voice.semantic.chain.DefaultChain;
import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.constant.SemanticConstant;

/**
 * Created by lxh on 2016-04-22 18:01.
 */
public class BTCallScene extends SemanticScene {
    @Override
    public void initChains() {
        initNomalChan();
        mChainMap.put(SemanticConstant.SERVICE_CHOOSE_CMD, mChainFactory.getChooseCmdChain());
        setType(SceneType.BTCALL);

    }

    @Override
    public DefaultChain getDefaultChain() {
        return new DefaultChain();
    }
}
