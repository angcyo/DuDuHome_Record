package com.dudu.workflow.weather;

import com.dudu.rest.common.RetrofitServiceFactory;
import com.dudu.rest.model.BaiduWeatherResponse;
import com.dudu.rest.service.BaiduApiService;

import rx.Observable;

/**
 * Created by Administrator on 2016/5/5.
 */
public class WeatherRequestRetrofitImpl implements WeatherRequest {

    /**
     * 根据日期、城市获取历史天气（过去七天之内）
     *
     * @param date     日期（格式：2016-05-06）
     * @param cityName 城市名称
     * @return
     */
    public Observable<BaiduWeatherResponse.BaiduWeatherResult.HistoryResult> getHistoryWeather(String date, String cityName) {
        return RetrofitServiceFactory.getBaiduWeatherService().requestWeathers(BaiduApiService.APIKEY_VALUE, cityName)
                .map(baiduWeatherResponse -> baiduWeatherResponse.retData.history)
                .map(historyResults -> {
                    for (int i = historyResults.length - 1; i >= 0; i--) {
                        if (date.equals(historyResults[i].date)) {
                            return historyResults[i];
                        }
                    }
                    return null;
                });
    }


    /**
     * 根据城市获取昨天天气
     *
     * @param cityName 城市名称
     * @return
     */
    public Observable<BaiduWeatherResponse.BaiduWeatherResult.HistoryResult> getYesterDayWeather(String cityName) {
        return RetrofitServiceFactory.getBaiduWeatherService().requestWeathers(BaiduApiService.APIKEY_VALUE, cityName)
                .map(baiduWeatherResponse -> baiduWeatherResponse.retData.history)
                .map(historyResults -> historyResults[historyResults.length - 1]);
    }

    /**
     * 根据日期、城市获取未来天气（七天之内）
     *
     * @param date     日期（格式：2016-05-06）
     * @param cityName 城市名称
     * @return
     */
    public Observable<BaiduWeatherResponse.BaiduWeatherResult.HistoryResult> getFutureWeather(String date, String cityName) {
        return RetrofitServiceFactory.getBaiduWeatherService().requestWeathers(BaiduApiService.APIKEY_VALUE, cityName)
                .map(baiduWeatherResponse -> baiduWeatherResponse.retData.history)
                .map(historyResults -> {
                    for (int i = historyResults.length - 1; i >= 0; i--) {
                        if (date.equals(historyResults[i].date)) {
                            return historyResults[i];
                        }
                    }
                    return null;
                });
    }
}
