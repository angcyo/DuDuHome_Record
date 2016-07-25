package com.dudu.event;

/**
 * Created by Administrator on 2016/2/15.
 */
public class ChooseEvent {


    private ChooseType chooseType;

    private int position;


    public enum ChooseType {

        STRATEGY_NUMBER,

        CHOOSE_PAGE,

        CHOOSE_NUMBER,

        NEXT_PAGE,

        PREVIOUS_PAGE,

        LAST_PAGE,

        LAST_ONE,

        PLAY_VIDEO

    }

    public ChooseType getChooseType() {
        return chooseType;
    }

    public int getPosition() {
        return position;
    }


    public ChooseEvent(ChooseType chooseType, int position) {
        this.position = position;
        this.chooseType = chooseType;
    }
}
