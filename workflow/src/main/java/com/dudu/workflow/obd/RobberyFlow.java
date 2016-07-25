package com.dudu.workflow.obd;

import com.dudu.commonlib.event.Events;
import com.dudu.commonlib.utils.TextVerify;
import com.dudu.workflow.common.DataFlowFactory;
import com.dudu.workflow.common.ObservableFactory;
import com.dudu.workflow.robbery.RobberyStateModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import de.greenrobot.event.EventBus;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/4/23.
 */
public class RobberyFlow {

    private static Logger logger = LoggerFactory.getLogger("workFlow.Robbery");

    public static void checkGunSwitch() {
        logger.debug("checkGunSwitch");
        if (DataFlowFactory.getRobberyMessageFlow() != null) {
            DataFlowFactory.getRobberyMessageFlow().obtainRobberyMessage()
                    .filter(robberyMessage1 -> !TextVerify.isEmpty(robberyMessage1.getRotatingSpeed()) && Integer.valueOf(robberyMessage1.getRotatingSpeed()) > 0)
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(robberyMessage ->
                                    EventBus.getDefault().post(new Events.RobberyEvent(Integer.valueOf(robberyMessage.getRotatingSpeed()),
                                            Integer.valueOf(robberyMessage.getOperationNumber()),
                                            Integer.valueOf(robberyMessage.getCompleteTime())
                                    ))
                            , throwable -> logger.error("obtainRobberyMessage", throwable)
                    );
        }
    }

    /**
     * 防劫踩油门逻辑，在completeTime的时间内踩油门numberOfOperations次，每次转速超过revolutions，则触发防劫
     *
     * @param revolutions        限制的转速
     * @param numberOfOperations 踩油门次数
     * @param completeTime       限定的时间
     * @return
     * @throws IOException
     */
    public static Subscription checkGunSwitch(int revolutions, int numberOfOperations, int completeTime) throws IOException {
        return ObservableFactory.accelerometersMonitoring(revolutions, numberOfOperations, completeTime)
                .doOnNext(aBoolean1 -> logger.debug("obd.checkGunSwitch.gun3Toggle:" + aBoolean1))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                            logger.debug("obd.checkGunSwitch.gun3Toggle.subscribe:" + aBoolean);
                        }
                        , throwable -> {
                            if ((throwable instanceof TimeoutException)) {
                                logger.debug("obd.checkGunSwitch.Gun toggle fail, try again");
                                EventBus.getDefault().post(new Events.RobberyEvent(revolutions, numberOfOperations, completeTime));
                            } else {
                                logger.debug("obd.checkGunSwitch.Gun toggle fail", throwable);
                            }
                        }
                        , () -> {
                            logger.debug("obd.checkGunSwitch.Gun toggle robbery, sync to app");
                            EventBus.getDefault().post(new RobberyStateModel(true));
                            EventBus.getDefault().post(new Events.RobberyEvent(revolutions, numberOfOperations, completeTime));
                        });
    }
}
