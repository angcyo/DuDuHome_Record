package com.dudu.rest.model.active;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.DeviceIDUtil;

import java.lang.reflect.Method;

import ch.qos.logback.core.android.SystemPropertiesProxy;

/**
 * Created by dengjun on 2016/3/17.
 * Description :
 */
public class ActiveDevice {
    private String roBoardPlatform;
    private String roBuildFingerprint;
    private String roFotaVersion;

    private String roSerialno;
    private String roProductManufacturer;
    private String roProductModel;

    private String simSerialno = "";
    private String launcherVersion;

    private String obeId;
    private String wifiStatus;


    public ActiveDevice() {
        SystemPropertiesProxy sps = SystemPropertiesProxy.getInstance();

        roBoardPlatform = sps.get("ro.board.platform","UNKNOWN");
        roBuildFingerprint = sps.get("ro.build.fingerprint", "UNKNOWN");

        roProductManufacturer = sps.get("ro.product.manufacturer","UNKNOWN");
        roFotaVersion = sps.get("ro.fota.version", "UNKNOWN");

        roSerialno = DeviceIDUtil.getAndroidID(CommonLib.getInstance().getContext());
        roProductModel = sps.get("ro.product.model", "UNKNOWN");

        if (DeviceIDUtil.getImsi(CommonLib.getInstance().getContext()) != null){
            simSerialno = DeviceIDUtil.getImsi(CommonLib.getInstance().getContext());
        }
        launcherVersion = DeviceIDUtil.getVersionName(CommonLib.getInstance().getContext());

        obeId = DeviceIDUtil.getIMEI(CommonLib.getInstance().getContext());
        wifiStatus = getWifiApEnabled()+"";

    }



    private int getWifiApEnabled() {
        try {
            WifiManager wifiManager = null;
            if (wifiManager == null) {
                wifiManager = (WifiManager) CommonLib.getInstance().getContext()
                        .getSystemService(Context.WIFI_SERVICE);
            }
            Method method = wifiManager.getClass()
                    .getMethod("isWifiApEnabled");
            method.setAccessible(true);
            if((Boolean) method.invoke(wifiManager))
                return 1;
        } catch (Exception e) {
            return 0;
        }

        return 0;
    }


    public String getRoBoardPlatform() {
        return roBoardPlatform;
    }

    public void setRoBoardPlatform(String roBoardPlatform) {
        this.roBoardPlatform = roBoardPlatform;
    }

    public String getRoBuildFingerprint() {
        return roBuildFingerprint;
    }

    public void setRoBuildFingerprint(String roBuildFingerprint) {
        this.roBuildFingerprint = roBuildFingerprint;
    }

    public String getRoFotaVersion() {
        return roFotaVersion;
    }

    public void setRoFotaVersion(String roFotaVersion) {
        this.roFotaVersion = roFotaVersion;
    }

    public String getRoSerialno() {
        return roSerialno;
    }

    public void setRoSerialno(String roSerialno) {
        this.roSerialno = roSerialno;
    }

    public String getRoProductManufacturer() {
        return roProductManufacturer;
    }

    public void setRoProductManufacturer(String roProductManufacturer) {
        this.roProductManufacturer = roProductManufacturer;
    }

    public String getRoProductModel() {
        return roProductModel;
    }

    public void setRoProductModel(String roProductModel) {
        this.roProductModel = roProductModel;
    }

    public String getSimSerialno() {
        return simSerialno;
    }

    public void setSimSerialno(String simSerialno) {
        this.simSerialno = simSerialno;
    }

    public String getLauncherVersion() {
        return launcherVersion;
    }

    public void setLauncherVersion(String launcherVersion) {
        this.launcherVersion = launcherVersion;
    }

    public String getObeId() {
        return obeId;
    }

    public void setObeId(String obeId) {
        this.obeId = obeId;
    }

    public String getWifiStatus() {
        return wifiStatus;
    }

    public void setWifiStatus(String wifiStatus) {
        this.wifiStatus = wifiStatus;
    }
}
