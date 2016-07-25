package com.dudu.resource;

import com.dudu.resource.resource.SyncAbstactResoucre;

/**
 * Created by sean on 4/2/16.
 */
public class MySyncResource extends SyncAbstactResoucre{
    @Override
    protected void initResource() {
        System.out.println("MySyncResource.initResource");
    }

    @Override
    protected void releaseResource() {
        System.out.println("MySyncResource.releaseResource");
    }
}
