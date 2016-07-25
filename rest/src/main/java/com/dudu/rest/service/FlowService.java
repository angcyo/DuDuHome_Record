package com.dudu.rest.service;

import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.model.flow.FlowSyncConfigRes;
import com.dudu.rest.model.flow.FlowUploadRequestRes;
import com.dudu.rest.model.flow.GetFlowRequestRes;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by dengjun on 2016/4/22.
 * Description :
 */
public interface FlowService {
    public static final String FLOW_SYNC_CONFIG = "done/obe/synConfiguration";
    public static final String FLOW_UPLOAD = "/done/traffic/trafficReport";
    public static final String GET_FLOW = "/done/obe/getFlow";

    @POST("/done/obe/synConfiguration/{businessId}/mirror")
    public Observable<FlowSyncConfigRes> flowSyncConfig(@Path("businessId") String businessId, @Body RequestBody requestBody);

    @POST("/done/traffic/trafficReport/{businessId}/mirror")
    public Observable<FlowUploadRequestRes> flowUpload(@Path("businessId") String businessId, @Body RequestBody requestBody);

    @POST("/done/obe/getFlow/{businessId}/mirror")
    public Observable<GetFlowRequestRes> getFlow(@Path("businessId") String businessId, @Body RequestBody requestBody);
}
