package com.dudu.persistence.driving;

import com.dudu.commonlib.utils.TextVerify;

import java.util.Arrays;

/**
 * Created by Administrator on 2016/3/8.
 */
public class FaultCode {
    public static final String CARCHECKTYPE_ROW = "carCheckType";
    public static final String FAULTCODE_ROW = "faultCode";
    /**
     * 发动机
     */
    public static final int ECM = 0;

    /**
     * 变速箱
     */
    public static final int TCM = 1;
    /**
     * ABS
     */
    public static final int ABS = 2;
    /**
     * 轮胎
     */
    public static final int WSB = 3;
    /**
     * 气囊
     */
    public static final int SRS = 4;

    private int carCheckType;

    private String faultCodeString;

    private String[] faultCode;

    public FaultCode(RealmFaultCode realmFaultCode) {
        this.carCheckType = realmFaultCode.getCarCheckType();
        this.faultCodeString = realmFaultCode.getFaultCode();
        if(!TextVerify.isEmpty(realmFaultCode.getFaultCode())){
            this.faultCode = realmFaultCode.getFaultCode().split("\\|");
        } else {
            this.faultCode = new String[0];
        }
    }

    public int getCarCheckType() {
        return carCheckType;
    }

    public void setCarCheckType(int carCheckType) {
        this.carCheckType = carCheckType;
    }

    public String[] getFaultCode() {
        return faultCode;
    }

    public void setFaultCode(String[] faultCode) {
        this.faultCode = faultCode;
    }

    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode.split("\\|");
    }

    public String getFaultCodeString() {
        return faultCodeString;
    }

    public void setFaultCodeString(String faultCodeString) {
        this.faultCodeString = faultCodeString;
    }

    @Override
    public String toString() {
        return "FaultCode{" +
                "carCheckType=" + carCheckType +
                ", faultCodeString='" + faultCodeString + '\'' +
                ", faultCode=" + Arrays.toString(faultCode) +
                '}';
    }

}
