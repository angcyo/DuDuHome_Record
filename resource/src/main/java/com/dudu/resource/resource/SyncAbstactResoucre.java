package com.dudu.resource.resource;

/**
 * Created by dengjun on 2016/3/31.
 * Description : 初始化过程同步的资源继承此类
 */
public abstract class SyncAbstactResoucre extends AbstractResource{
    @Override
    public void init() {
        super.init();
        resourceState = ResourceState.Inited;
    }

    @Override
    public void release() {
        super.release();
        resourceState = ResourceState.UnInit;
    }
}
