package com.dudu.persistence.driving;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Administrator on 2016/3/8.
 */
public class RealmFaultCode extends RealmObject {

    @PrimaryKey
    private int carCheckType;

    private String faultCode;

    public int getCarCheckType() {
        return carCheckType;
    }

    public void setCarCheckType(int carCheckType) {
        this.carCheckType = carCheckType;
    }


    public String getFaultCode() {
        return faultCode;
    }

    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode;
    }
}
