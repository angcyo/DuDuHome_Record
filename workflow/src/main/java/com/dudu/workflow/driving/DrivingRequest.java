package com.dudu.workflow.driving;

import com.dudu.persistence.driving.FaultCode;
import com.dudu.rest.model.driving.response.FaultCodeResponse;
import com.dudu.rest.model.driving.response.GetCarBrandResponse;
import com.dudu.rest.model.common.RequestResponse;

import java.util.List;

import rx.Observable;

/**
 * 驾车相关请求
 * Created by Eaway on 2016/2/17.
 */
public interface DrivingRequest {

    /**
     * 发送加速测试数据
     *
     * @param accTestTime
     * @param testFeedId
     * @param phone
     */
    public Observable<RequestResponse> pushAcceleratedTestData(String accTestTime, String testFeedId, String phone);

    /**
     * 发送驾驶习惯数据
     *
     * @param startTime
     * @param endTime
     */
    public Observable<RequestResponse> pushDrivingHabitsData(String startTime, String endTime);

    /**
     * 车辆故障码查询
     *
     * @param faultCode
     * @param carBrand
     */
    public Observable<FaultCodeResponse> inquiryFault(String[] faultCode, String carBrand);

    /**
     * 车辆故障码查询
     *
     * @param faultCodesList
     * @param carBrand
     */
    public Observable<FaultCodeResponse> inquiryFault(List<FaultCode> faultCodesList, String carBrand);

    /**
     * 查询用户车型
     *
     * @return
     */
    public Observable<GetCarBrandResponse> getCarBrand();

}
