package com.dudu.rest.service;

import com.dudu.rest.model.CheckUpdateResponse;
import com.dudu.rest.model.GetCallSubAccountResponse;
import com.dudu.rest.model.common.RequestBody;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Administrator on 2016/3/22.
 */
public interface AppService {

    public static final String UPGRADE_CHECK = "/upgrade/check";
    public static final String GET_CALL_SUB_ACCOUNT = "/getCallSubAccount";

    @POST("/upgrade/check/{businessId}/mirror")
    public Observable<CheckUpdateResponse> checkUpdate(@Path("businessId") String businessId, @Body RequestBody requestBody);

    @POST("/call/getCallSubAccount/{businessId}/mirror")
    public Observable<GetCallSubAccountResponse> getCallSubAccount(@Path("businessId") String businessId, @Body RequestBody requestBody);


}
