package com.dudu.navi.vauleObject;

import com.amap.api.maps.AMapUtils;

/**
 * Created by pc on 2015/11/14.
 */
public enum NaviDriveMode {

    /**
     * 速度最快
     */
    SPEEDFIRST(AMapUtils.DRIVING_DEFAULT, "速度最快"),

    /**
     * 时间最短且躲避拥堵
     */
    FASTESTTIME(AMapUtils.DRIVING_AVOID_CONGESTION, "时间最短且躲避拥堵"),

    /**
     * 避免收费
     */
    SAVEMONEY(AMapUtils.DRIVING_SAVE_MONEY, "避免收费"),

    /**
     * 距离最短
     */
    SHORTDESTANCE(AMapUtils.DRIVING_SHORT_DISTANCE, "距离最短"),

    /**
     * 避免收费且躲避拥堵
     */
    AVOIDCONGESTION(AMapUtils.DRIVING_SAVE_MONEY_AVOID_CONGESTION, "避免收费且躲避拥堵"),

    /**
     * 不走高速快速路
     */
    NOEXPRESSWAYS(AMapUtils.DRIVING_NO_HIGHWAY, "不走高速快速路");


    private int nCode;

    private String name;

    private NaviDriveMode(int nCode, String name) {
        this.nCode = nCode;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return String.valueOf(this.nCode);
    }

    public int getnCode() {
        return nCode;
    }
}
