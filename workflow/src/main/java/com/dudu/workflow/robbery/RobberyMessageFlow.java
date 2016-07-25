package com.dudu.workflow.robbery;

import com.dudu.commonlib.event.Events;
import com.dudu.persistence.RobberyMessage.RealRobberyMessageDataService;
import com.dudu.persistence.RobberyMessage.RobberyMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.functions.Action1;

/**
 * Created by Administrator on 2016/4/21.
 */
public class RobberyMessageFlow {

    private static final String TAG = "RobberyMessageFlow";

    private RealRobberyMessageDataService service;

    private Logger logger = LoggerFactory.getLogger(TAG);

    public RobberyMessageFlow(RealRobberyMessageDataService service) {
        this.service = service;
    }


    public void saveRobberyMessage(RobberyMessage robberyMessage) {
        service.saveRobberyMessage(robberyMessage).subscribe(new Action1<RobberyMessage>() {
            @Override
            public void call(RobberyMessage robberyMessage) {
                logger.debug("保存防劫的信息--成功:" + robberyMessage.toString() + "");
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.debug("保存防劫的信息--失败:" + robberyMessage.toString() + "");
            }
        });
    }

    public void changeRobberyMessage(RobberyMessage robberyMessage) {
        saveRobberyMessage(robberyMessage);
        EventBus.getDefault().post(new Events.RobberyEvent(Integer.valueOf(robberyMessage.getRotatingSpeed()), Integer.valueOf(robberyMessage.getOperationNumber()), Integer.valueOf(robberyMessage.getCompleteTime())));
    }

    public Observable<RobberyMessage> obtainRobberyMessage() {
        return service.findRobberyMessage();
    }

    public void saveRobberySwitch(boolean isOpen) {
        obtainRobberyMessage().subscribe(robberyMessage -> {
            logger.debug("开始保存防劫的开关：" + isOpen);
            robberyMessage.setRobberySwitch(isOpen);
            changeRobberyMessage(robberyMessage);
        }, throwable -> logger.error("saveRobberySwitch", throwable));
    }
}
