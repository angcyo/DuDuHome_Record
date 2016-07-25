package com.dudu.rest.model.driving.requestargs;

/**
 * Created by Administrator on 2016/4/18.
 */
public class InquiryFaultRequestArgs {
    private String faultCodes;
    private String carBrand;

    public InquiryFaultRequestArgs(String faultCodes, String carBrand) {
        this.faultCodes = faultCodes;
        this.carBrand = carBrand;
    }
}
