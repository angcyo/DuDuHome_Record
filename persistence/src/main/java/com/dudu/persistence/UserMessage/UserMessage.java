package com.dudu.persistence.UserMessage;

/**
 * Created by Administrator on 2016/3/22.
 */
public class UserMessage {

    /**
     * 设备号
     * */
    private long obeId;


    /**
     * 手势密码
     * */
    private String gesturePassword;

    /**
     * 手势密码的开关状态
     * */
    private boolean gesturePasswordSwitchState;


    /**
     * 数字密码
     * */
    private String digitPassword;


    /**
     * 数字密码的开关状态
     * */
    private boolean digitPasswordSwitchState;


  /**
   * 车辆的品牌
   * */
    private String carTypeName;

    /**
     * 车辆品牌的代号
     * */
    private long carType;

    /**
     *
     */
    private long audit_state;

    /**
     * 是否设置过车的品牌
     * */
    private boolean carTypeSetted;

    /**
     * 车辆的车型
     * */
    private String vehicleModel;

    public UserMessage() {

    }

    public UserMessage(RealUserMessage realUserMessage) {
        this.obeId = realUserMessage.getObeId();
        this.gesturePassword = realUserMessage.getGesturePassword();
        this.gesturePasswordSwitchState = realUserMessage.isGesturePasswordSwitchState();
        this.digitPassword = realUserMessage.getDigitPassword();
        this.digitPasswordSwitchState = realUserMessage.isDigitPasswordSwitchState();
        this.carType = realUserMessage.getCarType();
        this.carTypeName = realUserMessage.getCarTypeName();
        this.vehicleModel=realUserMessage.getVehicleModel();
        this.carTypeSetted = realUserMessage.isCarTypeSetted();
        this.audit_state = realUserMessage.getAuditState();
    }

    public long getObeId() {
        return obeId;
    }

    public void setObeId(long obeId) {
        this.obeId = obeId;
    }

    public String getGesturePassword() {
        return gesturePassword;
    }

    public void setGesturePassword(String gesturePassword) {
        this.gesturePassword = gesturePassword;
    }

    public String getDigitPassword() {
        return digitPassword;
    }

    public void setDigitPassword(String digitPassword) {
        this.digitPassword = digitPassword;
    }

    public String getCarTypeName() {
        return carTypeName;
    }

    public void setCarTypeName(String carTypeName) {
        this.carTypeName = carTypeName;
    }

    public long getCarType() {
        return carType;
    }

    public void setCarType(long carType) {
        this.carType = carType;
    }


    public long getAudit_state() {
        return audit_state;
    }

    public void setAudit_state(long audit_state) {
        this.audit_state = audit_state;
    }

    public boolean isGesturePasswordSwitchState() {
        return gesturePasswordSwitchState;
    }

    public void setGesturePasswordSwitchState(boolean gesturePasswordSwitchState) {
        this.gesturePasswordSwitchState = gesturePasswordSwitchState;
    }

    public boolean isDigitPasswordSwitchState() {
        return digitPasswordSwitchState;
    }

    public void setDigitPasswordSwitchState(boolean digitPasswordSwitchState) {
        this.digitPasswordSwitchState = digitPasswordSwitchState;
    }

    public boolean isCarTypeSetted() {
        return carTypeSetted;
    }

    public void setCarTypeSetted(boolean carTypeSetted) {
        this.carTypeSetted = carTypeSetted;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    @Override
    public String toString() {
        return "UserMessage{" +
                "obeId=" + obeId +
                ", gesturePassword='" + gesturePassword + '\'' +
                ", gesturePasswordSwitchState=" + gesturePasswordSwitchState +
                ", digitPassword='" + digitPassword + '\'' +
                ", digitPasswordSwitchState=" + digitPasswordSwitchState +
                ", carTypeName='" + carTypeName + '\'' +
                ", carType=" + carType +
                ", audit_state=" + audit_state +
                ", carTypeSetted=" + carTypeSetted +
                ", vehicleModel='" + vehicleModel + '\'' +
                '}';
    }
}
