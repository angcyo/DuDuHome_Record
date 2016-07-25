package com.dudu.persistence.weather;

import rx.Observable;

/**
 * Created by dengjun on 2016/3/24.
 * Description :
 */
public interface WeatherInfoService {
    public Observable<WeatherInfo> findWeatherInfo(String key);

    public Observable<WeatherInfo> saveWeatherInfo(WeatherInfo weatherInfo);
}
