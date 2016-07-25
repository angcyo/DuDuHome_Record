package com.dudu.service.service;

/**
 * Created by dengjun on 2016/4/14.
 * Description :
 */
public abstract class AbstactService implements IService{
    protected boolean serviceRunFlag = false;

    protected abstract void startService();
    protected abstract void stopService();

    @Override
    public void start() {
        if (serviceRunFlag == false){
            serviceRunFlag = true;
            startService();
        }
    }

    @Override
    public void stop() {
        if (serviceRunFlag == true){
            serviceRunFlag = false;
            stopService();
        }
    }
}
