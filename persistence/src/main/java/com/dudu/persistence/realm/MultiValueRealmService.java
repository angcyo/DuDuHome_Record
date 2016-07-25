package com.dudu.persistence.realm;

import com.dudu.persistence.rx.RealmManage;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by dengjun on 2016/4/8.
 * Description :
 */
public class MultiValueRealmService<T extends RealmObject> {
    private Class<T> valueClass;

    public MultiValueRealmService(Class<T> valueClass) {
        this.valueClass = valueClass;
    }

    public void find(RealmCallBack realmCallBack){
        try(Realm realm = RealmManage.getRealm()) {
            RealmResults<T> realmResults = realm.where(valueClass).findAll();
            if (realmCallBack != null){
                realmCallBack.onRealm(realmResults);
            }
        }
    }

    public void find(RealmQueryCondition<T> queryCondition, RealmCallBack realmCallBack){
        if (queryCondition == null){
            return;
        }
        try(Realm realm = RealmManage.getRealm()) {
            T realmResults = queryCondition.onCondition(realm.where(valueClass));
            if (realmCallBack != null){
                realmCallBack.onRealm(realmResults);
            }
        }
    }

    public void findMulti(RealmQueryCondition<RealmResults<T>> queryCondition, RealmCallBack realmCallBack){
        if (queryCondition == null){
            return;
        }
        try(Realm realm = RealmManage.getRealm()) {
            RealmResults<T> realmResults = queryCondition.onCondition(realm.where(valueClass));
            if (realmCallBack != null){
                realmCallBack.onRealm(realmResults);
            }
        }
    }

    /* 异步查找方式， 一般用在UI线程， RealmCallBack回调执行在主线程， 使用完后需要关闭realm*/
    public void findMultiAsync(RealmQueryCondition<RealmResults<T>> queryCondition, RealmCallBack realmCallBack){
//        if (queryCondition == null){
//            return ;
//        }
//        Realm realm = RealmManage.getRealm();
//        RealmResults<T> realmResults = queryCondition.onCondition(realm.where(valueClass));
//        realmResults.addChangeListener(new RealmChangeListener() {
//            @Override
//            public void onChange() {
//                realmResults.removeChangeListeners();
//                if (realmCallBack != null){
//                    realmCallBack.onRealm(realmResults);
//                }
//                if (realm != null){
//                    realm.close();
//                }
//            }
//        });
    }

    public void remove(RealmCallBack realmCallBack){
        try(Realm realm = RealmManage.getRealm()) {
            realm.beginTransaction();
            RealmResults<T> realmResults = realm.where(valueClass).findAll();
            if (realmCallBack != null){
                realmCallBack.onRealm(realmResults);
            }
            realmResults.clear();
            realm.commitTransaction();
        }
    }

    public void removeOne(RealmQueryCondition<T> queryCondition, RealmCallBack realmCallBack){
//        if (queryCondition == null){
//            return;
//        }
//        try(Realm realm = RealmManage.getRealm()) {
//            realm.beginTransaction();
//            T realmResult = queryCondition.onCondition(realm.where(valueClass));
//            if (realmCallBack != null){
//                realmCallBack.onRealm(realmResult);
//            }
//            if (realmResult != null){
//                realmResult.removeFromRealm();
//            }
//            realm.commitTransaction();
//        }
    }

    public void removeMulti(RealmQueryCondition<RealmResults<T>> queryCondition, RealmCallBack realmCallBack){
        if (queryCondition == null){
            return;
        }
        try(Realm realm = RealmManage.getRealm()) {
            realm.beginTransaction();
            RealmResults<T> realmResult = queryCondition.onCondition(realm.where(valueClass));
            if (realmCallBack != null){
                realmCallBack.onRealm(realmResult);
            }
            realmResult.clear();
            realm.commitTransaction();
        }
    }

    public void save(T value, RealmCallBack realmCallBack){
        try(Realm realm = RealmManage.getRealm()) {
            realm.beginTransaction();
            T valueSaved = realm.copyToRealm(value);
            if (realmCallBack != null){
                realmCallBack.onRealm(valueSaved);
            }
            realm.commitTransaction();
        }
    }

    public void saveAsync(T value, RealmCallBack realmCallBack){
//        try(Realm realm = RealmManage.getRealm()) {
//            realm.executeTransaction(new Realm.Transaction() {
//                @Override
//                public void execute(Realm realm) {
//                    realm.copyToRealm(value);
//                }
//            }, new Realm.Transaction.Callback(){
//                @Override
//                public void onSuccess() {
//                    if (realmCallBack != null){
//                        realmCallBack.onRealm(value);
//                    }
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    if (realmCallBack != null){
//                        realmCallBack.onError(e);
//                    }
//                }
//            });
//        }
    }

    public void modify(RealmQueryCondition<T> queryCondition, RealmCallBack realmCallBack){
        if (queryCondition == null){
            return;
        }
        try(Realm realm = RealmManage.getRealm()) {
            realm.beginTransaction();
            T realmResult = queryCondition.onCondition(realm.where(valueClass));
            if (realmCallBack != null){
                realmCallBack.onRealm(realmResult);
            }
            realm.commitTransaction();
        }
    }
}
