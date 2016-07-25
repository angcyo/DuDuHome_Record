package com.dudu.resource.location.loc;

/**
 * Created by dengjun on 2015/11/26.
 * Description :
 */
public interface ILocation {
    public void startLocation();

    public void stopLocation();

    public void registerLocationListener(LocationListener locationListener);

    public boolean isLocated();
}
