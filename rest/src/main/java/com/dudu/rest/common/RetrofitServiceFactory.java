package com.dudu.rest.common;

import com.dudu.rest.service.ActiveService;
import com.dudu.rest.service.AppService;
import com.dudu.rest.service.BaiduWeatherService;
import com.dudu.rest.service.DrivingService;
import com.dudu.rest.service.FlowService;
import com.dudu.rest.service.GuardService;
import com.dudu.rest.service.PortalService;
import com.dudu.rest.service.RobberyService;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Administrator on 2016/2/15.
 */
public class RetrofitServiceFactory {

    private static Retrofit getRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(IpConfig.getServerAddress())//设置服务端地址
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//设置支持RX数据响应
                .addConverterFactory(GsonConverterFactory.create())//使用Gson封装、解析数据
                .build();
    }

    public static Retrofit getBaiduApiRetrofit() {
        return new Retrofit.Builder()
                .baseUrl("http://apis.baidu.com")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//设置支持RX数据响应
                .addConverterFactory(GsonConverterFactory.create())//使用Gson封装、解析数据
                .build();
    }

    public static RobberyService getRobberyService() {
        return getRetrofit().create(RobberyService.class);
    }

    public static GuardService getGuardService() {
        return getRetrofit().create(GuardService.class);
    }

    public static DrivingService getDrivingService() {
        return getRetrofit().create(DrivingService.class);
    }

    public static FlowService getFlowService() {
        return getRetrofit().create(FlowService.class);
    }

    public static ActiveService getActiveService() {
        return getRetrofit().create(ActiveService.class);
    }


    public static PortalService getPortalService() {
        return getRetrofit().create(PortalService.class);
    }

    public static AppService getAppService() {
        return getRetrofit().create(AppService.class);
    }

    public static BaiduWeatherService getBaiduWeatherService() {
        return getBaiduApiRetrofit().create(BaiduWeatherService.class);
    }

}

