package com.dudu.rest.service;

import com.dudu.rest.model.driving.response.FaultCodeResponse;
import com.dudu.rest.model.driving.response.GetCarBrandResponse;
import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.model.common.RequestResponse;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Administrator on 2016/2/17.
 */
public interface DrivingService {

    public static final String CAR_GETCARBRAND = "/car/getCarBrand";
    public static final String VEHICLESELFTEST_GETFAULTTREATMENT = "/vehicleSelfTest/getFaultTreatment";
    public static final String DRIVESCORERECORD_CALCULATESCORE = "/driveScoreRecord/calculateScore";
    public static final String TEST_MIRRORTOAPP = "/test/mirrorToApp";

    /**
     * 提交加速测试数据
     *
     * @param requestBody
     * @return
     */
    @POST("/test/mirrorToApp/{businessId}/mirror")
    public Observable<RequestResponse> pushAcceleratedTestData(@Path("businessId") String businessId, @Body RequestBody requestBody);

    /**
     * 提交驾驶习惯数据
     *
     * @param requestBody
     * @return
     */
    @POST("/driveScoreRecord/calculateScore/{businessId}/mirror")
    public Observable<RequestResponse> pushDrivingHabits(@Path("businessId") String businessId, @Body RequestBody requestBody);

    /**
     * 故障查询
     *
     * @return
     */
    @POST("/vehicleSelfTest/getFaultTreatment/{businessId}/mirror")
    public Observable<FaultCodeResponse> inquiryFault(@Path("businessId") String businessId, @Body RequestBody requestBody);

    /**
     * 获取车型
     *
     * @return
     */
    @POST("/car/getCarBrand/{businessId}/mirror")
    public Observable<GetCarBrandResponse> getCarBrand(@Path("businessId") String businessId, @Body RequestBody requestBody);
}
