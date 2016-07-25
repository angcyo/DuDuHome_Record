package com.dudu.service.service;

import com.dudu.lib.container.MultiConcurrentHashMap;

/**
 * Created by dengjun on 2016/3/31.
 * Description :
 */
public class ServiceFactory {
    private static ServiceFactory instance;

    private MultiConcurrentHashMap<Object, Object> serviceContainer = new MultiConcurrentHashMap<Object, Object>();



    public static ServiceFactory getInstance(){
        if (instance == null){
            synchronized (ServiceFactory.class){
                if (instance == null){
                    instance = new ServiceFactory();
                }
            }
        }
        return instance;
    }

    private ServiceFactory() {

    }



    public <T> T getServiceInstance(Class key, Class<? extends T> value) throws Exception {
        Object service = null;
        if (!serviceContainer.containsValue(value)){
            synchronized (value){
                if (!serviceContainer.containsValue(value)){
                    try {
                        service = value.newInstance();
                        serviceContainer.put(key,service);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else {
            for (Object obj: serviceContainer.get(key)){
                if (obj.getClass().getName().equals(value.getName())){
                    service = obj;
                }
            }
        }
        if (service == null){
            throw new Exception("getDefaultConfig service instance  exception");
        }
        return (T)service;
    }

    public <T> void removeService(Class<? extends T> value){
        if(serviceContainer.containsValue(value)){
            serviceContainer.removeValue(value);
        }
    }
}
