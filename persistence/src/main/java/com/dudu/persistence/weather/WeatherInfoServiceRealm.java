package com.dudu.persistence.weather;

import com.dudu.persistence.realmmodel.weather.WeatherInfoRealm;
import com.dudu.persistence.rx.RealmObservable;

import io.realm.Realm;
import rx.Observable;

/**
 * Created by dengjun on 2016/3/24.
 * Description :
 */
public class WeatherInfoServiceRealm implements WeatherInfoService{

    @Override
    public Observable<WeatherInfo> findWeatherInfo(final  String key) {
        return RealmObservable.object((realm) -> {
            return findWeather(realm, key);
        }).map((weatherInfoRealm ->
                //TODO value object
                new WeatherInfo(weatherInfoRealm.getWeather(),weatherInfoRealm.getTemperature(),weatherInfoRealm.getWind())
        ));
    }

    private WeatherInfoRealm findWeather(Realm realm, String key){
        WeatherInfoRealm weatherInfoRealm
                = realm.where(WeatherInfoRealm.class)
                             .equalTo(WeatherInfo.WEATHER_KEY, key).findFirst();
        return weatherInfoRealm;
    }

    @Override
    public Observable<WeatherInfo> saveWeatherInfo(final WeatherInfo weatherInfo) {
        return RealmObservable.object((realm -> {
            WeatherInfoRealm weatherInfoRealm = new WeatherInfoRealm(weatherInfo.getWeather(), weatherInfo.getTemperature(), weatherInfo.getWind());
            return realm.copyToRealmOrUpdate(weatherInfoRealm);
        })).map((weatherInfoRealm)->
                new WeatherInfo(weatherInfoRealm.getWeather(),weatherInfoRealm.getTemperature(),weatherInfoRealm.getWind()));
    }
}
