package com.dudu.persistence.realm;

import io.realm.RealmQuery;

/**
 * Created by dengjun on 2016/4/9.
 * Description :查询条件
 */
public interface RealmQueryCondition<T>{
    public T onCondition(RealmQuery realmQuery);
}
