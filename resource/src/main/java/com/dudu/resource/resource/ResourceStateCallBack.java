package com.dudu.resource.resource;

/**
 * Created by dengjun on 2016/3/31.
 * Description :
 */
public interface ResourceStateCallBack<T> {
    void onStateChange(T state);
}
