package com.dudu.workflow.driving;

import android.util.Log;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.event.Events;
import com.dudu.commonlib.utils.File.KeyConstants;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;
import com.dudu.commonlib.utils.afinal.async.Arrays;
import com.dudu.persistence.driving.FaultCode;
import com.dudu.persistence.driving.FaultCodeService;
import com.dudu.rest.model.driving.response.FaultCodeDetailMessage;
import com.dudu.workflow.common.RequestFactory;
import com.dudu.workflow.obd.CarCheckType;
import com.dudu.workflow.obd.OBDStream;
import com.dudu.workflow.push.model.ReceiverPushData;
import com.dudu.workflow.switchmessage.AccTestData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func5;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/2/17.
 */
public class DrivingFlow {

    private static Logger log = LoggerFactory.getLogger("car.DrivingFlow");

    private FaultCodeService faultCodeService;

    private Subscription accVoltageTimer;

    private Subscription stopAccelerationTest;

    public void testAccSpeedFlow(Observable<ReceiverPushData> receiverDataObservable) throws IOException {
        Observable<String> typeObservable = receiverDataObservable
                .map(receiverPushData -> receiverPushData.result.type)
                .doOnNext(s -> {
                    try {
                        OBDStream.getInstance().exec("ATTSPMON");
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                });

        Observable<String> speedTimeObservable = typeObservable
                .flatMap(max_speed -> {
                    try {
                        return OBDStream.getInstance().testSpeedStream()
                                .takeUntil(aDouble -> {
                                    boolean overSpeed = aDouble > Integer.parseInt(max_speed) * 100;
                                    if (overSpeed) {
                                        SharedPreferencesUtil.putStringValue(CommonLib.getInstance().getContext(), SharedPreferencesUtil.MAX_SPEED, String.valueOf(aDouble));
                                    }
                                    return overSpeed;
                                })
                                .count()
                                .map(integer -> integer * 0.2)
                                .doOnNext(aDouble -> {
                                    log.debug("加速最大的速度:" + max_speed + " aDouble:" + aDouble + " max:" + max_speed + "  ..");
                                    stopAccelerationTest();
                                });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                })
                .map(Object::toString);
        stopAccelerationTest = Observable.combineLatest(receiverDataObservable, typeObservable, speedTimeObservable, OBDStream.getInstance().speedStream()
                , (receiverData, type, speed_time, speed) -> new AccTestData(type, speed_time, receiverData.result.testFeedId, receiverData.result.phone, String.valueOf(speed)))
                .doOnNext(accTestData -> Log.d("test speed result", accTestData.toString()))
                .subscribe(data -> {
                    Events.TestSpeedEvent event = new Events.TestSpeedEvent(Events.TEST_SPEED_STOP);
                    event.setSpeed(data.getSpeed());
                    event.setSpeedTotalTime(data.getAccTotalTime());
                    EventBus.getDefault().post(event);
                    log.debug("加速完成后的数据：" + data.getAccTotalTime() + "speed" + data.getSpeed());
                    stopAccelerationTest.unsubscribe();
                    RequestFactory.getDrivingRequest()
                            .pushAcceleratedTestData(new DecimalFormat("######0.00").format(Double.parseDouble(data.getAccTotalTime())), data.getTestFeedId(), data.getPhone())
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(requestResponse -> log.debug("requestResponse.resultCode:" + (requestResponse.resultCode))
                                    , throwable -> log.error("getDrivingRequest", throwable));
                }, throwable -> log.error("testAccSpeedFlow", throwable));
    }

    public void stopAccelerationTestFlow(){
        stopAccelerationTest();
        if(stopAccelerationTest!=null){
            stopAccelerationTest.unsubscribe();
        }
    }

    private void stopAccelerationTest(){
        try {
            OBDStream.getInstance().exec("ATTSPMOFF");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveFaultCodes(int type, String codes) {
        if (faultCodeService != null) {
            faultCodeService
                    .saveSwitch(type, codes)
                    .subscribe(faultCode -> {
                        log.debug(faultCode.getFaultCode() + "保存成功");
                    }, error -> {
                        log.error("saveFaultCodes", error);
                    });
        }
    }

    public Observable<String> getVoltageStream(boolean getAccVoltage) throws IOException {
        if (getAccVoltage) {
            startGetAccVoltageTimer();
            return OBDStream.getInstance().accVoltageStream();
        } else {
            return OBDStream.getInstance().batteryVoltageStream();
        }
    }

    private void startGetAccVoltageTimer() {
        log.info("interval.io.create 定时获取ACC电压");
        accVoltageTimer = Observable.interval(0, 5, TimeUnit.SECONDS, Schedulers.io())
                .subscribe((l) -> {
                    log.debug("interval.io 定时获取ACC电压");
                    onQueryACCVol();
                }, throwable -> log.error("interval.io startGetAccVoltageTimer", throwable));
    }

    public void checkShouldMonitorAccVoltage() throws IOException {
        OBDStream.getInstance().batteryVoltageStream()
                .map(batteryVoltage -> Double.valueOf(batteryVoltage))
                .filter(batteryVoltageDouble -> batteryVoltageDouble > 0)
                .observeOn(Schedulers.newThread())
                .timeout(1, TimeUnit.MINUTES)
                .subscribe(batteryVoltage1 -> {
                    boolean shouldGetAccVoltage = SharedPreferencesUtil.getBooleanValue(CommonLib.getInstance().getContext(), KeyConstants.KEY_SHOULD_GET_ACC_VOLTAGE, false);
                    if (shouldGetAccVoltage) {
                        SharedPreferencesUtil.putBooleanValue(CommonLib.getInstance().getContext(), KeyConstants.KEY_SHOULD_GET_ACC_VOLTAGE, false);
                        if (accVoltageTimer != null) {
                            accVoltageTimer.unsubscribe();
                        }
                        EventBus.getDefault().post(new Events.VoltageTypeChangeEvent(shouldGetAccVoltage));
                    }
                }, throwable -> {
                    if (throwable instanceof TimeoutException) {
                        boolean shouldGetAccVoltage = SharedPreferencesUtil.getBooleanValue(CommonLib.getInstance().getContext(), KeyConstants.KEY_SHOULD_GET_ACC_VOLTAGE, false);
                        if (!shouldGetAccVoltage) {
                            SharedPreferencesUtil.putBooleanValue(CommonLib.getInstance().getContext(), KeyConstants.KEY_SHOULD_GET_ACC_VOLTAGE, true);
                            startGetAccVoltageTimer();
                            EventBus.getDefault().post(new Events.VoltageTypeChangeEvent(shouldGetAccVoltage));
                        }
                    } else {
                        log.error("checkShouldMonitorAccVoltage");
                    }
                });
    }

    public void onQueryACCVol() {
        try {
            OBDStream.getInstance().exec("ATGETVOL");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Observable<FaultCode> getFaultCodes() {
        return Observable.merge(getFaultCodesHasCodes(CarCheckType.ECM),
                getFaultCodesHasCodes(CarCheckType.TCM),
                getFaultCodesHasCodes(CarCheckType.ABS),
                getFaultCodesHasCodes(CarCheckType.WSB),
                getFaultCodesHasCodes(CarCheckType.SRS));
    }

    public Observable<List<FaultCode>> getAllFaultCodes() {
        return Observable.zip(getFaultCodes(CarCheckType.ECM),
                getFaultCodes(CarCheckType.TCM),
                getFaultCodes(CarCheckType.ABS),
                getFaultCodes(CarCheckType.WSB),
                getFaultCodes(CarCheckType.SRS), faultCodesToList);
    }

    Func5<FaultCode, FaultCode, FaultCode, FaultCode, FaultCode, List<FaultCode>> faultCodesToList
            = (ecmFaultCode, tcmFaultCode, absFaultCode, wsbFaultCode, srsFaultCode) -> {

        List<FaultCode> faultCodeList = new ArrayList<>();

        if (ecmFaultCode != null && ecmFaultCode.getFaultCode() != null && ecmFaultCode.getFaultCode().length > 0) {
            faultCodeList.add(ecmFaultCode);
        }

        if (tcmFaultCode != null && tcmFaultCode.getFaultCode() != null && tcmFaultCode.getFaultCode().length > 0) {
            faultCodeList.add(tcmFaultCode);
        }

        if (absFaultCode != null && absFaultCode.getFaultCode() != null && absFaultCode.getFaultCode().length > 0) {
            faultCodeList.add(absFaultCode);
        }

        if (wsbFaultCode != null && wsbFaultCode.getFaultCode() != null && wsbFaultCode.getFaultCode().length > 0) {
            faultCodeList.add(wsbFaultCode);
        }

        if (srsFaultCode != null && srsFaultCode.getFaultCode() != null && srsFaultCode.getFaultCode().length > 0) {
            faultCodeList.add(srsFaultCode);
        }
        log.debug("faultCodeList size:" + faultCodeList.size());
        return faultCodeList;
    };

    public Observable<FaultCode> getFaultCodesHasCodes(CarCheckType type) {
        return getFaultCodes(type).filter(faultCode -> faultCode.getFaultCode().length > 0);
    }

    public Observable<FaultCode> getFaultCodes(CarCheckType type) {
        int typeValue = 0;
        switch (type) {
            case ECM:
                typeValue = FaultCode.ECM;
                break;
            case TCM:
                typeValue = FaultCode.TCM;
                break;
            case ABS:
                typeValue = FaultCode.ABS;
                break;
            case SRS:
                typeValue = FaultCode.SRS;
                break;
            case WSB:
                typeValue = FaultCode.WSB;
                break;
        }
        log.debug(type.name() + typeValue);
        return faultCodeService.findSwitch(typeValue);
    }

    public void getFaultCodes(CarCheckType type, Action1<FaultCode> action1) {
        getFaultCodesHasCodes(type).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(action1
                        , throwable -> log.debug("getFaultCodes", throwable)
                        , () -> log.debug("getFaultCodes onComplete"));
    }

    public void setFaultCodeService(FaultCodeService faultCodeService) {
        this.faultCodeService = faultCodeService;
    }

    public static ArrayList<FaultCodeDetailMessage> filterFaultCodeDetailMessage(FaultCodeDetailMessage[] faultCodeDetailMessages, ArrayList<FaultCodeDetailMessage> emptyFaultCodeMessageList) {
        log.debug("filterFaultCodeDetailMessage");
        ArrayList<FaultCodeDetailMessage> goalFaultCodeMessages = new ArrayList<>();
        goalFaultCodeMessages.addAll(Arrays.asList(faultCodeDetailMessages));
        for (FaultCodeDetailMessage emptyFaultCode : emptyFaultCodeMessageList) {
            boolean theFaultCodeHasDetailMessage = false;
            for (FaultCodeDetailMessage faultCodeDetailMessage : faultCodeDetailMessages) {
                if (emptyFaultCode.faultCode.trim().equals(faultCodeDetailMessage.faultCode.trim())) {
                    theFaultCodeHasDetailMessage = true;
                    continue;
                }
            }
            if (!theFaultCodeHasDetailMessage) {
                goalFaultCodeMessages.add(emptyFaultCode);
            }
        }
        log.debug("filterFaultCodeDetailMessage return emptyFaultCodeMessageList:" + emptyFaultCodeMessageList);
        log.debug("filterFaultCodeDetailMessage return goalFaultCodeMessages:" + goalFaultCodeMessages);
        return goalFaultCodeMessages;
    }

    public static List<FaultCodeDetailMessage> initEmptyFaultCodes(List<FaultCode> faultCodesList) {
        List<FaultCodeDetailMessage> mEmptyFaultCodes = new ArrayList<>();
        for (FaultCode faultCode : faultCodesList) {
            if (faultCode != null && faultCode.getFaultCode() != null && faultCode.getFaultCode().length > 0) {
                for (String faultCodeString : faultCode.getFaultCode()) {
                    mEmptyFaultCodes.add(new FaultCodeDetailMessage(faultCodeString));
                }
            }
        }
        return mEmptyFaultCodes;
    }
}
