package com.dudu.resource.weather;

import com.dudu.resource.resource.ResourceState;
import com.dudu.resource.resource.SyncAbstactResoucre;
import com.dudu.resource.weather.service.AmapWeather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dengjun on 2016/3/23.
 * Description :
 */
public class WeatherResource extends SyncAbstactResoucre {
    private AmapWeather amapWeather;

    private Logger log = LoggerFactory.getLogger("Resouce.weather");



    public WeatherResource() {

    }

    @Override
    protected void initResource() {
        log.debug("初始化天气资源");

        amapWeather = new AmapWeather();
    }

    @Override
    protected void releaseResource() {
        log.debug("释放天气资源");
        amapWeather = null;
    }




    public void queryWeather(String currentCity) throws Exception {
        if (getResourceState().equals(ResourceState.UnInit)){
            throw new Exception("weatherResouce  uninitialized");
        }
        amapWeather.queryLiveWeather(currentCity);
    }
}
