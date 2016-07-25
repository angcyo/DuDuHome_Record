package com.dudu.obd;

/**
 * Created by Administrator on 2016/3/15.
 */
public class ShowFaultPageEvent {
    private String[] faultCodes;

    private String faultInfo;

    public ShowFaultPageEvent(String[] faultCodes, String faultInfo) {
        this.faultCodes = faultCodes;
        this.faultInfo = faultInfo;
    }

    public String[] getFaultCodes() {
        return faultCodes;
    }

    public String getFaultInfo() {
        return faultInfo;
    }
}
