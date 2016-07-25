package com.dudu.workflow.obd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * Created by Administrator on 2016/3/10.
 */
public class SpeedFlow {

    private static Logger logger = LoggerFactory.getLogger("workFlow.Robbery");

    public static Observable<Boolean> carIsStoped() throws IOException {
        return OBDStream.getInstance().speedStream()
                .sample(1, TimeUnit.SECONDS)
                .first()
                .doOnNext(inputString -> logger.debug("obdString:" + inputString))
                .map(speed -> !(speed > 0));
    }

    public static Observable<Double> testCarSpeed() throws IOException {
        return OBDStream.getInstance().testSpeedStream();
    }

    public static Observable<Double> carSpeed() throws IOException {
        return OBDStream.getInstance().speedStream()
                .sample(1, TimeUnit.SECONDS)
                .first();
    }

}
