package com.dudu.workflow.obd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class CarCheckFlow {

    private static Logger logger = LoggerFactory.getLogger("car.CarCheckFlow");

    public static void startCarCheck(CarCheckType type) throws IOException {
        String order = "AT400";
        switch (type) {
            case ECM:
                order = "AT410";
                break;
            case TCM:
                order = "AT420";
                break;
            case ABS:
                order = "AT430";
                break;
            case SRS:
                order = "AT440";
                break;
        }
        OBDStream.getInstance().exec(order);
    }

    public static void clearCarCheckError(CarCheckType type) throws IOException {
        String order = "AT401";
        switch (type) {
            case ECM:
                order = "AT411";
                break;
            case TCM:
                order = "AT421";
                break;
            case ABS:
                order = "AT431";
                break;
            case SRS:
                order = "AT441";
                break;
        }
        OBDStream.getInstance().exec(order);

    }

    public static Observable<String> filterErrorString(Observable<String> input, CarCheckType type) {
        return input
                .skip(20, TimeUnit.MICROSECONDS)
                .filter(msg -> {
                    String order = "$400";
                    switch (type) {
                        case ECM:
                            order = "$410";
                            break;
                        case TCM:
                            order = "$420";
                            break;
                        case ABS:
                            order = "$430";
                            break;
                        case SRS:
                            order = "$440";
                            break;
                    }
                    return msg.startsWith(order);
                });
    }

    public static Observable<String> filterObdClearFault(Observable<String> input, CarCheckType type) {
        return input
                .skip(20, TimeUnit.MICROSECONDS)
                .filter(s -> {
                    String order = "$401";
                    switch (type) {
                        case ECM:
                            order = "$411";
                            break;
                        case TCM:
                            order = "$421";
                            break;
                        case ABS:
                            order = "$431";
                            break;
                        case SRS:
                            order = "$441";
                            break;
                    }
                    return s.startsWith(order);
                });
    }

    public Observable<String> engineFailed() throws IOException {
        return filterErrorString(OBDStream.getInstance().obdErrorString(), CarCheckType.ECM);
    }

    public Observable<String> gearboxFailed() throws IOException {
        return filterErrorString(OBDStream.getInstance().obdErrorString(), CarCheckType.TCM);
    }

    public Observable<String> ABSFailed() throws IOException {
        return filterErrorString(OBDStream.getInstance().obdErrorString(), CarCheckType.ABS);
    }

    public Observable<String> SRSFailed() throws IOException {
        return filterErrorString(OBDStream.getInstance().obdErrorString(), CarCheckType.SRS);
    }

    private static BehaviorSubject<String> behaviorSubject = BehaviorSubject.create();

    public static void post(String string) {
        behaviorSubject.onNext(string);
    }

    public Observable<String> engineFaultClear() throws IOException {
        return filterObdClearFault(OBDStream.getInstance().obdErrorString(), CarCheckType.ECM);
    }

    public Observable<String> gearboxFaultClear() throws IOException {
        return filterObdClearFault(OBDStream.getInstance().obdErrorString(), CarCheckType.TCM);
    }

    public Observable<String> ABSFaultClear() throws IOException {
        return filterObdClearFault(OBDStream.getInstance().obdErrorString(), CarCheckType.ABS);
    }

    public Observable<String> SRSFaultClear() throws IOException {
        return filterObdClearFault(OBDStream.getInstance().obdErrorString(), CarCheckType.SRS);
    }

    public Observable<String> ALLFaultClear() throws IOException {
        return filterObdClearFault(OBDStream.getInstance().obdErrorString(), CarCheckType.ALL);
    }

    public Observable<String> getFaultClear(CarCheckType carCheckType) throws IOException {
        return filterObdClearFault(OBDStream.getInstance().obdErrorString(), carCheckType);
    }

    public Observable<String> WSBFailed() throws IOException {
        return OBDStream.getInstance().obdErrorString()
                .filter(s -> s.contains("xxx"));
    }
}
