package com.dudu.workflow.flow;

import com.dudu.rest.model.flow.FlowSyncConfigRes;
import com.dudu.rest.model.flow.FlowUpload;
import com.dudu.rest.model.flow.FlowUploadRequestRes;
import com.dudu.rest.model.flow.GetFlowRequestRes;

import rx.Observable;

/**
 * Created by dengjun on 2016/4/22.
 * Description :
 */
public interface FlowRequest {
    public Observable<FlowSyncConfigRes> flowSyncConfig();

    public Observable<FlowUploadRequestRes> flowUpload(FlowUpload flowUpload);

    public Observable<GetFlowRequestRes> getFlow();
}
