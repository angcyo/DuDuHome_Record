package com.dudu.rest.model.driving.response;

import com.dudu.rest.model.common.RequestResponse;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

/**
 * Created by Administrator on 2016/3/16.
 */
public class FaultCodeResponse extends RequestResponse {

    @SerializedName("result")
    public FaultCodeDetailMessage[] result;

    @Override
    public String toString() {
        return "FaultCodeResponse{" +
                "result=" + Arrays.toString(result) +
                '}';
    }
}
