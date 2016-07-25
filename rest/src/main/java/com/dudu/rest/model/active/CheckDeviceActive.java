package com.dudu.rest.model.active;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.DeviceIDUtil;

import ch.qos.logback.core.android.SystemPropertiesProxy;

/**
 * Created by dengjun on 2016/3/17.
 * Description :
 */
public class CheckDeviceActive {
    private String roBuildFingerprint;
    private String roFotaVersion;

    private String roSerialno;
    private String roProductModel;

    private String simSerialno = "";
    private String launcherVersion;


    private String obeId;

    public CheckDeviceActive() {
        SystemPropertiesProxy sps = SystemPropertiesProxy.getInstance();

        roBuildFingerprint = sps.get("ro.build.fingerprint", "UNKNOWN");
        roFotaVersion = sps.get("ro.fota.version", "UNKNOWN");

        roSerialno = DeviceIDUtil.getAndroidID(CommonLib.getInstance().getContext());
        roProductModel = sps.get("ro.product.model", "UNKNOWN");

        if (DeviceIDUtil.getImsi(CommonLib.getInstance().getContext()) != null){
            simSerialno = DeviceIDUtil.getImsi(CommonLib.getInstance().getContext());
        }
        launcherVersion = DeviceIDUtil.getVersionName(CommonLib.getInstance().getContext());

        obeId = DeviceIDUtil.getIMEI(CommonLib.getInstance().getContext());

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

    public String getRoProductModel() {
        return roProductModel;
    }

    public void setRoProductModel(String roProductModel) {
        this.roProductModel = roProductModel;
    }

    public String getSimSeralno() {
        return simSerialno;
    }

    public void setSimSeralno(String simSeralno) {
        this.simSerialno = simSeralno;
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
}
