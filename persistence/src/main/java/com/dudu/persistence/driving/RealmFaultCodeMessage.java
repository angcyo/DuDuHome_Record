package com.dudu.persistence.driving;

import io.realm.RealmObject;

/**
 * Created by Eaway on 2016/3/8.
 */
public class RealmFaultCodeMessage extends RealmObject {

    /**
     * ID.
     */
    private int messageId;

    /**
     * FAULT_CODE.故障编码
     */
    private String faultCode;

    /**
     * CAR_TYPE.车型
     */
    private String carType;

    /**
     * CAR_BRAND.品牌
     */
    private String carBrand;

    /**
     * FAULT_INFO.故障信息
     */
    private String faultInfo;

    /**
     * TREATMENT_WAY.解决方法
     */
    private String treatmentWay;

    /**
     * FAULT_RANK.故障等级
     */
    private String faultRank;

    /**
     * FAULT_REASON.故障可能原因
     */
    private String faultReason;

    /**
     * CREATE_TIME.创建时间
     */
    private long createTime;

    /**
     * UPDATE_TIME.修改时间
     */
    private long updateTime;

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getFaultCode() {
        return faultCode;
    }

    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public String getCarBrand() {
        return carBrand;
    }

    public void setCarBrand(String carBrand) {
        this.carBrand = carBrand;
    }

    public String getFaultInfo() {
        return faultInfo;
    }

    public void setFaultInfo(String faultInfo) {
        this.faultInfo = faultInfo;
    }

    public String getTreatmentWay() {
        return treatmentWay;
    }

    public void setTreatmentWay(String treatmentWay) {
        this.treatmentWay = treatmentWay;
    }

    public String getFaultRank() {
        return faultRank;
    }

    public void setFaultRank(String faultRank) {
        this.faultRank = faultRank;
    }

    public String getFaultReason() {
        return faultReason;
    }

    public void setFaultReason(String faultReason) {
        this.faultReason = faultReason;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
