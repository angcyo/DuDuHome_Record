package com.dudu.voice.semantic.scene;

import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.constant.SemanticConstant;
import com.dudu.voice.semantic.chain.DefaultChain;
import com.dudu.voice.semantic.chain.map.MapChoiseDefaultChain;

/**
 * Created by 赵圣琪 on 2015/11/25.
 */
public class MapChoiseScene extends SemanticScene {

    @Override
    public void initChains() {
        mChainMap.put(SemanticConstant.SERVICE_CMD, mChainFactory.generateCmdChain());
        mChainMap.put(SemanticConstant.SERVICE_MAP, mChainFactory.getMapPlaceChain());
        mChainMap.put(SemanticConstant.SERVICE_NEARBY, mChainFactory.getMapNearbyChain());
        mChainMap.put(SemanticConstant.SERVICE_COMMONADDRESS,mChainFactory.getCommonAddressChain());
        mChainMap.put(SemanticConstant.SERVICE_LOCATION,mChainFactory.getMapLocationChain());
        mChainMap.put(SemanticConstant.SERVICE_NEAREST,mChainFactory.getMapNearestChain());
        mChainMap.put(SemanticConstant.SERVICE_COMMONADDRESS,mChainFactory.getCommonAddressChain());
        mChainMap.put(SemanticConstant.SERVICE_LOCATION,mChainFactory.getMapLocationChain());
        mChainMap.put(SemanticConstant.SERVICE_NEAREST,mChainFactory.getMapNearestChain());
        mChainMap.put(SemanticConstant.SERVICE_CHOOSE_PAGE,mChainFactory.getChoosePageChain());
        mChainMap.put(SemanticConstant.SERVICE_CHOOSE_CMD,mChainFactory.getChooseCmdChain());
        mChainMap.put(SemanticConstant.SERVICE_CHOOSE_STRATEGY,mChainFactory.getChooseCmdChain());
        mChainMap.put(SemanticConstant.SERVICE_BRIGHTNESS,mChainFactory.getBrightnessChain());
        mChainMap.put(SemanticConstant.SERVICE_VOLUME, mChainFactory.generateVolumeChain());

        setType(SceneType.MAP_CHOISE);

    }

    @Override
    public DefaultChain getDefaultChain() {
        return new MapChoiseDefaultChain();
    }

}
