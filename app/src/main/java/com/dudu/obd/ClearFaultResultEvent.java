package com.dudu.obd;

/**
 * Created by Administrator on 2016/3/11.
 */
public class ClearFaultResultEvent {
    public static final int START_CLEAR = 5;
    public static final int CLEAR_OK = 1;
    public static final int CLEAR_ERROR = 2;
    public static final int CLEAR_NOT_SUPPORT = 3;
    public static final int CLEAR_HAS_CODES = 4;

    private int result;

    public ClearFaultResultEvent(int result) {
        this.result = result;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
