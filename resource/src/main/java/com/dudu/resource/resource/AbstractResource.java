package com.dudu.resource.resource;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by dengjun on 2016/3/30.
 * Description :
 */
public abstract class AbstractResource implements IResource {
    protected volatile ResourceState resourceState = ResourceState.UnInit;
    private AtomicInteger atomicInteger = new AtomicInteger();

    protected abstract void initResource();

    protected abstract void releaseResource();

    @Override
    public void init() {
        if (atomicInteger.getAndIncrement() == 0) {
            resourceState = ResourceState.Initing;
            initResource();
        }
    }

    @Override
    public void release() {
        if (atomicInteger.decrementAndGet() == 0) {
            releaseResource();
        }
    }

    public ResourceState getResourceState() {
        return resourceState;
    }
}
