package com.dudu.persistence.realm;

import com.dudu.persistence.rx.RealmManage;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;

/**
 * Created by dengjun on 2016/4/19.
 * Description :
 */
public class SingleValueRealmServcie<T extends RealmObject> {
    private Class<T> valueClass;

    public SingleValueRealmServcie(Class<T> valueClass) {
        this.valueClass = valueClass;
    }


    public void find(RealmCallBack realmCallBack) {
        try (Realm realm = RealmManage.getRealm()) {
            T realmResult = realm.where(valueClass).findFirst();
            if (realmCallBack != null) {
                realmCallBack.onRealm(realmResult);
            }
        }
    }

    public void find(RealmQueryCondition<T> queryCondition, RealmCallBack realmCallBack) {
        if (queryCondition == null) {
            return;
        }
        try (Realm realm = RealmManage.getRealm()) {
            T realmResults = queryCondition.onCondition(realm.where(valueClass));
            if (realmCallBack != null) {
                realmCallBack.onRealm(realmResults);
            }
        }
    }


    public RealmQuery<T> query() {
        try (Realm realm = RealmManage.getRealm()) {
            return realm.where(valueClass);
        }
    }

    public void findAsync(RealmCallBack realmCallBack) {
//        Realm realm = RealmManage.getRealm();
//        RealmObject realmObject = realm.where(valueClass).findFirstAsync();
//        if (realmObject != null) {
//            realmObject.addChangeListener(() -> {
//                if (realmCallBack != null) {
//                    realmCallBack.onRealm(realmObject);
//                }
//                if (realm != null) {
//                    realm.close();
//                }
//            });
//        } else {
//            if (realmCallBack != null) {
//                realmCallBack.onRealm(realmObject);
//            }
//            if (realm != null) {
//                realm.close();
//            }
//        }
    }

    public Observable<T> findAsync() {
        try (Realm realm = RealmManage.getRealm()) {
            return realm.where(valueClass)
                    .findFirstAsync()
                    .asObservable();
        }
    }

    public Observable<RealmResults<T>> findAll() {
        Realm realm = RealmManage.getRealm();
        return realm.where(valueClass)
                .findAll()
                .asObservable()
                .filter(datas -> datas.isLoaded());
    }

    public Observable<RealmResults<T>> findAllAsync() {
        try (Realm realm = RealmManage.getRealm()) {
            return realm.where(valueClass)
                    .findAllAsync()
                    .asObservable()
                    .filter(datas -> datas.isLoaded());
        }
    }

    public void remove(RealmCallBack realmCallBack) {
//        try (Realm realm = RealmManage.getRealm()) {
//            realm.beginTransaction();
//            T realmResult = realm.where(valueClass).findFirst();
//            realmResult.removeFromRealm();
//            if (realmCallBack != null) {
//                realmCallBack.onRealm(realmResult);
//            }
//            realm.commitTransaction();
//        }
    }

    public void save(T value, RealmCallBack realmCallBack) {
        try (Realm realm = RealmManage.getRealm()) {
            realm.beginTransaction();
            T valueSaved = realm.copyToRealmOrUpdate(value);
            if (realmCallBack != null) {
                realmCallBack.onRealm(valueSaved);
            }
            realm.commitTransaction();
        }
    }

    public void close(){

    }

}
