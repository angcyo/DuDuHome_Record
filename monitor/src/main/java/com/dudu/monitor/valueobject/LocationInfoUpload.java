package com.dudu.monitor.valueobject;

import android.location.Location;

import com.amap.api.location.AMapLocation;
import com.dudu.monitor.utils.DataFormatUtil;
import com.dudu.monitor.utils.TimeUtils;

/**
 * Created by dengjun on 2015/11/26.
 * Description :
 */
public class LocationInfoUpload {
    /**
     * 纬度
     */
    private double a;

    /**
     * 经度
     */
    private double b;
    /**
     * 时间
     */
    private String c;
    /**
     * 速度
     */
    private float d;
    /**
     * 方向
     */
    private float e;

    /**
     * 高程
     */
    private double f;

    /**
     * 精度
     */
    private float g;

    private Integer h;        //类型 1.急加速 	2.急减速	3.急转弯	4.急变道	5.疲劳驾驶	6.发动机转速不匹配

//    private String obeId;

    public LocationInfoUpload(Location location) {

    }

    public LocationInfoUpload(LocationInfo location) {
        if (location == null)
            return;
        a = location.getLat();
        b = location.getLon();
        c = location.getCreateTime();
        d = location.getSpeeds();
        e = location.getDirection();
        f = location.getAltitude();
        g = location.getAccuracy();
        h = location.getType();
    }
}
