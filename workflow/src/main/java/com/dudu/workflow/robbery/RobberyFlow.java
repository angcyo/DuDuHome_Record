package com.dudu.workflow.robbery;

import com.dudu.workflow.obd.OBDStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * Created by Administrator on 2016/2/19.
 */
public class RobberyFlow {

    private Logger logger = LoggerFactory.getLogger("workFlow.Robbery");

    private static RobberyFlow mInstance = new RobberyFlow();

    public static RobberyFlow getInstance() {
        return mInstance;
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
    public Observable<Boolean> gunToggle(int revolutions, int numberOfOperations, int completeTime) throws IOException {
        return OBDStream.getInstance().engSpeedStream()
                .map(aDouble -> aDouble > revolutions)
                .distinctUntilChanged()
                .doOnNext(aBoolean1 -> logger.debug("obd.gun3Toggle:" + aBoolean1))
                .filter(aBoolean -> aBoolean)
                .take(numberOfOperations)
                .timeout(completeTime, TimeUnit.SECONDS);
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
    public Observable<Double> accelerometersMonitoring6(int revolutions, int numberOfOperations, int completeTime) throws IOException {
        return OBDStream.getInstance().engSpeedStream()
                .scan((lastSpeed, currentSpeed) -> {
                    logger.debug("accelerometersMonitoring" + lastSpeed + " " + currentSpeed);
                    return (currentSpeed - lastSpeed) > revolutions ? 1.0 : 0.0;
                })
                .map(aDouble -> aDouble > 0)
                .doOnNext(aBoolean1 -> logger.debug("obd.gun3Toggle:" + aBoolean1))
                .filter(hasAcced -> hasAcced)
                .zipWith(OBDStream.getInstance().engSpeedStream(), (hasAcced1, speed) -> speed)
                .take(numberOfOperations)
                .timeout(completeTime, TimeUnit.SECONDS);
    }

    private boolean hasAcc = false;

    public Observable<Double> accelerometersMonitoring(int revolutions, int numberOfOperations, int completeTime) throws IOException {
        return OBDStream.getInstance().engSpeedStream()
                .scan((lastSpeed, currentSpeed) -> {
                    logger.debug("accelerometersMonitoring" + lastSpeed + " " + currentSpeed);
                    hasAcc = (currentSpeed - lastSpeed) >= revolutions;
                    return currentSpeed;
                })
                .doOnNext(aBoolean1 -> logger.debug("obd.gun3Toggle:" + hasAcc))
                .filter(hasAcced -> hasAcc)
                .take(numberOfOperations)
                .timeout(completeTime, TimeUnit.SECONDS);
    }
}
