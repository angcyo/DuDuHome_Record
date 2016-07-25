package com.dudu.rest.model;

import com.dudu.rest.model.common.RequestResponse;

/**
 * Created by Administrator on 2016/5/4.
 */
public class GetCallSubAccountResponse extends RequestResponse {
    public GetCallSubAccountResponseResult result;

    public class GetCallSubAccountResponseResult {
        public String dateCreated;
        public String subToken;
        public String voipPwd;
        public String subAccountSid;
        public String voipAccount;

        @Override
        public String toString() {
            return "GetCallSubAccountResponseResult{" +
                    "dateCreated='" + dateCreated + '\'' +
                    ", subToken='" + subToken + '\'' +
                    ", voipPwd='" + voipPwd + '\'' +
                    ", subAccountSid='" + subAccountSid + '\'' +
                    ", voipAccount='" + voipAccount + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "GetCallSubAccountResponse{" +
                "result=" + result +
                '}';
    }
}
