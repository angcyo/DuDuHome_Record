package com.dudu.lib.container;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dengjun on 2016/3/31.
 * Description :
 */
public class SingltonContainer {
    protected Map<Object, Object> objectContainer = new ConcurrentHashMap<Object, Object>(32);


    public <T> T getInstance(Class<T> value){
        return getInstance(value, value);
    }

    public <T> T getInstance(Class<T> key, Class<? extends T> value){
        Object obj = objectContainer.get(key);
        if (obj == null){
            synchronized (value){
                if (obj == null){
                    try {
                        obj = value.newInstance();
                        objectContainer.put(key, obj);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return (T)obj;
    }



    synchronized public <T> T remove(Class<T> key){
        return  (T)objectContainer.remove(key);
    }


}

