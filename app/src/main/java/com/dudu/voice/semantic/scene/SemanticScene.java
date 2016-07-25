package com.dudu.voice.semantic.scene;

import com.dudu.voice.semantic.chain.DefaultChain;
import com.dudu.voice.semantic.chain.SemanticChain;
import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.constant.SemanticConstant;
import com.dudu.voice.semantic.factory.SimpleChainFactory;

import java.util.HashMap;

/**
 * Created by 赵圣琪 on 2015/11/25.
 */
public abstract class SemanticScene {

    protected HashMap<String, SemanticChain> mChainMap = new HashMap<>();

    protected SimpleChainFactory mChainFactory;

    private SceneType type;

    public SemanticScene() {
        mChainFactory = SimpleChainFactory.getInstance();

        initChains();
    }

    public abstract void initChains();

    public SemanticChain getChain(String service) {
        return mChainMap.get(service);
    }

    /**
     * 不能返回空对象 否则会导致崩溃
     *
     * @return
     */
    public abstract DefaultChain getDefaultChain();

    protected void initNomalChan() {
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
        mChainMap.put(SemanticConstant.SERVICE_BRIGHTNESS, mChainFactory.getBrightnessChain());
        mChainMap.put(SemanticConstant.SERVICE_PLAY_VIDEO, mChainFactory.getPlayVideoCmdChain());
    }

    public SceneType getType() {
        return type;
    }

    public void setType(SceneType type) {
        this.type = type;

    }
}
