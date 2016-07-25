package com.dudu.rest.service;

import com.dudu.rest.model.BaiduWeatherResponse;

import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Administrator on 2016/5/5.
 */
public interface BaiduWeatherService {
    /**
     * 获取车型
     *
     * @return
     */
    @GET("/apistore/weatherservice/recentweathers")
    public Observable<BaiduWeatherResponse> requestWeathers(@Header("apikey") String apikey, @Query("cityname") String cityname);
}
