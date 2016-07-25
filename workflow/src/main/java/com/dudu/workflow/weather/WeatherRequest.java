package com.dudu.workflow.weather;

import com.dudu.rest.model.BaiduWeatherResponse;

import rx.Observable;

/**
 * Created by Administrator on 2016/5/5.
 */
public interface WeatherRequest {

    public Observable<BaiduWeatherResponse.BaiduWeatherResult.HistoryResult> getHistoryWeather(String date, String cityName);

    public Observable<BaiduWeatherResponse.BaiduWeatherResult.HistoryResult> getYesterDayWeather(String cityName);
}
