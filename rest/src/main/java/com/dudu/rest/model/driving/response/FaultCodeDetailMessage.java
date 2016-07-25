package com.dudu.rest.model.driving.response;

/**
 * Created by Administrator on 2016/3/30.
 */
public class FaultCodeDetailMessage {
    /**
     * ID.
     */
    public Integer id;

    /**
     * FAULT_CODE.故障编码
     */
    public String faultCode;

    /**
     * CAR_BRAND.品牌
     */
    public String carBrand;

    /**
     * FAULT_INFO.故障信息
     */
    public String faultInfo;

    /**
     * TREATMENT_WAY.解决方法
     */
    public String treatmentWay;

    /**
     * FAULT_RANK.故障等级
     */
    public String faultRank;

    /**
     * FAULT_REASON.故障可能原因
     */
    public String faultReason;

    /**
     * CREATE_TIME.创建时间
     */
    public long createTime;

    /**
     * UPDATE_TIME.修改时间
     */
    public long updateTime;

    /**
     * 空数据
     */
    public boolean dataIsEmpty = false;

    public FaultCodeDetailMessage(String faultCode) {
        this.faultCode = faultCode;
    }

    @Override
    public String toString() {
        return "FaultCodeDetailMessage{" +
                "id=" + id +
                ", faultCode='" + faultCode + '\'' +
                ", carBrand='" + carBrand + '\'' +
                ", faultInfo='" + faultInfo + '\'' +
                ", treatmentWay='" + treatmentWay + '\'' +
                ", faultRank='" + faultRank + '\'' +
                ", faultReason='" + faultReason + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", dataIsEmpty=" + dataIsEmpty +
                '}';
    }
}
