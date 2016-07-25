package com.dudu.resource.resource;

/**
 * Created by dengjun on 2016/3/31.
 * Description :
 */
public enum ResourceState {
    UnInit(0),
    Initing(1),
    Inited(2);


    int state = 0;

     ResourceState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
