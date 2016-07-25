package com.dudu.voice.semantic.scene;

import com.dudu.voice.semantic.chain.DefaultChain;
import com.dudu.voice.semantic.chain.VolumeDefaultChain;
import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.constant.SemanticConstant;

/**
 * Created by Robi on 2016-04-06 15:43.
 */
public class VolumeScene extends SemanticScene {
    @Override
    public void initChains() {
        mChainMap.put(SemanticConstant.SERVICE_PHONE, mChainFactory.generatePhoneChain());
        mChainMap.put(SemanticConstant.SERVICE_VOLUME, mChainFactory.generateVolumeChain());
        mChainMap.put(SemanticConstant.SERVICE_CMD, mChainFactory.generateCmdChain());
        mChainMap.put(SemanticConstant.SERVICE_MAP, mChainFactory.getMapPlaceChain());
        mChainMap.put(SemanticConstant.SERVICE_NEARBY, mChainFactory.getMapNearbyChain());
        mChainMap.put(SemanticConstant.SERVICE_NAVI, mChainFactory.getNavigationChain());
        mChainMap.put(SemanticConstant.SERVICE_DATE_TIME, mChainFactory.getDatetimeChain());
        mChainMap.put(SemanticConstant.SERVICE_DIM, mChainFactory.getDimScreenChain());
        mChainMap.put(SemanticConstant.SERVICE_VIDEO, mChainFactory.getVideoPlayChain());
        mChainMap.put(SemanticConstant.SERVICE_COMMONADDRESS, mChainFactory.getCommonAddressChain());
        mChainMap.put(SemanticConstant.SERVICE_LOCATION, mChainFactory.getMapLocationChain());
        mChainMap.put(SemanticConstant.SERVICE_NEAREST, mChainFactory.getMapNearestChain());
        mChainMap.put(SemanticConstant.SERVICE_WEATHER, mChainFactory.getWeatherChain());
        mChainMap.put(SemanticConstant.SERVICE_CHANGE_CONMMONADDTRSS, mChainFactory.getChangeCommonAdrChain());
        mChainMap.put(SemanticConstant.SERVICE_BRIGHTNESS,mChainFactory.getBrightnessChain());
        setType(SceneType.VOLUME);

    }

    @Override
    public DefaultChain getDefaultChain() {
        return new VolumeDefaultChain();
    }
}
