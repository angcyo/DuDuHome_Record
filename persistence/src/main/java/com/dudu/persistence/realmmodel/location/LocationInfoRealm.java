package com.dudu.persistence.realmmodel.location;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by dengjun on 2016/4/6.
 * Description :
 */
public class LocationInfoRealm extends RealmObject {
    @PrimaryKey
    private String key;

    /**
     * 经度
     */
    private double lon;
    /**
     * 纬度
     */
    private double lat;
    /**
     * 高程
     */
    private double altitude;
    /**
     * 速度
     */
    private float speeds;
    /**
     * 方向
     */
    private float direction;
    /**
     * 精度
     */
    private float accuracy;
    /**
     * 时间
     */
    private String createTime;
    /**
     * 城市
     */
    private String city;
    /**
     * 地址
     */
    private String address;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getSpeeds() {
        return speeds;
    }

    public void setSpeeds(float speeds) {
        this.speeds = speeds;
    }

    public float getDirection() {
        return direction;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
