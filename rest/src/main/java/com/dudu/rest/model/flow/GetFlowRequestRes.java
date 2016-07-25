package com.dudu.rest.model.flow;

import com.dudu.rest.model.common.RequestResponse;

/**
 * Created by dengjun on 2016/4/22.
 * Description :
 */
public class GetFlowRequestRes extends RequestResponse{
    public GetFlowRes result;

    public class GetFlowRes {
        //该月剩余总流量
        public float remainingFlow;


        public float getRemainingFlow() {
            return remainingFlow;
        }

        public void setRemainingFlow(float remainingFlow) {
            this.remainingFlow = remainingFlow;
        }
    }
}
