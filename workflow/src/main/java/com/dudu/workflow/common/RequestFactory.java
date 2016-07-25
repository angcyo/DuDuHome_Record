package com.dudu.workflow.common;

import com.dudu.workflow.active.ActiveRequest;
import com.dudu.workflow.active.ActiveRequestRetrofitImpl;
import com.dudu.workflow.app.AppRequest;
import com.dudu.workflow.app.AppRequestRetrofitImpl;
import com.dudu.workflow.driving.DrivingRequest;
import com.dudu.workflow.driving.DrivingRequestRetrofitImpl;
import com.dudu.workflow.flow.FlowRequest;
import com.dudu.workflow.flow.FlowRequestRetrofitImpl;
import com.dudu.workflow.guard.GuardRequest;
import com.dudu.workflow.guard.GuardRequestRetrofitImpl;
import com.dudu.workflow.portal.PortalRequest;
import com.dudu.workflow.portal.PortalRequestRetrofitImpl;
import com.dudu.workflow.robbery.RobberyRequest;
import com.dudu.workflow.robbery.RobberyRequestRetrofitImpl;
import com.dudu.workflow.weather.WeatherRequest;
import com.dudu.workflow.weather.WeatherRequestRetrofitImpl;

/**
 * Created by Administrator on 2016/2/16.
 */
public class RequestFactory {

    public static RobberyRequest getRobberyRequest() {
        return new RobberyRequestRetrofitImpl();
    }

    public static GuardRequest getGuardRequest() {
        return new GuardRequestRetrofitImpl();
    }

    public static DrivingRequest getDrivingRequest() {
        return new DrivingRequestRetrofitImpl();
    }

    public static AppRequest getAppRequest() {
        return new AppRequestRetrofitImpl();
    }


    public static PortalRequest getPortalRequest() {
        return new PortalRequestRetrofitImpl();
    }

    public static ActiveRequest getActiveRequest() {
        return new ActiveRequestRetrofitImpl();
    }

    public static FlowRequest getFlowRequest() {
        return new FlowRequestRetrofitImpl();
    }

    public static WeatherRequest getWeatherRequest() {
        return new WeatherRequestRetrofitImpl();
    }
}
