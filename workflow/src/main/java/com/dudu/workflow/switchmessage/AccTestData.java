package com.dudu.workflow.switchmessage;

/**
 * Created by Administrator on 2016/2/20.
 */
public class AccTestData {

    private String accTotalTime;
    private String accType;
    private String testFeedId;
    private String phone;
    private String speed;

    public AccTestData(String accType, String accTotalTime, String testFeedId, String phone,String speed) {
        this.accTotalTime = accTotalTime;
        this.testFeedId = testFeedId;
        this.accType = accType;
        this.phone = phone;
        this.speed=speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getSpeed() {
        return speed;
    }

    public String getAccTotalTime() {
        return accTotalTime;
    }

    public void setAccTotalTime(String accTotalTime) {
        this.accTotalTime = accTotalTime;
    }

    public String getAccType() {
        return accType;
    }

    public void setAccType(String type) {
        this.accType = type;
    }

    public String getTestFeedId() {
        return testFeedId;
    }

    public void setTestFeedId(String testFeedId) {
        this.testFeedId = testFeedId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

