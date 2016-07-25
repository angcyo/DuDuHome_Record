package com.dudu.weather;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.utils.TimeUtils;
import com.dudu.android.launcher.utils.WeatherUtils;
import com.dudu.workflow.common.RequestFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/1/7.
 */
public class WeatherManager {

    public static final String ChinaAndGlobal = "http://apis.baidu.com/heweather/weather/free";

    private Context mContext;

    private Logger logger;

    private String mSpeakWord;

    private Object mWeatherObj;


    public WeatherManager() {
        mContext = LauncherApplication.getContext();

        logger = LoggerFactory.getLogger("voice.weather");
    }

    private String newdate;

    private int witchDay;

    /**
     * ChinaAndGlobal网的天气数据
     */
    public void requestChinaGlobalWeather(final String city, final String date) throws UnsupportedEncodingException {

        witchDay = WeatherUtils.getWitchDay(date);
        if (witchDay < 0) {
            newdate = TimeUtils.parseTime(TimeUtils.format2, TimeUtils.format, date);

            RequestFactory.getWeatherRequest().getHistoryWeather(newdate, city)
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(historyResult -> {
                        if (historyResult != null) {
                            if (witchDay == -1) {
                                newdate = "昨天";
                            } else if (witchDay == -2) {
                                newdate = "前天";
                            } else {
                                newdate = TimeUtils.parseTime(TimeUtils.format, TimeUtils.format8, date);
                            }
                            mSpeakWord = city + newdate + "天气:\n " + historyResult.type + " \n 最高气温 "
                                    + historyResult.hightemp + " \n 最低气温 " + historyResult.lowtemp
                                    + " \n 风力是 " + historyResult.fengli + " \n 风向是 " + historyResult.fengxiang;
                            getWeatherDataSuccess();
                        } else {
                            getWeatherDataFailure();

                        }
                    }, throwable -> logger.error("requestChinaGlobalWeather", throwable));

            return;
        }

        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url = ChinaAndGlobal + "?city=" + URLEncoder.encode(city, "UTF-8");
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                logger.debug(response + "");
                try {
                    JSONObject weatherJson = response.getJSONArray("HeWeather data service 3.0").getJSONObject(0);

                    Gson gson = new Gson();
                    WeatherChinaGlobal weather = gson.fromJson(weatherJson.toString(), new TypeToken<WeatherChinaGlobal>() {}.getType());

                    if (weather != null) {
                        weather.setArea(city);
                        mSpeakWord = weather.getSpeakWord(date);
                        mWeatherObj = weather.getWeatherObj(date);
                        if (!TextUtils.isEmpty(mSpeakWord)) {
                            getWeatherDataSuccess();
                        } else {
                            getWeatherDataFailure();

                        }
                    } else {
                        getWeatherDataFailure();
                    }
                } catch (JSONException e) {
                    getWeatherDataFailure();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                getWeatherDataFailure();
                logger.error(error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("apikey", "97af1078ea7574b80a2c1653498065ac");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
        queue.start();
    }


    public interface OnWeatherDataListener {

        void onWeatherSuccess();

        void onWeatherFailure();
    }

    private OnWeatherDataListener mOnWeatherDataListener;

    public void setOnWeatherDataListener(OnWeatherDataListener l) {
        mOnWeatherDataListener = l;
    }

    private void getWeatherDataSuccess() {
        if (mOnWeatherDataListener != null) {
            mOnWeatherDataListener.onWeatherSuccess();
        }
    }

    private void getWeatherDataFailure() {
        logger.error("获取天气信息失败...");
        if (mOnWeatherDataListener != null) {
            mOnWeatherDataListener.onWeatherFailure();
        }
    }

    public String getSpeakWord() {
        return mSpeakWord;
    }

    public Object getWeatherObj() {
        return mWeatherObj;
    }

}
