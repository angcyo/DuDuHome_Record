package com.dudu.persistence.app;

import rx.Observable;

/**
 * Created by Administrator on 2016/3/22.
 */
public interface AppVersionService {
    Observable<Version> findVersion();
    Observable<Version> saveVersion(Version version);
}
