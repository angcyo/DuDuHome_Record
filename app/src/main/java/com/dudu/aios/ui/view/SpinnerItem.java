package com.dudu.aios.ui.view;

/**
 * Created by Administrator on 2016/4/28.
 */
public class SpinnerItem {
    private int ID = 0;
    private String Value = "ALL";

    public SpinnerItem () {
        ID = 0;
        Value = "ALL";
    }

    public SpinnerItem (int _ID, String _Value) {
        ID = _ID;
        Value = _Value;
    }

    @Override
    public String toString() {
        return Value;
    }

    public int getID() {
        return ID;
    }

    public String getValue() {
        return Value;
    }
}
