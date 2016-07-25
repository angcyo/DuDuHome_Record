package com.dudu.voice.semantic.scene;

import com.dudu.voice.semantic.chain.DefaultChain;
import com.dudu.voice.semantic.chain.SemanticChain;
import com.dudu.voice.semantic.constant.SceneType;

/**
 * Created by 赵圣琪 on 2015/11/25.
 */
public class HomeScene extends SemanticScene {

    @Override
    public void initChains() {
        initNomalChan();
        setType(SceneType.HOME);
    }

    @Override
    public SemanticChain getChain(String service) {
        return mChainMap.get(service);
    }

    @Override
    public DefaultChain getDefaultChain() {
        return new DefaultChain();
    }



}
