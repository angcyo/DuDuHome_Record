package com.dudu.rest.model;

import com.dudu.persistence.RobberyMessage.RobberyMessage;

/**
 * Created by Administrator on 2016/3/17.
 */
public class SetRobberySwitchStateRequest {

    private String robberySwitchs;

    private String revolutions;

    private String numberOfOperations;

    private String completeTime;

    public SetRobberySwitchStateRequest(RobberyMessage robberyMessage) {
        this.robberySwitchs=robberyMessage.isRobberySwitch()?"1":"0";
        this.revolutions=robberyMessage.getRotatingSpeed();
        this.completeTime=robberyMessage.getCompleteTime();
        this.numberOfOperations=robberyMessage.getOperationNumber();

    }
}
