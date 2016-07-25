package com.dudu.persistence.app;

import com.dudu.commonlib.CommonLib;
import com.dudu.persistence.rx.RealmObservable;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/3/22.
 */
public class RealmAppVersionService implements AppVersionService {

    @Override
    public Observable<Version> findVersion() {
        return RealmObservable.object(new Func1<Realm, RealmVersion>() {
            @Override
            public RealmVersion call(Realm realm) {
                RealmResults<RealmVersion> versionList = realm.where(RealmVersion.class).findAll();
                if (versionList.size() > 0) {
                    return versionList.first();
                } else {
                    RealmVersion realmData = new RealmVersion();
                    realmData.setId(CommonLib.getInstance().getObeId());
                    realmData.setObdVersion("0.0.0");
                    realmData.setLauncherVersion(0);
                    return realmData;
                }
            }
        }).map(new Func1<RealmVersion, Version>() {
            @Override
            public Version call(RealmVersion realmVersion) {
                return versionFromRealm(realmVersion);
            }
        });
    }

    @Override
    public Observable<Version> saveVersion(final Version version) {
        return RealmObservable.object(new Func1<Realm, RealmVersion>() {
            @Override
            public RealmVersion call(Realm realm) {
                RealmVersion realmVersion = new RealmVersion();
                realmVersion.setId(version.getId());
                realmVersion.setObdVersion(version.getObdVersion());
                realmVersion.setLauncherVersion(version.getLauncherVersion());
                return realm.copyToRealmOrUpdate(realmVersion);
            }
        }).map(new Func1<RealmVersion, Version>() {
            @Override
            public Version call(RealmVersion realmUser) {
                // map to UI object
                return versionFromRealm(realmUser);
            }
        });
    }

    private static Version versionFromRealm(RealmVersion realmVersion) {
        Version version = new Version();
        version.setId(realmVersion.getId());
        version.setLauncherVersion(realmVersion.getLauncherVersion());
        version.setObdVersion(realmVersion.getObdVersion());
        return version;
    }
}
