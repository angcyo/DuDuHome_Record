package com.dudu.rest.model.flow;

import com.dudu.rest.model.common.RequestResponse;

/**
 * Created by dengjun on 2016/4/22.
 * Description :
 */
public class FlowUploadRequestRes extends RequestResponse {
    public FlowUploadRes result;

    public class FlowUploadRes {
        //该月剩余总流量
        public float remainingFlow = 1048576;

        /* 流量开关*/
        public int trafficControl = -1;

        /* 当日流量超限异常*/
        public int exceptionState = 0;

        /*每月流量告警 */
        public int trafficState = -1;


        public int getTrafficControl() {
            return trafficControl;
        }

        public void setTrafficControl(int trafficControl) {
            this.trafficControl = trafficControl;
        }

        public float getRemainingFlow() {
            return remainingFlow;
        }

        public void setRemainingFlow(float remainingFlow) {
            this.remainingFlow = remainingFlow;
        }

        public int getExceptionState() {
            return exceptionState;
        }

        public void setExceptionState(int exceptionState) {
            this.exceptionState = exceptionState;
        }

        public int getTrafficState() {
            return trafficState;
        }

        public void setTrafficState(int trafficState) {
            this.trafficState = trafficState;
        }

    }
}
