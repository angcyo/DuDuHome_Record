package com.dudu.rest.model.common;

import java.util.UUID;

/**
 * Created by Administrator on 2016/4/16.
 */
public class RequestArgs {

    private String methodName;
    private String guid;
    private String json;

    public RequestArgs(String methodName, String json) {
        this.methodName = methodName;
        this.guid = UUID.randomUUID().toString();
        this.json = json;
    }
}
