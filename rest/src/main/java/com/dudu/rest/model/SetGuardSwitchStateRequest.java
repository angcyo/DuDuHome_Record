package com.dudu.rest.model;

/**
 * Created by Administrator on 2016/3/15.
 */
public class SetGuardSwitchStateRequest {

    private String platform = "mirror";

    private String thiefSwitchState;

    private String obeId;

    public SetGuardSwitchStateRequest(String thiefSwitchState, String obeId) {
        this.thiefSwitchState = thiefSwitchState;
        this.obeId = obeId;
    }
}
