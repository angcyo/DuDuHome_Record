package com.dudu.rest.service;

import com.dudu.rest.model.GetGuardStatusResponse;
import com.dudu.rest.model.SetDigitPasswordRequest;
import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.model.common.RequestResponse;
import com.dudu.rest.model.driving.requestargs.VerificationVerificationCodeArgs;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

/**
 * 防盗相关接口
 * Created by Administrator on 2016/2/16.
 */
public interface GuardService {

    public static final String THEFT_SETTHIEFSWITCHSTATE = "/theft/setThiefSwitchState";
    public static final String THEFT_GETSTATUS = "/theft/getStatus";

    /**
     * 防盗开关
     *
     * @return
     */
    @POST("/theft/setThiefSwitchState/{businessId}/mirror")
    public Observable<RequestResponse> setThiefSwitchState(@Path("businessId") String businessId, @Body RequestBody request);

    /**
     * 获取防盗状态
     *
     * @param businessId app端是 phone,后视镜是 obeId
     */
    @POST("/theft/getStatus/{businessId}/mirror")
    Observable<GetGuardStatusResponse> getStatus(@Path("businessId") String businessId, @Body RequestBody request);

    /**
     * 设置数字密码
     */
    @POST("theft/setProtectThiefPassword")
    Observable<RequestResponse> setDigitPassword(@Body SetDigitPasswordRequest setDigitPasswordRequest);

    /**
     * 获取验证码
     **/
    @GET("registered/sms/{businessId}/resendSms/mirror")
    Observable<RequestResponse> obtainVerificationCode(@Path("businessId") String businessId);

    /**
     * 设置密码：发送验证码和密码
     **/
    @POST("registered/verifyPhone")
    Observable<RequestResponse> verificationVerificationCode(@Body VerificationVerificationCodeArgs verificationVerificationCodeRequest);

}
