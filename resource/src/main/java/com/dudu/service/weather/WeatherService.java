package com.dudu.service.weather;

import com.dudu.persistence.rx.RealmManage;
import com.dudu.persistence.realmmodel.weather.WeatherInfoRealm;
import com.dudu.resource.resource.ResourceFactory;
import com.dudu.resource.weather.WeatherResource;
import com.dudu.service.service.IService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by dengjun on 2016/3/25.
 * Description :
 */
public class WeatherService implements IService {
    private WeatherResource weatherResource;
    private Subscription weatherSubscriptions;

    private Subscription wertherSubscription2;
    private Subscription testSubscription;

    private Logger log = LoggerFactory.getLogger("Resouce.weather");
    @Override
    public void start() {
       test();


        weatherResource = ResourceFactory.getInstance().getResourceContainer().getInstance(WeatherResource.class);
        weatherResource.init();
        log.info("interval.io.create Resouce.weather");
        weatherSubscriptions =Observable.interval(5, 30,TimeUnit.SECONDS,Schedulers.io())
                .subscribe((l)->{
                    try {
                        weatherResource.queryWeather("深圳");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    log.error("interval.io 异常", throwable);
                });
    }

    private void test(){
        log.info("interval.io.create test Resouce.weather");
        testSubscription = Observable.interval(5, 30, TimeUnit.SECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(l->{
                    if (RealmManage.createObservable(WeatherInfoRealm.class) != null){
                        log.info("数据库有天气数据了");
                        testSubscription.unsubscribe();
                        wertherSubscription2 =RealmManage
                                .createObservable(WeatherInfoRealm.class)
                                .subscribe(weatherInfoRealm->{
                                    log.info("天气有更新：{}, {}, {}", weatherInfoRealm.getWeather(), weatherInfoRealm.getTemperature(), weatherInfoRealm.getWind());
                                }, throwable -> {
                                    log.error("异常", throwable);
                                });
                    }
                }, throwable -> {
                    log.error("异常", throwable);
                });
    }

    @Override
    public void stop() {
        weatherSubscriptions.unsubscribe();
        weatherResource.release();
    }
}
