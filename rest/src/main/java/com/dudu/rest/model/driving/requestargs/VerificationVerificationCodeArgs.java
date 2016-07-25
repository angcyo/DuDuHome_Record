package com.dudu.rest.model.driving.requestargs;

/**
 * Created by Administrator on 2016/3/22.
 */
public class VerificationVerificationCodeArgs {

    private String platform="mirror";

    private String obeId;

    private String password;

    private String codes;

    public VerificationVerificationCodeArgs(String obeId, String password, String codes){
        this.obeId=obeId;
        this.password=password;
        this.codes=codes;
    }
}
