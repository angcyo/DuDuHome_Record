package com.dudu.rest.service;

import com.dudu.rest.model.active.ActiveRequestResponse;
import com.dudu.rest.model.common.RequestBody;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by dengjun on 2016/4/22.
 * Description :
 */
public interface ActiveService {
    public static final String ACTIVE_DEVICE = "/done/obe/obeActivation";
    public static final String CHECK_ACTIVE = "/done/obe/detectionObeActivation";

    @POST("/done/obe/obeActivation/{businessId}/mirror")
    public Observable<ActiveRequestResponse> activeDevice(@Path("businessId") String businessId, @Body RequestBody requestBody);

    @POST("/done/obe/detectionObeActivation/{businessId}/mirror")
    public Observable<ActiveRequestResponse> checkActive(@Path("businessId") String businessId, @Body RequestBody requestBody);
}
