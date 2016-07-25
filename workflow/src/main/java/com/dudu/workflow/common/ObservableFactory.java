package com.dudu.workflow.common;

import com.dudu.persistence.driving.FaultCode;
import com.dudu.workflow.driving.DrivingFlow;
import com.dudu.workflow.obd.CarCheckFlow;
import com.dudu.workflow.push.model.ReceiverPushData;
import com.dudu.workflow.robbery.RobberyFlow;

import java.io.IOException;

import rx.Observable;

/**
 * Created by Administrator on 2016/2/19.
 */
public class ObservableFactory {
    private static RobberyFlow robberyFlow = new RobberyFlow();

    private static DrivingFlow drivingFlow = new DrivingFlow();
    private static CarCheckFlow carCheckFlow = new CarCheckFlow();

    public static void init() {
    }

    public static void testAccSpeedFlow(ReceiverPushData receiverPushData) throws IOException {
        drivingFlow.testAccSpeedFlow(Observable.just(receiverPushData));
    }

    public static void stopAccelerationTestFlow() {
        drivingFlow.stopAccelerationTestFlow();
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
    public static Observable<Boolean> accelerometersMonitoring(int revolutions, int numberOfOperations, int completeTime) throws IOException {
        return robberyFlow.gunToggle(revolutions, numberOfOperations, completeTime);
    }

    public static CarCheckFlow getCarCheckFlow() {
        return carCheckFlow;
    }

    public static Observable<String> engineFailed() throws IOException {
        return carCheckFlow.engineFailed()
                .map(codes -> {
                    if (codes.contains(",")) {
                        String[] contents = codes.split(",");
                        if (contents.length > 0) {
                            DataFlowFactory.getDrivingFlow().saveFaultCodes(FaultCode.ECM, contents[1]);
                        }
                    }
                    return codes;
                });
    }

    public static Observable<String> gearboxFailed() throws IOException {
        return carCheckFlow.gearboxFailed()
                .map(codes -> {
                    if (codes.contains(",")) {
                        String[] contents = codes.split(",");
                        if (contents.length > 0) {
                            DataFlowFactory.getDrivingFlow().saveFaultCodes(FaultCode.TCM, contents[1]);
                        }
                    }
                    return codes;
                });
    }

    public static Observable<String> ABSFailed() throws IOException {
        return carCheckFlow.ABSFailed()
                .map(codes -> {
                    if (codes.contains(",")) {
                        String[] contents = codes.split(",");
                        if (contents.length > 0) {
                            DataFlowFactory.getDrivingFlow().saveFaultCodes(FaultCode.ABS, contents[1]);
                        }
                    }
                    return codes;
                });
    }

    public static Observable<String> SRSFailed() throws IOException {
        return carCheckFlow.SRSFailed()
                .map(codes -> {
                    if (codes.contains(",")) {
                        String[] contents = codes.split(",");
                        if (contents.length > 0) {
                            DataFlowFactory.getDrivingFlow().saveFaultCodes(FaultCode.SRS, contents[1]);
                        }
                    }
                    return codes;
                });
    }

    public static Observable<String> WSBFailed() throws IOException {
        return carCheckFlow.WSBFailed()
                .map(codes -> {
                    if (codes.contains(",")) {
                        String[] contents = codes.split(",");
                        if (contents.length > 0) {
                            DataFlowFactory.getDrivingFlow().saveFaultCodes(FaultCode.WSB, codes);
                        }
                    }
                    return codes;
                });
    }

    public static DrivingFlow getDrivingFlow() {
        return drivingFlow;
    }

}
