package com.dudu.workflow.app;

import com.dudu.commonlib.CommonLib;
import com.dudu.persistence.app.AppVersionService;
import com.dudu.persistence.app.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;

/**
 * Created by Administrator on 2016/3/22.
 */
public class LocalAppVersionFlow {
    private static final String TAG = "LocalAppVersionFlow";

    private Logger logger = LoggerFactory.getLogger(TAG);

    private AppVersionService appVersionService;

    public LocalAppVersionFlow(AppVersionService appVersionService) {
        this.appVersionService = appVersionService;
    }

    public void saveVersion(String obdVersion, Integer launcherVersion) {
        Version version = new Version();
        String obeId = CommonLib.getInstance().getObeId();
        version.setId(obeId);
        version.setObdVersion(obdVersion);
        version.setLauncherVersion(launcherVersion);
        appVersionService.saveVersion(version)
                .subscribe(version1 -> logger.debug("保存成功"),
                        error -> logger.error("saveUserName", error));
    }

    public Observable<String> getObdVersion() {
        return appVersionService.findVersion()
                .map(version -> version.getObdVersion());
    }

    public Observable<Integer> getLauncherVersion() {
        return appVersionService.findVersion()
                .map(version -> version.getLauncherVersion());
    }

    public Observable<Version> getVersionInfo() {
        return appVersionService.findVersion();
    }
}
