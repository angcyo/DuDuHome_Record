package com.dudu.carChecking;

import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.Contacts;
import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;
import com.dudu.commonlib.utils.TextVerify;
import com.dudu.monitor.obd.ObdManage;
import com.dudu.obd.ClearFaultResultEvent;
import com.dudu.obd.FaultCodesEvent;
import com.dudu.obd.ShowFaultPageEvent;
import com.dudu.persistence.driving.FaultCode;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.TTSType;
import com.dudu.workflow.common.DataFlowFactory;
import com.dudu.workflow.common.ObservableFactory;
import com.dudu.workflow.common.RequestFactory;
import com.dudu.workflow.obd.CarCheckFlow;
import com.dudu.workflow.obd.CarCheckType;
import com.dudu.workflow.obd.FaultCodeFlow;
import com.dudu.workflow.obd.OBDStream;
import com.dudu.workflow.obd.SpeedFlow;
import com.dudu.workflow.obd.VehicleConstants;
import com.dudu.workflow.tpms.TirePressureData;
import com.dudu.workflow.tpms.TpmsDataCallBack;
import com.dudu.workflow.tpms.TpmsDatasFlow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.dudu.workflow.obd.CarCheckType.ABS;
import static com.dudu.workflow.obd.CarCheckType.ECM;
import static com.dudu.workflow.obd.CarCheckType.SRS;
import static com.dudu.workflow.obd.CarCheckType.TCM;
import static com.dudu.workflow.obd.CarCheckType.WSB;

/**
 * Created by lxh on 2016/2/21.
 */
public class CarCheckingProxy {

    private static CarCheckingProxy carCheckingProxy;

    private boolean isCheckingFaults = false;
    private boolean isClearingFault = false;
    private boolean isWaitingForClearingFault = false;

    private List<CarCheckType> carCheckTypeList;
    private List<CarCheckType> carCheckTypeCheckedList;
    private List<CarCheckType> clearCarCheckTypeList;

    private Logger logger;

    private Subscription clearSubscription;

    private Subscription execSubscription;
    private Subscription carIsStopSubscription;
    private Subscription setCarTypeSubscription;

    private CarCheckingProxy() {
        init();
    }

    private void init() {
        logger = LoggerFactory.getLogger("car.checking");
        carCheckTypeList = new ArrayList<>();
        carCheckTypeCheckedList = new ArrayList<>();
        clearCarCheckTypeList = new ArrayList<>();
    }

    public static CarCheckingProxy getInstance() {

        if (carCheckingProxy == null) {
            carCheckingProxy = new CarCheckingProxy();
        }
        return carCheckingProxy;
    }

    public void requestCarTypeAndStartCarchecking() {
        logger.debug("requestCarTypeAndStartCarchecking");
        RequestFactory.getDrivingRequest().getCarBrand()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(requestResponse -> {
                    logger.debug("requestCarTypeAndStartCarchecking.requestResponse " + requestResponse.resultCode);
                    if (requestResponse.resultCode == 0) {
                        SharedPreferencesUtil.putBooleanValue(CommonLib.getInstance().getContext(), Contacts.BINDING_STATE, true);
                        if (requestResponse.result != null && requestResponse.result.audit_state.equals("2")) {
                            DataFlowFactory.getUserMessageFlow().saveCarType(requestResponse.result);
                            setCarTypeAndStartCarChecking(requestResponse.result.obd_car_no);
                            return;
                        }
                    }
                    startCarCheckingFromDBCarType();
                }, throwable -> {
                    logger.debug("checkCarTypeAndStartCarChecking", throwable);
                    startCarCheckingFromDBCarType();
                });
    }

    private void startCarCheckingFromDBCarType() {
        logger.debug("startCarCheckingFromDBCarType");
        DataFlowFactory.getUserMessageFlow().obtainUserMessage()
                .map(userMessage -> userMessage.getCarType())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(carType -> {
                    logger.debug("startCarCheckingFromDBCarType+carType:" + carType);
                    setCarTypeAndStartCarChecking(carType);
                }, throwable1 -> {
                    VoiceManagerProxy.getInstance().startSpeaking(
                            CommonLib.getInstance().getContext().getString(R.string.check_faults_fail), TTSType.TTS_DO_NOTHING, false);
                    logger.error("startChecking", throwable1);
                });
    }

    private void setCarTypeAndStartCarChecking(long carType) {
        logger.debug("setCarTypeAndStartCarChecking:" + carType);
        try {
            setCarTypeSubscription = OBDStream.getInstance().OBDSetCarType()
                    .timeout(5, TimeUnit.SECONDS)
                    .subscribe(
                            result -> {
                                logger.debug("setCarTypeAndStartCarChecking.result:" + result);
                                if (setCarTypeSubscription != null) {
                                    setCarTypeSubscription.unsubscribe();
                                }
                                if (carType == VehicleConstants.CAR_TYPE_BMW) {

                                }
                                startCarCheckingIfCarStop();
                            },
                            throwable -> {
                                if (setCarTypeSubscription != null) {
                                    setCarTypeSubscription.unsubscribe();
                                }
                                VoiceManagerProxy.getInstance().startSpeaking(
                                        CommonLib.getInstance().getContext().getString(R.string.check_faults_fail_timeout), TTSType.TTS_DO_NOTHING, false);
                                logger.error("setCarTypeAndStartCarChecking", throwable);
                            });
            OBDStream.getInstance().exec("ATSETVEHICLE=" + carType);
        } catch (IOException e) {
            logger.error("setCarTypeAndStartCarChecking", e);
        }
    }

    private void startBMWCarChecking() {
        try {
            OBDStream.getInstance().exec("ATROFF");
            VoiceManagerProxy.getInstance().startSpeaking(
                    CommonLib.getInstance().getContext().getString(R.string.check_BMW_faults_after_flamout_fired), TTSType.TTS_DO_NOTHING, false);
            Observable.timer(1, TimeUnit.MINUTES)
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(aLong -> isWaitingForClearingFault = false
                            , throwable -> logger.error("startBMWCarChecking", throwable));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startCarCheckingIfCarStop() {
        logger.debug("startCarCheckingIfCarStop");
        try {
            carIsStopSubscription = SpeedFlow.carIsStoped()
                    .doOnNext(aBoolean -> logger.debug("obdString" + aBoolean))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(stoped -> {
                                startCarCheckingIfCarStoped(stoped);
                            }, throwable -> {
                                carIsStopSubscription.unsubscribe();
                                logger.error("carIsStoped", throwable);
                            }
                    );
        } catch (Exception e) {
            logger.error("startChecking", e);
        }
    }

    private void startCarCheckingIfCarStoped(boolean stoped) {
        logger.debug("startCarCheckingIfCarStoped:" + stoped);
        carIsStopSubscription.unsubscribe();
        if (stoped) {
            startCarCheckingInOrder(false);
        } else {
            VoiceManagerProxy.getInstance().startSpeaking(
                    CommonLib.getInstance().getContext().getString(R.string.check_after_stop), TTSType.TTS_DO_NOTHING, false);
        }
    }

    private void startCarCheckingInOrder(boolean afterClearCode) {
        logger.debug("startCarCheckingInOrder:" + afterClearCode);
        if (isCheckingFaults) {
            return;
        }
        isCheckingFaults = true;
        subsriberInitChecking();
        try {
            subsriberNextChecking(afterClearCode);
            CarCheckFlow.startCarCheck(ECM);
        } catch (IOException e) {
            e.printStackTrace();
        }
        EventBus.getDefault().post(new FaultCodesEvent(ECM, FaultCodesEvent.CHECK_CODES_START, FaultCodesEvent.CHECK_CODES_RESULT_NO_CODES));
    }

    private void subsriberInitChecking() {
        logger.debug("subsriberInitChecking");
        carCheckTypeList.clear();
        carCheckTypeList.add(CarCheckType.ECM);
        carCheckTypeList.add(CarCheckType.TCM);
        carCheckTypeList.add(CarCheckType.ABS);
        carCheckTypeList.add(CarCheckType.SRS);
        carCheckTypeList.add(CarCheckType.WSB);
        carCheckTypeCheckedList.clear();
    }

    private void subsriberNextChecking(boolean afterClearCode) {
        logger.debug("subsriberNextChecking:" + afterClearCode);
        if (carCheckTypeList.size() <= 0) {
            finishACarChecking(afterClearCode);
            return;
        }
        CarCheckType type = carCheckTypeList.get(0);
        carCheckTypeList.remove(0);
        logger.debug("carCheckTypeList.size()" + carCheckTypeList.size());
        try {
            logger.debug("subsriberNextChecking:" + type);
            switch (type) {
                case ECM:
                    execSubscription = startNextChecking(ObservableFactory.engineFailed(), ECM, afterClearCode);
                    break;
                case TCM:
                    execSubscription = startNextChecking(ObservableFactory.gearboxFailed(), TCM, afterClearCode);
                    break;
                case ABS:
                    execSubscription = startNextChecking(ObservableFactory.ABSFailed(), ABS, afterClearCode);
                    break;
                case SRS:
                    execSubscription = startNextChecking(ObservableFactory.SRSFailed(), SRS, afterClearCode);
                    break;
                case WSB:
                    Observable.timer(2, TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(aLong -> startNextChecking(afterClearCode)
                                    , throwable -> {
                                        logger.error("subsriberNextChecking", throwable);
                                        startNextChecking(afterClearCode);
                                    });
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Subscription startNextChecking(Observable<String> observable, CarCheckType currentType, boolean afterClear) {
        logger.debug("startNextChecking:" + currentType);
        return observable
                .doOnNext(s -> logger.debug("startNextChecking:" + s))
                .timeout(ObdManage.READ_FAULT_TIME, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(codes -> {
                    logger.debug("startNextChecking:" + codes + " " + currentType);
                    doNextChecking(codes, currentType, afterClear);
                }, throwable -> {
                    logger.error("startNextChecking", throwable);
                    doNextChecking(null, currentType, afterClear);
                });
    }

    private void startNextChecking(boolean afterClear) {
        logger.debug("startNextChecking:" + CarCheckType.WSB);
        TpmsDatasFlow.findAllTirePressureDatas(new TpmsDataCallBack() {

            @Override
            public void onDatas(List<TirePressureData> result) {
                doNextChecking(",", CarCheckType.WSB, afterClear);
            }

            @Override
            public void onError(Exception error) {
                logger.error("startNextChecking", error);
                doNextChecking(null, CarCheckType.WSB, afterClear);
            }
        });
    }

    private void doNextChecking(String faultCodes, CarCheckType currentType, boolean afterClear) {
        logger.debug("doNextChecking:" + faultCodes + " " + currentType + " " + afterClear);
        boolean noFault = TextVerify.isEmpty(faultCodes) || faultCodes.trim().endsWith("null");
        boolean notSupport = !TextVerify.isEmpty(faultCodes) && faultCodes.trim().endsWith("SUPPORT");
        boolean error = !TextVerify.isEmpty(faultCodes) && faultCodes.trim().endsWith("ERROR");
        int result = notSupport ? FaultCodesEvent.CHECK_CODES_RESULT_NOT_SUPPORT :
                (error ? FaultCodesEvent.CHECK_CODES_RESULT_ERROR :
                        (noFault ? FaultCodesEvent.CHECK_CODES_RESULT_NO_CODES : FaultCodesEvent.CHECK_CODES_RESULT_HAS_CODES));
        execSubscription.unsubscribe();
        CarCheckType nextCheckType = null;
        if (carCheckTypeList.size() > 0) {
            nextCheckType = carCheckTypeList.get(0);
        }
        EventBus.getDefault().post(new FaultCodesEvent(currentType, FaultCodesEvent.CHECK_CODES_STOP, result));
        carCheckTypeCheckedList.add(currentType);
        if (nextCheckType != null) {
            EventBus.getDefault().post(new FaultCodesEvent(nextCheckType, FaultCodesEvent.CHECK_CODES_START, FaultCodesEvent.CHECK_CODES_RESULT_NO_CODES));
            if (nextCheckType != WSB) {
                final CarCheckType finalNextCheckType = nextCheckType;
                Observable.timer(1, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.newThread())
                        .subscribe(aLong -> {
                            try {
                                CarCheckFlow.startCarCheck(finalNextCheckType);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }, throwable -> logger.error("doNextChecking", throwable));
            }
        }
        subsriberNextChecking(afterClear);
    }

    private void finishACarChecking(boolean afterClear) {
        logger.debug("finishACarChecking:" + afterClear);
        DataFlowFactory.getDrivingFlow()
                .getAllFaultCodes()
                .timeout(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(faultCodeList -> {
                    logger.debug("getDefaultConfig carChecking is Over" + faultCodeList.size());
                    isCheckingFaults = false;
                    if (!afterClear) {
                        TpmsDatasFlow.findAllTirePressureDatas(new TpmsDataCallBack() {

                            @Override
                            public void onDatas(List<TirePressureData> result) {
                                showCheckingError(faultCodeList, result);
                            }

                            @Override
                            public void onError(Exception error) {
                                logger.error("startNextChecking", error);
                                showCheckingError(faultCodeList, null);
                            }
                        });
                    } else {
                        if (faultCodeList.size() > 0) {
                            EventBus.getDefault().post(new ClearFaultResultEvent(ClearFaultResultEvent.CLEAR_HAS_CODES));
                        } else {
                            EventBus.getDefault().post(new ClearFaultResultEvent(ClearFaultResultEvent.CLEAR_OK));
                        }
                    }
                }, throwable -> {
                    isCheckingFaults = false;
                    if (afterClear) {
                        EventBus.getDefault().post(new ClearFaultResultEvent(ClearFaultResultEvent.CLEAR_HAS_CODES));
                    }
                    logger.error("startNextChecking", throwable);
                });

    }

    public void cancelChecking() {
        if (carIsStopSubscription != null && !carIsStopSubscription.isUnsubscribed()) {
            carIsStopSubscription.unsubscribe();
        }
        if (execSubscription != null && !execSubscription.isUnsubscribed()) {
            execSubscription.unsubscribe();
        }
        isCheckingFaults = false;
    }

    public void clearFault(CarCheckType... carCheckTypes) {
        logger.debug("clearFault:" + carCheckTypes.toString());
        try {
            if (isClearingFault) {
                return;
            }
            initClearCheckingList(carCheckTypes);
            if (clearCarCheckTypeList.size() > 0) {
                isClearingFault = true;
                CarCheckFlow.clearCarCheckError(clearCarCheckTypeList.get(0));
                subsriberNextClearChecking();
            }
        } catch (Exception e) {
            logger.error("carChecking error ", e);
        }
    }

    private void initClearCheckingList(CarCheckType... carCheckTypes) {
        logger.debug("initClearCheckingList:" + carCheckTypes.toString());
        clearCarCheckTypeList.clear();
        for (CarCheckType carCheckType : carCheckTypes) {
            clearCarCheckTypeList.add(carCheckType);
        }
    }

    private void subsriberNextClearChecking() {
        logger.debug("subsriberNextClearChecking");
        if (clearCarCheckTypeList.size() <= 0) {
            isClearingFault = false;
            startCarCheckingInOrder(true);
            return;
        }
        CarCheckType type = clearCarCheckTypeList.get(0);
        clearCarCheckTypeList.remove(0);
        try {
            clearSubscription = startNextClearCarCheckError(
                    ObservableFactory.getCarCheckFlow().getFaultClear(type)
                    , clearCarCheckTypeList.size() > 0 ? clearCarCheckTypeList.get(0) : null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void nextClearing(CarCheckType nextCheckType) throws IOException {
        subsriberNextClearChecking();
        if (nextCheckType != null) {
            CarCheckFlow.clearCarCheckError(nextCheckType);
        }
    }

    private Subscription startNextClearCarCheckError(Observable<String> observable, CarCheckType nextCheckType) {
        logger.debug("startNextClearCarCheckError:");
        return observable
                .timeout(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    logger.debug(result);
                    clearSubscription.unsubscribe();
                    try {
                        if (result.endsWith(VehicleConstants.VEHICLE_OK)) {
                            nextClearing(nextCheckType);
                        } else if (result.endsWith(VehicleConstants.VEHICLE_ERROR)) {
                            clearCarCheckTypeList.clear();
                            nextClearing(nextCheckType);
                        } else {
                            isClearingFault = false;
                            clearCarCheckTypeList.clear();
                            EventBus.getDefault().post(new ClearFaultResultEvent(ClearFaultResultEvent.CLEAR_NOT_SUPPORT));
                        }
                    } catch (IOException e) {
                        isClearingFault = false;
                        e.printStackTrace();
                    }
                }, throwable -> {
                    clearSubscription.unsubscribe();
                    isClearingFault = false;
                    EventBus.getDefault().post(new ClearFaultResultEvent(ClearFaultResultEvent.CLEAR_ERROR));
                    logger.error("startNextClearCarCheckError", throwable);
                });
    }

    private void showCheckingError(List<FaultCode> faultCodeList, List<TirePressureData> tirePressureDatas) {
        logger.debug("showCheckingError:" + faultCodeList.toArray().toString());
        String[] faultCodeTypes = new String[faultCodeList.size()];
        for (int i = 0; i < faultCodeList.size(); i++) {
            FaultCode faultCode = faultCodeList.get(i);
            faultCodeTypes[i] = FaultCodeFlow.getVehicleConstants(faultCode.getCarCheckType());
        }
        logger.debug("carChecking showCheckingError {}", faultCodeTypes.length);
        if (faultCodeList.size() > 0 || tirePressureDatas.size() > 0) {
            StringBuffer playText = new StringBuffer();
//            playText.append("为您检测到");
            for (String faultCodeType : faultCodeTypes) {
                playText.append(FaultCodeFlow.getShowConstants(faultCodeType));
                playText.append("、");
            }
            if (tirePressureDatas != null && tirePressureDatas.size() > 0) {
                playText.append(FaultCodeFlow.getShowConstants(VehicleConstants.VEHICLE_WSB));
            }
//            playText.append("故障");


            EventBus.getDefault().post(new ShowFaultPageEvent(faultCodeTypes, playText.toString()));
        } else {

        }
    }

    public void cancelClearFaults() {
        if (clearSubscription != null && !clearSubscription.isUnsubscribed()) {
            clearSubscription.unsubscribe();
        }
        isClearingFault = false;
    }

    public boolean isCheckingFaults() {
        return isCheckingFaults;
    }

    public boolean isClearingFault() {
        return isClearingFault;
    }

    public List<CarCheckType> getCarCheckTypeCheckedList() {
        return carCheckTypeCheckedList;
    }

    public List<CarCheckType> getCarCheckTypeList() {
        return carCheckTypeList;
    }

}
