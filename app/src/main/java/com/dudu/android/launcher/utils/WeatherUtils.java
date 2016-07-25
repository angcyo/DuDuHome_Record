package com.dudu.android.launcher.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.services.weather.LocalDayWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.R;
import com.dudu.event.DeviceEvent;
import com.dudu.monitor.repo.location.LocationManage;
import com.dudu.resource.location.utils.LocationUtils;
import com.dudu.weather.WeatherStream;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;

public class WeatherUtils {

    private static final String TAG = "WeatherUtils";

    private final static String[] WEATHER_STRINGS = new String[]{"晴", "多云",
            "阴", "阵雨", "雷阵雨", "小雨", "中雨", "大雨", "暴雨", "大暴雨", "特大暴雨", "小雪",
            "中雪", "大雪", "阵雪", "暴雪", "雾", "小雨转中雨", "中雨转大雨", "大雨转暴雨", "暴雨转大暴雨",
            "大暴雨转特大暴雨", "小雪转中雪", "中雪转大雪", "大雪转暴雪"};

    private static List<String> weatherList = new ArrayList<String>();

    static {
        weatherList = Arrays.asList(WEATHER_STRINGS);
    }

    public static boolean isNight(long time) {
        SimpleDateFormat df = new SimpleDateFormat("HH", Locale.getDefault());
        String timeStr = df.format(new Date(System.currentTimeMillis()));
        try {
            int timeHour = Integer.parseInt(timeStr);
            return (timeHour >= 18 || timeHour <= 6);
        } catch (NumberFormatException e) {
            Log.v(TAG, e.getMessage() + "");
        }
        return false;
    }

    public static int getWeatherType(String weather) {
        if (TextUtils.isEmpty(weather)) {
            return Constants.NO_VALUE_FLAG;
        }

        int type = weatherList.indexOf(weather);
        if (type == -1) {
            return Constants.NO_VALUE_FLAG;
        }

        return type;
    }

    public static int getWeatherIcon(int type) {
        if (isNight(System.currentTimeMillis()))
            switch (type) {
                case Constants.SUNNY:
                    return R.drawable.weather_sunny;
                case Constants.CLOUDY:
                    return R.drawable.weather_cloudy;
                case Constants.LIGHT_RAIN:
                case Constants.MODERATE_RAIN:
                case Constants.HEAVY_RAIN:
                case Constants.SHOWER:
                case Constants.STORM:
                    return R.drawable.weather_rain;
                default:
                    break;
            }

        switch (type) {
            case Constants.SUNNY:
                return R.drawable.weather_sunny;
            case Constants.CLOUDY:
                return R.drawable.weather_cloudy;
            case Constants.OVERCAST:
                return R.drawable.weather_overcast;
            case Constants.SHOWER:
                return R.drawable.weather_rain;
            case Constants.THUNDERSHOWER:
                return R.drawable.weather_thunder_shower;
            case Constants.LIGHT_RAIN:
            case Constants.MODERATE_RAIN:
            case Constants.HEAVY_RAIN:
            case Constants.LIGHT_TO_MODERATE_RAIN:
            case Constants.MODERATE_TO_HEAVY_RAIN:
            case Constants.RAIN_TO_STORM:
                return R.drawable.weather_rain;
            case Constants.STORM:
            case Constants.HEAVY_STORM:
            case Constants.SEVERE_STORM:
            case Constants.STORM_TO_HEAVY_STORM:
            case Constants.HEAVY_TO_SEVERE_STORM:
                return R.drawable.weather_storm;
            case Constants.LIGHT_SNOW:
            case Constants.MODERATE_SNOW:
            case Constants.HEAVY_SNOW:
            case Constants.LIGHT_TO_MODERATE_SNOW:
            case Constants.MODERATE_TO_HEAVY_SNOW:
            case Constants.HEAVY_TO_SNOWSTORM:
                return R.drawable.weather_snow;
            case Constants.SNOWSTORM:
                return R.drawable.weather_snow_storm;
            case Constants.SNOW_SHOWER:
                return R.drawable.weather_snow_shower;
            case Constants.FOGGY:
                return R.drawable.weather_foggy;
            default:
                return R.drawable.weather_cloudy;
        }
    }

    public static void requestWeather(Context context) {
        String currentCity = getCurrentCity();
        if (currentCity != null) {
            LogUtils.v("weather", "获取当前的额城市为：" + currentCity);
            WeatherSearchQuery mQuery = new WeatherSearchQuery(currentCity, WeatherSearchQuery.WEATHER_TYPE_LIVE);
            WeatherSearch mSearch = new WeatherSearch(context);
            mSearch.setOnWeatherSearchListener(new MyWeatherSearchListener());
            mSearch.setQuery(mQuery);
            mSearch.searchWeatherAsyn(); //异步搜索
        }

    }

    public static String getCurrentCity() {
        String currentCity = "";
        if (LocationManage.getInstance().getCurrentLocation() != null) {
            if (!TextUtils.isEmpty(LocationManage.getInstance().getCurrentLocation().getCity())) {
                currentCity = LocationManage.getInstance().getCurrentLocation().getCity();
            } else {
                currentCity = WeatherStream.getInstance().getCity();
            }
        }

        return currentCity;
    }

    public static int daysBetween(Date start, Date end) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            start = sdf.parse(sdf.format(start));
            end = sdf.parse(sdf.format(end));
            Calendar cal = Calendar.getInstance();
            cal.setTime(start);
            long time1 = cal.getTimeInMillis();
            cal.setTime(end);
            long time2 = cal.getTimeInMillis();
            long between_days = (time2 - time1) / (1000 * 3600 * 24);

            return Integer.parseInt(String.valueOf(between_days));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * @param province 需要处理的字符串
     * @return 过滤一些末尾结束的文字(省)，避免查询不到结果
     */
    public static String getQueryProvince(String province) {

        if (!TextUtils.isEmpty(province)) {
            if (province.endsWith("省")) {
                province = province.substring(0, province.length() - 1);
            }
        }

        return province;
    }

    public static String getQueryCity(String city) {
        String target = "";

        if (!TextUtils.isEmpty(city)) {
            if (city.length() > 2) {
                if (city.endsWith("县")) {
                    target = city.substring(0, city.length() - 1);
                } else if (city.endsWith("市")) {
                    target = city.substring(0, city.length() - 1);
                } else {
                    target = city;
                }
            } else {
                target = city;
            }
        }

        return target;
    }

    /**
     * @param timeStr 需要处理的字符串 eg:20150915
     * @return 过滤一些末尾结束的文字(市，县)，避免查询不到结果
     */
    public static String getQueryTime(String timeStr) {
        String target;

        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyyMMdd");

        if (!TextUtils.isEmpty(timeStr)) {
            target = timeStr;
        } else {
            target = targetFormat.format(new Date());
        }

        return target;
    }

    public static String getDate(int day) {
        Date date = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        String nowDate = sf.format(date);
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sf.parse(nowDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        cal.add(Calendar.DAY_OF_YEAR, day);
        String nextDate = sf.format(cal.getTime());
        return nextDate;
    }

    private static class MyWeatherSearchListener implements WeatherSearch.OnWeatherSearchListener {
        @Override
        public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int i) {
            if (i == 0) {
                if (localWeatherLiveResult != null && localWeatherLiveResult.getLiveResult() != null) {
                    LocalWeatherLive weatherLive = localWeatherLiveResult.getLiveResult();
                    String weather = weatherLive.getWeather();
                    String temperature = weatherLive.getTemperature();
                    String wind = weatherLive.getWindDirection() + "风" + weatherLive.getWindPower() + "级";
                    String weatherText = weather + "\n温度" + temperature + "℃\n" + wind;
                    LocationUtils.getInstance(LauncherApplication.getContext()).setCurrentCityWeather(weatherText);
                    EventBus.getDefault().post(new DeviceEvent.Weather(weather, temperature));
                } else {
                    LogUtils.v("weather", "获取天气失败...");
                }
            } else {
                LogUtils.v("weather", "获取天气失败..");
            }
        }


        @Override
        public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {
            if (i == 0) {
                if (localWeatherForecastResult != null && localWeatherForecastResult.getWeatherForecastQuery() != null) {
                    LocalWeatherForecast mForecast = localWeatherForecastResult.getForecastResult();
                    List<LocalDayWeatherForecast> list = mForecast.getWeatherForecast();
                    for (LocalDayWeatherForecast forecast : list) {
                        LogUtils.v("weather", "date:" + forecast.getDate());
                        LogUtils.v("weather", "tem:" + forecast.getDayTemp() + "-" + forecast.getNightTemp());
                    }
                } else {
                    LogUtils.v("weather", "获取天气失败...");
                }
            } else {
                LogUtils.v("weather", "获取天气失败..");
            }
        }
    }


    public static int getWitchDay(String time) {
        Date todayDate = new Date();
        Date queryDate = null;
        try {
            queryDate = new SimpleDateFormat("yyyyMMdd").parse(time);
        } catch (ParseException e) {

        }

        return daysBetween(todayDate, queryDate);
    }
}
