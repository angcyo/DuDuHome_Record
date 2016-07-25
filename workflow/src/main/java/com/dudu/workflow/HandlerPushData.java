package com.dudu.workflow;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.event.Events;
import com.dudu.commonlib.utils.TextVerify;
import com.dudu.drivevideo.frontcamera.FrontCameraManage;
import com.dudu.persistence.RobberyMessage.RobberyMessage;
import com.dudu.rest.model.driving.response.GetCarBrandResponse;
import com.dudu.workflow.common.DataFlowFactory;
import com.dudu.workflow.common.ObservableFactory;
import com.dudu.workflow.log.SendLogs;
import com.dudu.workflow.obd.CarLock;
import com.dudu.workflow.obd.SpeedFlow;
import com.dudu.workflow.push.model.PushParams;
import com.dudu.workflow.push.model.ReceiverPushData;
import com.dudu.workflow.upgrade.LauncherUpgrade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import rx.Subscription;

/**
 * Created by Administrator on 2016/3/30.
 */
public class HandlerPushData {

    private static HandlerPushData mInstance;
    private Logger logger = LoggerFactory.getLogger("workFlow.webSocket.HandlerPushData");
    private Subscription speedSubscription;

    public static HandlerPushData getInstance() {
        if (mInstance == null) {
            mInstance = new HandlerPushData();
        }
        return mInstance;
    }

    public void handlerData(ReceiverPushData data) {
        logger.debug("推送的数据：" + data.toString());
        if (data != null && data.result != null) {
            String method = data.result.method;
            logger.debug("推送的数据：method:" + method);
            if (data.resultCode == 0 && method != null) {
                switch (method) {
                    case PushParams.ROBBERY_STATE:
                        String thiefSwitchState = data.result.robberySwitchs;
                        String operationNumber = data.result.numberOfOperations;
                        String completeTime = data.result.completeTime;
                        String rotatingSpeed = data.result.revolutions;
                        logger.debug("推送的数据：防劫的状态:" + thiefSwitchState);
                        if (!TextVerify.isEmpty(thiefSwitchState) && !TextVerify.isEmpty(operationNumber) && !TextVerify.isEmpty(completeTime) && !TextVerify.isEmpty(rotatingSpeed)) {
                            RobberyMessage robberyMessage = new RobberyMessage();
                            robberyMessage.setObied(CommonLib.getInstance().getObeId());
                            robberyMessage.setRobberySwitch(thiefSwitchState.endsWith("1") ? true : false);
                            robberyMessage.setOperationNumber(operationNumber);
                            robberyMessage.setRotatingSpeed(rotatingSpeed);
                            robberyMessage.setCompleteTime(completeTime);
                            DataFlowFactory.getRobberyMessageFlow().changeRobberyMessage(robberyMessage);
                        }
                        break;
                    case PushParams.TEST_SPEED_START:
                        try {
                            logger.debug("订阅获取当前车速的事件");
                            ObservableFactory.stopAccelerationTestFlow();
                            if (speedSubscription!=null&&speedSubscription.isUnsubscribed()){
                                speedSubscription.unsubscribe();
                            }
                            speedSubscription = SpeedFlow.carSpeed()
                                    .subscribe(speed -> {
                                        logger.debug("取消订阅获取当前车速的事件");
                                        speedSubscription.unsubscribe();
                                        if (speed > 0) {
                                            logger.debug("检测到车速大于0，提示用户先停车然后在进行加速测试");
                                            EventBus.getDefault().post(new Events.TestSpeedEvent(Events.TEST_SPEED_ZERO));
                                        } else {
                                            try {
                                                logger.debug("开始加速测试，跳转加速测试的界面");
                                                EventBus.getDefault().post(new Events.TestSpeedEvent(Events.TEST_SPEED_START));
                                                ObservableFactory.testAccSpeedFlow(data);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, throwable -> {
                                        speedSubscription.unsubscribe();
                                        logger.debug("订阅获取当前车速的事件失败", throwable);
                                    });
                        } catch (IOException e) {
                            logger.debug("获取当前速度方法错误:" + e.getMessage());
                        }
                        break;
                    case PushParams.GUARD_STATE:
                        String guardSwitchState = data.result.thiefSwitchState;
                        logger.debug("推送的数据：防盗的状态:" + guardSwitchState);
                        if (guardSwitchState != null) {
                            DataFlowFactory.getSwitchDataFlow().saveGuardSwitch(guardSwitchState.equals("1") ? true : false);
                            EventBus.getDefault().post(new Events.GuardSwitchState(guardSwitchState.equals("1") ? true : false));
                            checkLockStatus("1".equals(guardSwitchState));
                        }
                        break;
                    case PushParams.GUARD_SET_PASSWORD:
                        String gesturePassword = data.result.protectThiefSignalPassword;
                        String gesturePasswordSwitchState = data.result.protectThiefSignalState;
                        String digitPassword = data.result.protectThiefPassword;
                        String digitPasswordSwitchState = data.result.protectThiefState;
                        logger.debug("推送的数据：手势密码:" + gesturePassword);
                        logger.debug("推送的数据：手势密码的开关状态:" + gesturePasswordSwitchState);
                        logger.debug("推送的数据：数字密码:" + digitPassword);
                        logger.debug("推送的数据：数字密码的开关状态:" + digitPasswordSwitchState);
                        if (!TextVerify.isEmpty(gesturePassword)) {
                            DataFlowFactory.getUserMessageFlow().saveGesturePassword(gesturePassword);
                        }
                        if (!TextVerify.isEmpty(gesturePasswordSwitchState)) {
                            boolean isOpen = gesturePasswordSwitchState.trim().equals("0") ? false : true;
                            DataFlowFactory.getUserMessageFlow().saveGesturePasswordSwitchState(isOpen);
                        }
                        if (!TextVerify.isEmpty(digitPassword)) {
                            DataFlowFactory.getUserMessageFlow().saveDigitPassword(digitPassword);
                        }
                        if (!TextVerify.isEmpty(digitPasswordSwitchState)) {
                            boolean isOpen = digitPasswordSwitchState.trim().equals("0") ? false : true;
                            DataFlowFactory.getUserMessageFlow().saveDigitPasswordSwitchState(isOpen);
                        }
                        break;
                    case PushParams.THEFT_APPROVAL:
                        GetCarBrandResponse.GetCarBrandResult getCarBrandResult = new GetCarBrandResponse.GetCarBrandResult();
                        getCarBrandResult.brand = data.result.brand;
                        getCarBrandResult.audit_state = String.valueOf(data.result.audit_state);
                        getCarBrandResult.obd_car_no = data.result.obd_car_no;
                        getCarBrandResult.model = data.result.model;
                        getCarBrandResult.cars_category = data.result.cars_category;
                        DataFlowFactory.getUserMessageFlow()
                                .saveCarType(getCarBrandResult);
                        break;
                    case PushParams.LOG_UPLOAD:
                        logger.info("日志上传地址：{}", data.result.logUploadUrl);
                        new SendLogs().uploadLog(data.result.logUploadUrl);
                        break;
                    case PushParams.LAUNCHER_UPGRADE:
                        logger.info("收到推送升级消息");
                        LauncherUpgrade.queryVersionInfo();
                        break;
                    case PushParams.START_STREAM:
//                        EventBus.getDefault().post(new EventStartStream());
                        FrontCameraManage.getInstance().startUploadVideoStream();
                        break;
                    case PushParams.STOP_STREAM:
//                        EventBus.getDefault().post(new EventStopStream());
                        FrontCameraManage.getInstance().stopUploadVideoStream();
                        break;
                }
            }
        }
    }

    private void checkLockStatus(boolean isOpen) {
        if (isOpen) {
            logger.debug("给车上锁");
            CarLock.lockCar();
        } else {
            logger.debug("给车解锁");
            CarLock.unlockCar();
        }
    }
}
