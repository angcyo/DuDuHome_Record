package com.dudu.carChecking;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/5/3.
 */
public class VehicleAnimationFragmentArgs implements Serializable {
    private boolean clearCodes;
    private String faultInfo;
    private String[] faultCodes;
    private boolean askClearCodes;
    private boolean codesTypesIs4;

    public boolean isClearCodes() {
        return clearCodes;
    }

    public void setClearCodes(boolean clearCodes) {
        this.clearCodes = clearCodes;
    }

    public String getFaultInfo() {
        return faultInfo;
    }

    public void setFaultInfo(String faultInfo) {
        this.faultInfo = faultInfo;
    }

    public String[] getFaultCodes() {
        return faultCodes;
    }

    public void setFaultCodes(String[] faultCodes) {
        this.faultCodes = faultCodes;
    }

    public boolean isAskClearCodes() {
        return askClearCodes;
    }

    public void setAskClearCodes(boolean askClearCodes) {
        this.askClearCodes = askClearCodes;
    }

    public boolean isCodesTypesIs4() {
        return codesTypesIs4;
    }

    public void setCodesTypesIs4(boolean codesTypesIs4) {
        this.codesTypesIs4 = codesTypesIs4;
    }
}
