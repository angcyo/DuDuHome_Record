package com.dudu.persistence.realm;

import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by dengjun on 2016/4/12.
 * Description :
 */
public interface RealmResultsQueryCondition <T extends RealmObject>  extends RealmQueryCondition <RealmResults<T>>{
    @Override
    RealmResults<T> onCondition(RealmQuery realmQuery);
}
