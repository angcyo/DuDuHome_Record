package com.dudu.persistence.RobberyMessage;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Administrator on 2016/4/21.
 */
public class RealRobberyMessage extends RealmObject {

    @PrimaryKey
    private String obied;

    private boolean robberySwitch;

    private String rotatingSpeed;

    private String operationNumber;

    private String completeTime;

    public String getObied() {
        return obied;
    }

    public RealRobberyMessage() {
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
}
