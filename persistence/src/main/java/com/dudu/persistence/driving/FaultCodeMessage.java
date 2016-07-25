package com.dudu.persistence.driving;

/**
 * Created by Eaway on 2016/3/8.
 */
public class FaultCodeMessage {

    /**
     * ID.
     */
    private Integer id;

    /**
     * FAULT_CODE.故障编码
     */
    private String faultCode;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFaultCode() {
        return faultCode;
    }

    public void setFaultCode(String faultCode) {
        this.faultCode = faultCode == null ? null : faultCode.trim();
    }

    public String getCarBrand() {
        return carBrand;
    }

    public void setCarBrand(String carBrand) {
        this.carBrand = carBrand == null ? null : carBrand.trim();
    }

    public String getFaultInfo() {
        return faultInfo;
    }

    public void setFaultInfo(String faultInfo) {
        this.faultInfo = faultInfo == null ? null : faultInfo.trim();
    }

    public String getTreatmentWay() {
        return treatmentWay;
    }

    public void setTreatmentWay(String treatmentWay) {
        this.treatmentWay = treatmentWay == null ? null : treatmentWay.trim();
    }

    public String getFaultRank() {
        return faultRank;
    }

    public void setFaultRank(String faultRank) {
        this.faultRank = faultRank == null ? null : faultRank.trim();
    }

    public String getFaultReason() {
        return faultReason;
    }

    public void setFaultReason(String faultReason) {
        this.faultReason = faultReason == null ? null : faultReason.trim();
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", faultCode=").append(faultCode);
        sb.append(", carBrand=").append(carBrand);
        sb.append(", faultInfo=").append(faultInfo);
        sb.append(", treatmentWay=").append(treatmentWay);
        sb.append(", faultRank=").append(faultRank);
        sb.append(", faultReason=").append(faultReason);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }
}
