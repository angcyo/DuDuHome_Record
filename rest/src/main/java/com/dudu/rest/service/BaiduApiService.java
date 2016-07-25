package com.dudu.rest.service;

import com.dudu.commonlib.utils.DataJsonTranslation;
import com.dudu.rest.model.BaiduWeatherResponse;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by Administrator on 2016/5/5.
 */
public class BaiduApiService {
    public static final String BAIDU_HTTPURL = "http://apis.baidu.com/apistore/weatherservice/recentweathers";
    public static final String APIKEY = "apikey";
    public static final String APIKEY_VALUE = "a3fb6dc3bb5c6a6e85b6985cc884edf9";
    public static final String CITYNAME = "cityname";
    public static final String CITYID = "cityid";

    /**
     * @param cityName 城市
     * @return 返回结果
     */
    public static Observable<String> requestWeatherByCityNames(String cityName) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                BufferedReader reader = null;
                String result = null;
                StringBuffer sbf = new StringBuffer();
                String httpUrl = BAIDU_HTTPURL + "?" + CITYNAME + "=" + cityName;
                try {
                    URL url = new URL(httpUrl);
                    HttpURLConnection connection = (HttpURLConnection) url
                            .openConnection();
                    connection.setRequestMethod("GET");
                    // 填入apikey到HTTP header
                    connection.setRequestProperty(APIKEY, APIKEY_VALUE);
                    connection.connect();
                    InputStream is = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    String strRead = null;
                    while ((strRead = reader.readLine()) != null) {
                        sbf.append(strRead);
                        sbf.append("\r\n");
                    }
                    reader.close();
                    result = sbf.toString();
                    subscriber.onNext(result);
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static Observable<BaiduWeatherResponse> requestWeathers(String cityName) {
        return requestWeatherByCityNames(cityName)
                .map(results -> (BaiduWeatherResponse) DataJsonTranslation.jsonToObject(results, BaiduWeatherResponse.class));
    }
}
