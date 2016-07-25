package com.dudu.resource.location;

import com.dudu.resource.location.amap.AmapLocation;
import com.dudu.resource.location.loc.ILocation;
import com.dudu.resource.location.loc.LocationListener;
import com.dudu.resource.resource.SyncAbstactResoucre;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dengjun on 2016/3/26.
 * Description :
 */
public class LocationResource extends SyncAbstactResoucre {
    private Logger log = LoggerFactory.getLogger("Resouce.location");

    private ILocation location;

    public LocationResource() {
        location = new AmapLocation();
    }

    @Override
    protected void initResource() {
        log.info("初始化位置资源");

        location.startLocation();
    }

    @Override
    protected void releaseResource() {
        log.info("释放位置资源");
        location.stopLocation();
    }


    public void setLocationListener(LocationListener locationListener){
        location.registerLocationListener(locationListener);
    }
}
