package com.dudu.voice.semantic.factory;

import com.dudu.voice.semantic.chain.BrightnessChain;
import com.dudu.voice.semantic.chain.ChooseCmdChain;
import com.dudu.voice.semantic.chain.CmdChain;
import com.dudu.voice.semantic.chain.DatetimeChain;
import com.dudu.voice.semantic.chain.DimScreenChain;
import com.dudu.voice.semantic.chain.FaultCmdChain;
import com.dudu.voice.semantic.chain.NavigationChain;
import com.dudu.voice.semantic.chain.PhoneChain;
import com.dudu.voice.semantic.chain.PlayVideoCmdChain;
import com.dudu.voice.semantic.chain.VideoPlayChain;
import com.dudu.voice.semantic.chain.VolumeChain;
import com.dudu.voice.semantic.chain.WeatherChain;
import com.dudu.voice.semantic.chain.map.ChangeConmmonAddressChain;
import com.dudu.voice.semantic.chain.map.CommonAddressChain;
import com.dudu.voice.semantic.chain.map.CommonNaviChain;
import com.dudu.voice.semantic.chain.map.MapChoosePageChain;
import com.dudu.voice.semantic.chain.map.MapLocationChain;
import com.dudu.voice.semantic.chain.map.MapNearbyChain;
import com.dudu.voice.semantic.chain.map.MapNearestChain;
import com.dudu.voice.semantic.chain.map.MapPlaceNaviChain;
import com.dudu.voice.semantic.chain.map.WhetherChain;

public class SimpleChainFactory {

    private static SimpleChainFactory mInstance;

    private SimpleChainFactory() {

    }

    public static SimpleChainFactory getInstance() {
        if (mInstance == null) {
            mInstance = new SimpleChainFactory();
        }

        return mInstance;
    }

    public PhoneChain generatePhoneChain() {
        PhoneChain chain = new PhoneChain();
        return chain;
    }

    public VolumeChain generateVolumeChain() {
        VolumeChain chain = new VolumeChain();
        chain.addChildChain(chain);
        return chain;
    }

    public CmdChain generateCmdChain() {
        CmdChain chain = new CmdChain();
        return chain;
    }

    public NavigationChain getNavigationChain() {
        NavigationChain chain = new NavigationChain();
        return chain;
    }

    public CommonAddressChain getCommonAddressChain() {
        CommonAddressChain chain = new CommonAddressChain();
        WhetherChain whetherChain = new WhetherChain();
        chain.addChildChain(whetherChain);
        return chain;
    }

    public DatetimeChain getDatetimeChain() {
        DatetimeChain chain = new DatetimeChain();
        return chain;
    }

    public DimScreenChain getDimScreenChain() {
        DimScreenChain chain = new DimScreenChain();
        return chain;
    }

    public VideoPlayChain getVideoPlayChain() {
        VideoPlayChain chain = new VideoPlayChain();
        return chain;
    }

    public MapPlaceNaviChain getMapPlaceChain() {
        MapPlaceNaviChain chain = new MapPlaceNaviChain();
        chain.addChildChain(chain);
        return chain;
    }

    public MapLocationChain getMapLocationChain() {
        MapLocationChain chain = new MapLocationChain();
        chain.addChildChain(chain);
        return chain;
    }

    public MapNearbyChain getMapNearbyChain() {
        MapNearbyChain chain = new MapNearbyChain();
        chain.addChildChain(chain);
        return chain;
    }

    public MapNearestChain getMapNearestChain() {
        MapNearestChain chain = new MapNearestChain();
        chain.addChildChain(chain);
        return chain;
    }

    public WeatherChain getWeatherChain() {
        WeatherChain chain = new WeatherChain();
        return chain;
    }

    public ChangeConmmonAddressChain getChangeCommonAdrChain() {
        ChangeConmmonAddressChain chain = new ChangeConmmonAddressChain();
        return chain;
    }

    public MapChoosePageChain getChoosePageChain() {
        MapChoosePageChain choosePageChain = new MapChoosePageChain();
        choosePageChain.addChildChain(choosePageChain);
        return choosePageChain;
    }

    public ChooseCmdChain getChooseCmdChain() {
        ChooseCmdChain chain = new ChooseCmdChain();
        chain.addChildChain(chain);
        return chain;
    }

    public FaultCmdChain getFaultCmdChain() {
        FaultCmdChain faultCmdChain = new FaultCmdChain();
        return faultCmdChain;
    }

    public BrightnessChain getBrightnessChain() {
        BrightnessChain brightnessChain = new BrightnessChain();
        brightnessChain.addChildChain(brightnessChain);
        return brightnessChain;
    }

    public PlayVideoCmdChain getPlayVideoCmdChain() {
        PlayVideoCmdChain playVideoCmdChain = new PlayVideoCmdChain();
        playVideoCmdChain.addChildChain(playVideoCmdChain);
        return playVideoCmdChain;
    }

    public CommonNaviChain getCommonNaviChain() {
        CommonNaviChain commonNaviChain = new CommonNaviChain();
        return commonNaviChain;
    }

}
