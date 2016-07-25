package com.dudu.persistence.realm;

import com.dudu.persistence.rx.RealmObservable;

import io.realm.RealmObject;
import rx.Observable;

/**
 * Created by dengjun on 2016/4/6.
 * Description :
 */
public class RealmSingleValueService<T extends RealmObject> implements SingleValueService<T> {
    private Class<T> valueClass;


    public RealmSingleValueService(Class<T> cls) {
//        valueClass = (Class<T>) ((ParameterizedType) getClass()
//                .getGenericSuperclass()).getActualTypeArguments()[0].getClass();
        valueClass = cls;
    }

    @Override
    public Observable<T> find() {
        return RealmObservable.object((realm -> {
            return realm.where(valueClass).findFirst();
        }));
    }

    @Override
    public Observable<T> remove() {
        return RealmObservable.object(realm -> {
            T value =  realm.where(valueClass).findFirst();
            realm.where(valueClass).findFirst().deleteFromRealm();
            return value;
        });
    }

    @Override
    public Observable<T> save(T value) {
        return RealmObservable.object((realm -> {
             return realm.copyToRealmOrUpdate(value);
        }));
    }
}
