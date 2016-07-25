package com.dudu.rest.model.active;

import com.dudu.rest.model.common.RequestResponse;
import com.google.gson.annotations.SerializedName;

/**
 * Created by dengjun on 2016/4/22.
 * Description :
 */
public class ActiveRequestResponse extends RequestResponse {
    @SerializedName("result")
    public ActiveDeviceResInfo result;

    public class ActiveDeviceResInfo{
        public String tencentPayUri;

        public String aliPayUri;
    }
}
