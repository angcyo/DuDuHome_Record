package com.dudu.obd;

import com.dudu.workflow.obd.CarCheckType;

/**
 * Created by Administrator on 2016/3/12.
 */
public class FaultCodesEvent {

    public static final int CHECK_CODES_START = 1;
    public static final int CHECK_CODES_STOP = 2;
    public static final int CHECK_CODES_RESULT_HAS_CODES = 3;
    public static final int CHECK_CODES_RESULT_NOT_SUPPORT = 4;
    public static final int CHECK_CODES_RESULT_ERROR = 5;
    public static final int CHECK_CODES_RESULT_NO_CODES = 6;

    private CarCheckType carCheckType;

    private int startOrStop;

    private int checkFaultCodeResult;

    public FaultCodesEvent(CarCheckType carCheckType, int startOrStop, int checkFaultCodeResult) {
        this.carCheckType = carCheckType;
        this.startOrStop = startOrStop;
        this.checkFaultCodeResult = checkFaultCodeResult;
    }

    public CarCheckType getCarCheckType() {
        return carCheckType;
    }

    public void setCarCheckType(CarCheckType carCheckType) {
        this.carCheckType = carCheckType;
    }

    public int getStartOrStop() {
        return startOrStop;
    }

    public void setStartOrStop(int startOrStop) {
        this.startOrStop = startOrStop;
    }

    public int getCheckFaultCodeResult() {
        return checkFaultCodeResult;
    }

    public void setCheckFaultCodeResult(int checkFaultCodeResult) {
        this.checkFaultCodeResult = checkFaultCodeResult;
    }
}
