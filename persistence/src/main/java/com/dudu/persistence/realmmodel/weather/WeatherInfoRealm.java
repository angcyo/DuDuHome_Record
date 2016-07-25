package com.dudu.persistence.realmmodel.weather;

import com.dudu.persistence.weather.WeatherInfo;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by dengjun on 2016/3/24.
 * Description :
 */
public class WeatherInfoRealm extends RealmObject {

    @PrimaryKey
    private  String weatherKey;

    private String weather;

    private String temperature;

    private String wind;

    public WeatherInfoRealm() {
    }

    public WeatherInfoRealm(String weather, String temperature, String wind) {
        this.weather = weather;
        this.temperature = temperature;
        this.wind = wind;
        weatherKey = WeatherInfo.WEATHER_KEY;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public String getWeatherKey() {
        return weatherKey;
    }

    public void setWeatherKey(String weatherKey) {
        this.weatherKey = weatherKey;
    }
}
