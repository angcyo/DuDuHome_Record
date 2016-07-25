package com.dudu.monitor.obd;

import com.dudu.monitor.active.ActiveDeviceManage;
import com.dudu.monitor.event.CarStatus;
import com.dudu.monitor.obd.modol.FlamoutData;
import com.dudu.monitor.obd.modol.FlamoutDataUpload;
import com.dudu.monitor.obd.modol.ObdRTData;
import com.dudu.monitor.obd.modol.ObdRTDataUpload;
import com.dudu.network.NetworkManage;
import com.dudu.network.message.DriveHabitsDataUpload;
import com.dudu.network.message.ObdRtDataUpload;
import com.dudu.workflow.obd.OBDStream;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by dengjun on 2016/3/8.
 * Description :
 */
public class ObdManage {
    private static ObdManage instance = null;
    private int curSpeed;//当前车速
    private float curRpm;//当前转速
    public static final int READ_FAULT_TIME = 10;
    /* 车辆状态*/
    private CarStatus carStatus = CarStatus.OFFLINE;
    private float cur_batteryV;

    private Subscription realTimeObdDataSubscription;
    private Subscription flamoutDataSubscription;

    private Gson gson;

    private Logger log = LoggerFactory.getLogger("car.ObdManage");

    private ObdManage() {
        gson = new Gson();
    }


    public static ObdManage getInstance() {
        if (instance == null) {
            synchronized (ObdManage.class) {
                if (instance == null) {
                    instance = new ObdManage();
                }
            }
        }
        return instance;
    }

    public void init() {
        sendStartDataStreamCmd();
        log.info("初始化obd数据上传");
        proObdRtData();
        proFlamoutData();
    }

    public static void obdSleep() {
        try {
            OBDStream.getInstance().exec("ATENTERSLEEP");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取总里程
     */
    public static void obdGetTotalDistance() {
        try {
            OBDStream.getInstance().exec("ATGETMIL");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取剩余油量
     */
    public static void obdRemainL() {
        try {
            OBDStream.getInstance().exec("ATGETFUEL");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void proObdRtData() {
        try {
            realTimeObdDataSubscription =
                    OBDStream.getInstance()
                            .OBDRTData()
                            .map((obdRtDataStringArray) -> new ObdRTData(obdRtDataStringArray))
                            .doOnNext((obdRTData) -> {
                                preProObdRealData(obdRTData);
                            })
                            .map((obdRTData) -> new ObdRTDataUpload(obdRTData))
                            .buffer(15, TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.newThread())
                            .subscribe(obdRTDataUploadList -> {
                                        sendObdRealData(obdRTDataUploadList);
                                    },
                                    (throwable -> {
                                        log.error("OBDRTData buffer 异常：", throwable);
                                    }),
                                    () -> log.error("该次读取结束"));
        } catch (IOException e) {
            log.error("proObdRtData 异常：", e);
        }
    }

    private void sendStartDataStreamCmd() {
        rx.Observable.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(l -> {
                    try {
                        log.info("发送开启数据流命令");
                        OBDStream.getInstance().exec("ATRON");
                    } catch (IOException e) {
                        log.error("异常：", e);
                    }
                }, throwable -> {
                    log.error("异常：", throwable);
                });
    }


    public void release() {
        if (realTimeObdDataSubscription != null) {
            realTimeObdDataSubscription.unsubscribe();
            realTimeObdDataSubscription = null;
        }
        if (flamoutDataSubscription != null) {
            flamoutDataSubscription.unsubscribe();
            flamoutDataSubscription = null;
        }
    }

    private void preProObdRealData(ObdRTData obdRTData) {
//        log.debug("车速：{}，转速：{}", obdRTData.getSpd(), obdRTData.getEngSpd());
    }

    private void sendObdRealData(List<ObdRTDataUpload> obdRTDataUploadList) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (ObdRTDataUpload obdRTDataUpload : obdRTDataUploadList) {
                jsonArray.put(new JSONObject(gson.toJson(obdRTDataUpload)));
            }
            if (jsonArray.length() > 0 && ActiveDeviceManage.getInstance().isDeviceActived()) {
                NetworkManage.getInstance().sendMessage(new ObdRtDataUpload(jsonArray));
            }
        } catch (JSONException e) {
            log.error("异常", e);
        }
    }


    private void proFlamoutData() {
        try {
            flamoutDataSubscription =
                    OBDStream.getInstance()
                            .OBDTTData()
                            .map((obdTTDataStringArray) -> new FlamoutData(obdTTDataStringArray))
                            .map((flamoutData1 -> new FlamoutDataUpload(flamoutData1)))
                            .subscribeOn(Schedulers.newThread())
                            .subscribe((flamoutData -> {
                                        log.debug("收到熄火数据：{}", gson.toJson(flamoutData));
                                        sendFlamoutData(flamoutData);
                                    }),
                                    (throwable -> {
                                        log.error("异常：", throwable);
                                    }));
        } catch (IOException e) {
            log.error("异常：", e);
        }
    }

    private void sendFlamoutData(FlamoutDataUpload flamoutDataUpload) {
        try {
            if (ActiveDeviceManage.getInstance().isDeviceActived()) {
                new Thread(() -> {
                    NetworkManage.getInstance().sendMessage(new DriveHabitsDataUpload(gson.toJson(flamoutDataUpload)));
                }).start();
            }

        } catch (Exception e) {
            log.error("异常：", e);
        }
    }


    public int getCurSpeed() {
        return curSpeed;
    }

    public void setCurSpeed(int curSpeed) {
        this.curSpeed = curSpeed;
    }

    public float getCurRpm() {
        return curRpm;
    }

    public void setCurRpm(float curRpm) {
        this.curRpm = curRpm;
    }

    public CarStatus getCarStatus() {
        return carStatus;
    }

    public void setCarStatus(CarStatus carStatus) {
        this.carStatus = carStatus;
    }

    public float getCur_batteryV() {
        return cur_batteryV;
    }

    public void setCur_batteryV(float cur_batteryV) {
        this.cur_batteryV = cur_batteryV;
    }
}
