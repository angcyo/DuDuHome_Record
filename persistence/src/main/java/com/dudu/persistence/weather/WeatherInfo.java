package com.dudu.persistence.weather;

/**
 * Created by lxh on 2016/1/22.
 */
public class WeatherInfo {
    public static final String WEATHER_KEY = "weatherKey";

    private String weather;

    private String temperature;

    private String wind;

    public WeatherInfo(String weather, String temperature, String wind) {
        this.weather = weather;
        this.temperature = temperature;
        this.wind = wind;
    }

    public String getWeather() {

        return weather;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getWind() {
        return wind;
    }
}
