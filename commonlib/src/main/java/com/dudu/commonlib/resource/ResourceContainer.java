package com.dudu.commonlib.resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dengjun on 2016/3/22.
 * Description :
 */
public class ResourceContainer<K, V extends ResourceInterface> {
    private Map<K, V> resourceContainer;

    public ResourceContainer() {
        resourceContainer = new ConcurrentHashMap<K, V>(32);
    }


    public void put(K k, V v){
        if (k != null && v != null)
            resourceContainer.put(k, v);
    }

    public V get(K k){
        return resourceContainer.get(k);
    }

    public void remove(K k){
        resourceContainer.remove(k);
    }

    public void releaseAll(){
        for (Map.Entry<K, V> entry : resourceContainer.entrySet()){
            V v = entry.getValue();
            v.release();
        }
        resourceContainer.clear();
    }
}
