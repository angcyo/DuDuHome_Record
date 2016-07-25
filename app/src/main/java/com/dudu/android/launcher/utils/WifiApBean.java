package com.dudu.android.launcher.utils;

/**
 * Created by Administrator on 2016/5/27.
 */
public class WifiApBean {

    private String ssid;

    private String password;

    public WifiApBean() {

    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "WifiApBean{" +
                "ssid='" + ssid + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
