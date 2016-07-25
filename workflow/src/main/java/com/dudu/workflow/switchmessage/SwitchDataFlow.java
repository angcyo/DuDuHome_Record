package com.dudu.workflow.switchmessage;

import com.dudu.persistence.switchmessage.SwitchMessage;
import com.dudu.persistence.switchmessage.SwitchMessageService;
import com.dudu.workflow.common.CommonParams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.functions.Func3;
import rx.functions.Func4;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/2/20.
 */
public class SwitchDataFlow {

    private static final String TAG = "SwitchDataFlow";

    private Logger logger = LoggerFactory.getLogger(TAG);

    private SwitchMessageService switchMessageService;

    public SwitchDataFlow(SwitchMessageService switchMessageService) {
        this.switchMessageService = switchMessageService;
    }

    public void init() {

    }

    public void saveGuardSwitch(boolean opened) {
        logger.debug("保存防盗开关状态为" + (opened ? "开启" : "关闭"));
        saveSwitchState(SwitchMessage.GUARD_SWITCH_KEY, opened);
    }

    public Observable<Boolean> getGuardSwitch() {
        return getSwitchState(SwitchMessage.GUARD_SWITCH_KEY);
    }

    private void saveSwitchState(String key, boolean opened) {
        SwitchMessage switchMessage = new SwitchMessage(key, opened);
        switchMessageService.saveSwitch(switchMessage)
                .subscribeOn(Schedulers.newThread())
                .subscribe(message -> {
                    logger.debug(message.getSwitchKey() + "保存为" + message.isSwitchOpened() + "成功");
                }, (error) -> {
                    logger.error("saveSwitchState", error);
                });
    }

    private Observable<Boolean> getSwitchState(String key) {
        return switchMessageService.findSwitch(key)
                .map(message -> {
                    logger.debug(message.getSwitchKey() + "的值为：" + message.isSwitchOpened());
                    return message.isSwitchOpened();
                });
    }
}
