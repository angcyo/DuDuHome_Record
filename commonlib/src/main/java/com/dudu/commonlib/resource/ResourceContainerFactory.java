package com.dudu.commonlib.resource;

/**
 * Created by dengjun on 2016/3/23.
 * Description :
 */
public class ResourceContainerFactory {
    private static ResourceContainerFactory instance = null;

    /* 存放与汽车点火无关的资源*/
    private ResourceContainer normalResourceContainer;
    /* 存放汽车点火才开启的资源*/
    private ResourceContainer carRunningResourceContainer;

    public static ResourceContainerFactory getInstance(){
        if (instance == null){
            synchronized (ResourceContainerFactory.class){
                if (instance == null){
                    instance = new ResourceContainerFactory();
                }
            }
        }
        return instance;
    }

    private ResourceContainerFactory() {
    }

    public  ResourceContainer getNormalResourceContainer(){
        if (normalResourceContainer == null){
            normalResourceContainer = new ResourceContainer();
        }
        return normalResourceContainer;
    }

    public ResourceContainer getCarRunningResourceContainer(){
        if (carRunningResourceContainer == null){
            carRunningResourceContainer = new ResourceContainer();
        }
        return carRunningResourceContainer;
    }
}
