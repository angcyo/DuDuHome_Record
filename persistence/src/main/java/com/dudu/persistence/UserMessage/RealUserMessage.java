package com.dudu.persistence.UserMessage;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by luo zha on 2016/3/22.
 */
public class RealUserMessage extends RealmObject {

    @PrimaryKey
    private long obeId;

    private String gesturePassword;

    private boolean gesturePasswordSwitchState;

    private String digitPassword;

    private boolean digitPasswordSwitchState;

    private String carTypeName;

    private long carType;

    private long auditState;

    private String vehicleModel;

    private boolean carTypeSetted;

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

    public long getAuditState() {
        return auditState;
    }

    public void setAuditState(long auditState) {
        this.auditState = auditState;
    }

    public boolean isDigitPasswordSwitchState() {
        return digitPasswordSwitchState;
    }

    public void setDigitPasswordSwitchState(boolean digitPasswordSwitchState) {
        this.digitPasswordSwitchState = digitPasswordSwitchState;
    }

    public boolean isGesturePasswordSwitchState() {
        return gesturePasswordSwitchState;
    }

    public void setGesturePasswordSwitchState(boolean gesturePasswordSwitchState) {
        this.gesturePasswordSwitchState = gesturePasswordSwitchState;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public boolean isCarTypeSetted() {
        return carTypeSetted;
    }

    public void setCarTypeSetted(boolean carTypeSetted) {
        this.carTypeSetted = carTypeSetted;
    }
}
