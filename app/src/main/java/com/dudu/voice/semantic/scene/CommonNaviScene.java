package com.dudu.voice.semantic.scene;

import com.dudu.voice.semantic.chain.DefaultChain;
import com.dudu.voice.semantic.constant.SemanticConstant;

/**
 * Created by lxh on 2016-05-27 18:10.
 */
public class CommonNaviScene extends SemanticScene{
    @Override
    public void initChains() {
        initNomalChan();
        mChainMap.put(SemanticConstant.SERVICE_COMMON_NAVI,mChainFactory.getCommonNaviChain());
    }

    @Override
    public DefaultChain getDefaultChain() {
        return new DefaultChain();
    }
}
