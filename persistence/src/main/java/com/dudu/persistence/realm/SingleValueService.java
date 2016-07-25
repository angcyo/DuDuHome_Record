package com.dudu.persistence.realm;

import io.realm.RealmObject;
import rx.Observable;

/**
 * Created by dengjun on 2016/4/6.
 * Description :
 */
public interface SingleValueService <T extends RealmObject>{
    public Observable<T> find();

    public Observable<T> remove();

    public Observable<T> save(T value);
}
