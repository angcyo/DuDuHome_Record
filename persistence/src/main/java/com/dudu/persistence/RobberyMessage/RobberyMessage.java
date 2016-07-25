package com.dudu.persistence.RobberyMessage;

import com.dudu.commonlib.CommonLib;

/**
 * Created by Administrator on 2016/4/21.
 */
public class RobberyMessage {
    /**
     * 设备号
     */
    private String obied;

    /**
     * 防劫开关的状态
     */
    private boolean robberySwitch;

    /**
     * 转速
     */
    private String rotatingSpeed;

    /***
     * 操作的次数
     */
    private String operationNumber;

    /**
     * 完成的时间
     */
    private String completeTime;

    public RobberyMessage() {
        this.obied = CommonLib.getInstance().getObeId();
    }

    public RobberyMessage(RealRobberyMessage realRobberyMessage) {
        this.obied = realRobberyMessage.getObied();
        this.robberySwitch = realRobberyMessage.isRobberySwitch();
        this.rotatingSpeed = realRobberyMessage.getRotatingSpeed();
        this.operationNumber = realRobberyMessage.getOperationNumber();
        this.completeTime = realRobberyMessage.getCompleteTime();
    }


    public String getObied() {
        return obied;
    }

    public void setObied(String obied) {
        this.obied = obied;
    }

    public boolean isRobberySwitch() {
        return robberySwitch;
    }

    public void setRobberySwitch(boolean robberySwitch) {
        this.robberySwitch = robberySwitch;
    }

    public String getRotatingSpeed() {
        return rotatingSpeed;
    }

    public void setRotatingSpeed(String rotatingSpeed) {
        this.rotatingSpeed = rotatingSpeed;
    }

    public String getOperationNumber() {
        return operationNumber;
    }

    public void setOperationNumber(String operationNumber) {
        this.operationNumber = operationNumber;
    }

    public String getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(String completeTime) {
        this.completeTime = completeTime;
    }

    @Override
    public String toString() {
        return "RobberyMessage{" +
                "obied='" + obied + '\'' +
                ", robberySwitch=" + robberySwitch +
                ", rotatingSpeed='" + rotatingSpeed + '\'' +
                ", operationNumber='" + operationNumber + '\'' +
                ", completeTime='" + completeTime + '\'' +
                '}';
    }
}
