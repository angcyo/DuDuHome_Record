package com.dudu.rest.model;

/**
 * Created by Administrator on 2016/3/23.
 */
public class SetDigitPasswordRequest {

    private String platform="mirror";

    private String obId;

    private String digitPasswordState;

    private String digitPasswordMessage;

    public SetDigitPasswordRequest(String obId,String digitPasswordState,String digitPasswordMessage){
        this.obId=obId;
        this.digitPasswordState=digitPasswordState;
        this.digitPasswordMessage=digitPasswordMessage;

    }
}
