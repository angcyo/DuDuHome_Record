package com.dudu.workflow.userMessage;

import com.dudu.persistence.UserMessage.RealUserMessageDataService;
import com.dudu.persistence.UserMessage.UserMessage;
import com.dudu.rest.model.driving.response.GetCarBrandResponse;
import com.dudu.workflow.common.DataFlowFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by luo zha  on 2016/3/22.
 */
public class UserMessageFlow {

    private static final String TAG = "UserMessageFlow";

    private RealUserMessageDataService service;

    private Logger logger = LoggerFactory.getLogger(TAG);

    public UserMessageFlow(RealUserMessageDataService service) {
        this.service = service;
    }

    public void saveUserMessage(UserMessage userMessage) {
        service.saveUserMessage(userMessage).subscribe(new Action1<UserMessage>() {
            @Override
            public void call(UserMessage userMessage) {
                logger.debug(userMessage.toString() + "--保存用户的信息成功");
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.debug(userMessage.toString() + "--保存用户的信息失败");
            }
        });
    }

    public Observable<UserMessage> obtainUserMessage() {
        logger.debug("---开始用户信息");
        return service.findUserMessage();
    }

    public Observable<Boolean> auditHasPassed() {
        return DataFlowFactory.getUserMessageFlow().obtainUserMessage()
                .map(userMessage -> userMessage.getAudit_state())
                .map(auditState -> auditState == 2);
    }

    public void saveGesturePassword(String gesturePassword) {
        logger.debug("开始保存手势密码：：" + gesturePassword);
        obtainUserMessage().subscribe(userMessage -> {
            userMessage.setGesturePassword(gesturePassword);
            saveUserMessage(userMessage);
        }, throwable -> logger.error("saveGesturePassword", throwable));
    }

    public void saveGesturePasswordSwitchState(boolean isOpen) {
        logger.debug("开始保存手势密码的开关状态：" + isOpen);
        obtainUserMessage().subscribe(userMessage -> {
            userMessage.setGesturePasswordSwitchState(isOpen);
            saveUserMessage(userMessage);
        }, throwable -> logger.error("saveGesturePasswordSwitchState", throwable));
    }

    public void saveDigitPassword(String digitPassword) {
        logger.debug("开始保存数字密码：：" + digitPassword);
        obtainUserMessage().subscribe(userMessage -> {
            userMessage.setDigitPassword(digitPassword);
            saveUserMessage(userMessage);
        }, throwable -> logger.error("saveDigitPassword", throwable));
    }

    public void saveDigitPasswordSwitchState(boolean isOpen) {
        logger.debug("开始保存数字密码的开关状态：" + isOpen);
        obtainUserMessage().subscribe(userMessage -> {
            userMessage.setDigitPasswordSwitchState(isOpen);
            saveUserMessage(userMessage);
        }, throwable -> logger.error("saveDigitPasswordSwitchState", throwable));
    }


    public void saveGuardStatus(String gesturePassword, boolean gesturePasswordSwitchState, String digitPassword, boolean digitPasswordSwitchState, long auditState) {
        logger.debug("开始保存用户的信息：：" + gesturePassword);
        obtainUserMessage().subscribe(userMessage -> {
            logger.debug("获取数据库用户的信息：" + userMessage.toString());
            userMessage.setGesturePassword(gesturePassword);
            userMessage.setGesturePasswordSwitchState(gesturePasswordSwitchState);
            userMessage.setDigitPassword(digitPassword);
            userMessage.setDigitPasswordSwitchState(digitPasswordSwitchState);
            userMessage.setAudit_state(auditState);
            saveUserMessage(userMessage);
        }, throwable -> logger.error("saveGuardStatus", throwable));
    }

    public void saveCarType(GetCarBrandResponse.GetCarBrandResult getCarBrandResult) {
        logger.debug("开始保存车型：：" + getCarBrandResult);
        obtainUserMessage()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userMessage -> {
                    userMessage.setCarTypeName(getCarBrandResult.brand);
                    userMessage.setCarType(getCarBrandResult.obd_car_no);
                    userMessage.setAudit_state(Long.valueOf(getCarBrandResult.audit_state));
                    userMessage.setVehicleModel(getCarBrandResult.cars_category);
                    userMessage.setCarTypeSetted(false);
                    saveUserMessage(userMessage);
                }, throwable -> logger.debug("saveCarType", throwable));
    }

    public void saveAuditState(long auditState) {
        logger.debug("开始保存审核状态：：" + auditState);
        obtainUserMessage()
                .subscribe(userMessage -> {
                    userMessage.setAudit_state(auditState);
                    saveUserMessage(userMessage);
                }, throwable -> logger.debug("saveCarType", throwable));
    }

}
