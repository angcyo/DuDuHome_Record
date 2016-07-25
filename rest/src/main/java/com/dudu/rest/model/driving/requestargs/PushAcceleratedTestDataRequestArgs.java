package com.dudu.rest.model.driving.requestargs;

/**
 * Created by Administrator on 2016/3/30.
 */
public class PushAcceleratedTestDataRequestArgs {
    public String obeId;
    public String platform;
    public String testResult;
    public String testFeedId;
    public String phone;

    public PushAcceleratedTestDataRequestArgs(String obeId, String platform, String testResult, String testFeedId, String phone) {
        this.obeId = obeId;
        this.platform = platform;
        this.testResult = testResult;
        this.testFeedId = testFeedId;
        this.phone = phone;
    }
}
