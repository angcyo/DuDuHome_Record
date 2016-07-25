package com.dudu.resource.resource;

import com.dudu.lib.container.SingltonContainer;

/**
 * Created by dengjun on 2016/3/31.
 * Description :
 */
public class ResourceFactory{
    public static ResourceFactory instance;

    private SingltonContainer resourceContainer;

    public static ResourceFactory getInstance(){
        if (instance == null){
            synchronized (ResourceFactory.class){
                if (instance == null){
                    instance = new ResourceFactory();
                }
            }
        }
        return instance;
    }

    private ResourceFactory() {
        resourceContainer = new SingltonContainer();
    }

    public SingltonContainer getResourceContainer() {
        return resourceContainer;
    }

    public void dd(){

    }
}
