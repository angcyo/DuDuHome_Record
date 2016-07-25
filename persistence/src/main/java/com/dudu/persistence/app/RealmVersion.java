package com.dudu.persistence.app;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Administrator on 2016/3/22.
 */
public class RealmVersion extends RealmObject {

    @PrimaryKey
    private String id;

    private String obdVersion;

    private int launcherVersion;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObdVersion() {
        return obdVersion;
    }

    public void setObdVersion(String obdVersion) {
        this.obdVersion = obdVersion;
    }

    public int getLauncherVersion() {
        return launcherVersion;
    }

    public void setLauncherVersion(int launcherVersion) {
        this.launcherVersion = launcherVersion;
    }
}
