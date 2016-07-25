package com.dudu.rest.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by luozha on 2016/3/11.
 */
public class AppUserMessage {
    /**
     *
     * */

    /**
     * 访问网络返回的结果码
     */
    @SerializedName("resultCode")
    private int resultCode;

    /**
     * 访问网络返回的信息
     */
    @SerializedName("resultMsg")
    private String resultMsg;

    /**
     * 账号
     */
    @SerializedName("phone")
    private String account;

    /**
     * 头像地址
     */
    @SerializedName("head_url")
    private String headUrl;

    /**
     * 驾驶证地址
     */
    @SerializedName("driver_license_url")
    private String driverLicenseUrl;

    /**
     * obeId
     */
    @SerializedName("obeId")
    private String oneId;

    /**
     * 昵称
     */
    @SerializedName("nickname")
    private String nickName;

    /**
     * 防盗审批状态
     */
    @SerializedName("thief_audit_state")
    private String thiefAuditState;

    /**
     * 行驶证图片地址
     */
    @SerializedName("driving_license_url")
    private String drivingLicenseUrl;

    /**
     * 防盗审批说明
     */
    @SerializedName("thief_audit_desc")
    private String thiefAuditDesc;

    /**
     * "防盗状态  0:未开启 1:密码开启 2:指纹开启 3:图案密码开启 4:无密码开启"
     */
    @SerializedName("protect_thief_state")
    private String protectThiefState;

    /**
     * 防盗开关状态
     */
    @SerializedName("thief_switch_state")
    private String thiefSwitchState;

    /**
     * "防盗密码信息 手势密码 指纹密码
     */
    @SerializedName("protect_thief_password")
    private String protectThiefPassword;

    public AppUserMessage() {
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    public String getDriverLicenseUrl() {
        return driverLicenseUrl;
    }

    public void setDriverLicenseUrl(String driverLicenseUrl) {
        this.driverLicenseUrl = driverLicenseUrl;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getOneId() {
        return oneId;
    }

    public void setOneId(String oneId) {
        this.oneId = oneId;
    }

    public String getThiefAuditState() {
        return thiefAuditState;
    }

    public void setThiefAuditState(String thiefAuditState) {
        this.thiefAuditState = thiefAuditState;
    }

    public String getDrivingLicenseUrl() {
        return drivingLicenseUrl;
    }

    public void setDrivingLicenseUrl(String drivingLicenseUrl) {
        this.drivingLicenseUrl = drivingLicenseUrl;
    }

    public String getThiefAuditDesc() {
        return thiefAuditDesc;
    }

    public void setThiefAuditDesc(String thiefAuditDesc) {
        this.thiefAuditDesc = thiefAuditDesc;
    }

    public String getProtectThiefState() {
        return protectThiefState;
    }

    public void setProtectThiefState(String protectThiefState) {
        this.protectThiefState = protectThiefState;
    }

    public String getThiefSwitchState() {
        return thiefSwitchState;
    }

    public void setThiefSwitchState(String thiefSwitchState) {
        this.thiefSwitchState = thiefSwitchState;
    }

    public String getProtectThiefPassword() {
        return protectThiefPassword;
    }

    public void setProtectThiefPassword(String protectThiefPassword) {
        this.protectThiefPassword = protectThiefPassword;
    }
}
