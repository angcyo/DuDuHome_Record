package com.dudu.workflow.flow;

import com.dudu.commonlib.CommonLib;
import com.dudu.rest.common.IpUtils;
import com.dudu.rest.common.RetrofitServiceFactory;
import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.model.flow.FlowSyncConfigRes;
import com.dudu.rest.model.flow.FlowUpload;
import com.dudu.rest.model.flow.FlowUploadRequestRes;
import com.dudu.rest.model.flow.GetFlowRequestRes;
import com.dudu.rest.service.FlowService;

import rx.Observable;

/**
 * Created by dengjun on 2016/4/22.
 * Description :
 */
public class FlowRequestRetrofitImpl implements FlowRequest {
    @Override
    public Observable<FlowSyncConfigRes> flowSyncConfig() {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(FlowService.FLOW_SYNC_CONFIG, null, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory
                .getFlowService()
                .flowSyncConfig(CommonLib.getInstance().getObeId(), requestBody);
    }

    @Override
    public Observable<FlowUploadRequestRes> flowUpload(FlowUpload flowUpload) {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(FlowService.FLOW_UPLOAD, flowUpload, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory
                .getFlowService()
                .flowUpload(CommonLib.getInstance().getObeId(), requestBody);
    }

    @Override
    public Observable<GetFlowRequestRes> getFlow() {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(FlowService.GET_FLOW, null, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory
                .getFlowService()
                .getFlow(CommonLib.getInstance().getObeId(), requestBody);
    }
}
