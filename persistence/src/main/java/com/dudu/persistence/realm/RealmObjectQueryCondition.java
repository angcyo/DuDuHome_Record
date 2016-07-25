package com.dudu.persistence.realm;

import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Created by dengjun on 2016/4/12.
 * Description :
 */
public interface RealmObjectQueryCondition<T extends RealmObject> extends RealmQueryCondition<T> {
    @Override
    T onCondition(RealmQuery realmQuery);
}
