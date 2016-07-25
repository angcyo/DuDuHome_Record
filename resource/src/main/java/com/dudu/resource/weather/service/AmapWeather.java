package com.dudu.resource.weather.service;

import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.dudu.commonlib.CommonLib;
import com.dudu.persistence.realm.RealmSingleValueService;
import com.dudu.persistence.realm.SingleValueService;
import com.dudu.persistence.weather.WeatherInfo;
import com.dudu.persistence.realmmodel.weather.WeatherInfoRealm;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.schedulers.Schedulers;

/**
 * Created by dengjun on 2016/3/23.
 * Description :
 */
public class AmapWeather implements WeatherSearch.OnWeatherSearchListener{

    private WeatherSearch weatherSearch;

    private Logger log = LoggerFactory.getLogger("Resouce.weather");

//    private WeatherInfoService weatherInfoService;
    private SingleValueService<WeatherInfoRealm> weatherInfoRealmSingleValueService;


    public AmapWeather() {
        weatherSearch = new WeatherSearch(CommonLib.getInstance().getContext());
        weatherSearch.setOnWeatherSearchListener(this);

//        weatherInfoService = new WeatherInfoServiceRealm();
        weatherInfoRealmSingleValueService = new RealmSingleValueService<WeatherInfoRealm>(WeatherInfoRealm.class);
    }

    public void queryCurCityLiveWeather(){
        queryLiveWeather(getCurrentCity());
    }

    public void queryLiveWeather(String currentCity){
        queryWeather(currentCity, WeatherSearchQuery.WEATHER_TYPE_LIVE);
    }

    public void queryWeather(String currentCity, int queryType){
        WeatherSearchQuery weatherSearchQuery = new WeatherSearchQuery(currentCity, queryType);
        weatherSearch.setQuery(weatherSearchQuery);
        weatherSearch.searchWeatherAsyn();
    }

    public String getCurrentCity(){
        return "深圳";
    }

    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int i) {
        //运行在主线程
        log.debug("实时天气：{},响应码：{}", new Gson().toJson(localWeatherLiveResult), i);
        WeatherInfo weatherInfo = new WeatherInfo(localWeatherLiveResult.getLiveResult().getWeather(),
                                                                                    localWeatherLiveResult.getLiveResult().getTemperature(),
                                                                                    localWeatherLiveResult.getLiveResult().getWindDirection()+ localWeatherLiveResult.getLiveResult().getWindPower());

        if (i == 0){
            /*weatherInfoService.saveWeatherInfo(weatherInfo)
                    .subscribeOn(Schedulers.io())
                    .subscribe((weatherInfo1 -> {
                        log.debug("保存的实时天气：{}", new Gson().toJson(weatherInfo1));
                    }), throwable -> {
                        log.error("异常：", throwable);
                    });*/

            weatherInfoRealmSingleValueService
                    .save(new WeatherInfoRealm(weatherInfo.getWeather(), weatherInfo.getTemperature(), weatherInfo.getWind()))
                    .subscribeOn(Schedulers.io())
                    .subscribe((weatherInfo1 -> {
                        log.debug("保存的实时天气：天气：{}, 温度：{}，风向：{}", weatherInfo1.getWeather(), weatherInfo1.getTemperature(), weatherInfo1.getWind());
                    }), throwable -> {
                        log.error("异常：", throwable);
                    });
        }
    }

    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {

    }


}
