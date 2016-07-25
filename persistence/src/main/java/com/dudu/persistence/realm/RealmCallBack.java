package com.dudu.persistence.realm;

/**
 * Created by dengjun on 2016/4/8.
 * Description :
 */
public interface RealmCallBack<T, E> {
    public void onRealm(T result);
    public void onError(E error);
}
