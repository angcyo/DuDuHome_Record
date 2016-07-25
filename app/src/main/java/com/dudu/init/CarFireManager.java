package com.dudu.init;

import android.content.Context;
import android.os.PowerManager;

import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.android.launcher.utils.CarStatusUtils;
import com.dudu.android.launcher.utils.WifiApAdmin;
import com.dudu.carChecking.CarCheckingProxy;
import com.dudu.commonlib.CommonLib;
import com.dudu.drivevideo.frontcamera.FrontCameraManage;
import com.dudu.drivevideo.rearcamera.RearCameraManage;
import com.dudu.drivevideo.spaceguard.StorageSpaceService;
import com.dudu.monitor.active.ActiveDeviceManage;
import com.dudu.monitor.flow.FlowManage;
import com.dudu.monitor.obd.ObdManage;
import com.dudu.monitor.obdUpdate.ObdUpdateService;
import com.dudu.monitor.portal.PortalManage;
import com.dudu.monitor.repo.location.LocationManage;
import com.dudu.monitor.tirepressure.TirePressureManage;
import com.dudu.network.NetworkManage;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.workflow.common.ObservableFactory;
import com.dudu.workflow.obd.OBDStream;
import com.dudu.workflow.obd.ObdGpioControl;
import com.dudu.workflow.obd.RobberyFlow;
import com.dudu.workflow.tpms.TpmsStream;
import com.dudu.workflow.upgrade.LauncherUpgrade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/5/19.
 */
public class CarFireManager {
    private static Logger logger = LoggerFactory.getLogger("init.CarFireManager");

    private static CarFireManager mInstance = new CarFireManager();

    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;

    private Subscription requestVersionInfoSubsCription;
    private Subscription flamoutControlSubscription;
    private Subscription obdInitSubsCription;
    private Subscription obdUpdateSubsCription;
    private Subscription tpmsInitSubsCription;
    private Subscription releaseWakeLockSubscription;
    private Subscription updateObdSubscription;

    public static CarFireManager getInstance() {
        return mInstance;
    }

    private CarFireManager() {
        mPowerManager = (PowerManager) CommonLib.getInstance().getContext().getSystemService(Context.POWER_SERVICE);
    }

    public void doIfFired() {
        CarStatusUtils.isFired()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fired -> {
                            if (fired) {
                                logger.info("点火，启动各种服务");
                                CarFireManager.getInstance().fireControl();
                            } else {
                                logger.info("未点火，不启动各种服务");
                            }
                        }
                        , throwable -> logger.error("doIfFired", throwable));
    }

    /**
     * 点火操作
     */
    public void fireControl() {
        logger.debug("fireControl");
        cancelAllSubscription();

        CarStatusUtils.saveCarIsFire(true);
        //开启语音唤醒
        VoiceManagerProxy.getInstance().startWakeup();

        initObd();
        initTpms();

        ActiveDeviceManage.getInstance().init();
        LocationManage.getInstance().startSendLocation();
        PortalManage.getInstance().init();
        FlowManage.getInstance().init();
        requestVersionInfo();
        NetworkManage.getInstance().init();
        StorageSpaceService.getInstance().init();

        RearCameraManage.getInstance().setRecordEnable(true);
        RearCameraManage.getInstance().startRecord();

        //直接可以在主线程中初始化，摄像头操控都在handlerThread 中完成，不会阻塞主线程
        FrontCameraManage.getInstance().setRecordEnable(true);//点火才使能录像
        FrontCameraManage.getInstance().init();
        FrontCameraManage.getInstance().startRecord();//防止已经初始化了，导致不能开启录像

        new Thread() {
            public void run() {
                logger.debug("fireControl:Thread.run");
                if (CarStatusUtils.isWifiAvailable()) {
                    WifiApAdmin.startWifiAp(CommonLib.getInstance().getContext());
                }
                acquireLock();
            }
        }.start();

    }

    /**
     * 熄火操作
     */
    public void flamoutControl() {
        logger.debug("flamoutControl");
        cancelAllSubscription();

        CarStatusUtils.saveCarIsFire(false);
        ActivitiesManager.toMainActivity();
        ActiveDeviceManage.getInstance().release();
        releaseWakeLock();
        ObdManage.obdSleep();
        LocationManage.getInstance().cancerSendLocation();
        PortalManage.getInstance().release();
        flamoutDelayControl();
    }

    public void acquireLock() {
        logger.debug("启动wakelock");
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "screenswakelock");
        mWakeLock.acquire();
    }

    private void releaseWakeLock() {
        logger.debug("释放wakelock");
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    public void releaseWakeLockIfNotFired() {
        logger.debug("releaseWakeLockIfNotFired");
        if (releaseWakeLockSubscription != null) {
            releaseWakeLockSubscription.unsubscribe();
        }
        releaseWakeLockSubscription = Observable.timer(2, TimeUnit.SECONDS)
                .zipWith(CarStatusUtils.isFired(), (aLong, isFired) -> isFired)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fired -> {
                    if (!fired) {
                        releaseWakeLock();
                    }
                }, throwable -> logger.error("obdWorkStart", throwable));
    }

    /**
     * 版本检测
     */
    private void requestVersionInfo() {
        logger.debug("requestVersionInfo");
        requestVersionInfoSubsCription = Observable
                .timer(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(l -> {
                    logger.debug("requestVersionInfo:LauncherUpgrade.queryVersionInfo");
                    LauncherUpgrade.queryVersionInfo();
                }, throwable -> {
                    logger.error("requestVersionInfo", throwable);
                });
    }

    /**
     * 点火后的obd操作
     */
    private void initObd() {
        logger.debug("initObd");
        //启用obd
        ObdGpioControl.powerOnObd();
        ObdGpioControl.wakeObd();

        OBDStream.getInstance().init();
        ObdUpdateService.getInstance().init();
        //检查obd版本
        obdUpdateSubsCription = ObdUpdateService.getInstance().delayQueryServerVersion(30);
        //上传数据流、检测电压数据、开始车辆自检、开始防劫逻辑检测
        obdInitSubsCription = Observable.timer(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    logger.debug("initObd:ObdManage.init");
                    ObdManage.getInstance().init();
                    try {
                        ObservableFactory.getDrivingFlow().checkShouldMonitorAccVoltage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    CarCheckingProxy.getInstance().requestCarTypeAndStartCarchecking();
                    RobberyFlow.checkGunSwitch();
                }, throwable -> logger.error("initObd", throwable));
    }

    /**
     * 点火后的胎压操作
     */
    private void initTpms() {
        logger.debug("initTpms");
        TpmsStream.getInstance().init();
        tpmsInitSubsCription = Observable.timer(2, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(aLong -> {
                    logger.debug("initTpms:TirePressureManage.init");
                    TirePressureManage.getInstance().init();
                }, throwable -> logger.error("initTpms", throwable));
    }

    /**
     * 熄火延时操作（停止语音唤醒、obd流停止下电、断开长链接、停止录像、释放摄像头）
     */
    private void flamoutDelayControl() {
        logger.debug("flamoutDelayControl");
        updateObdSubscription = Observable.timer(1, TimeUnit.MINUTES)
                .subscribeOn(Schedulers.newThread())
                .subscribe(aLong1 -> ObdUpdateService.getInstance().updateObdBin());
        flamoutControlSubscription = Observable.timer(5,TimeUnit.MINUTES)
                .subscribeOn(Schedulers.newThread())
                .subscribe(aLong -> {
                    flamoutControlBeforeSleep();
                }, throwable -> logger.error("flamoutControl", throwable));
    }

    public void flamoutControlBeforeSleep() {
        logger.debug("flamoutControlBeforeSleep:");

        VoiceManagerProxy.getInstance().stopSpeaking();
        VoiceManagerProxy.getInstance().onStop();
        VoiceManagerProxy.getInstance().stopWakeup();

        ObdManage.getInstance().release();

        //关闭热点
        WifiApAdmin.closeWifiAp(CommonLib.getInstance().getContext());

        FlowManage.getInstance().release();
        NetworkManage.getInstance().release();

        //停止录像
        logger.debug("即将释放Camera和MediaRecorder资源");
//                    CameraControl.instance().setState(CameraControl.STATE_RELEASE);
        FrontCameraManage.getInstance().setRecordEnable(false);
        FrontCameraManage.getInstance().stopRecord();

        RearCameraManage.getInstance().stopPreview();
        RearCameraManage.getInstance().setRecordEnable(false);
        RearCameraManage.getInstance().stopRecord();

        StorageSpaceService.getInstance().release();

        TirePressureManage.getInstance().release();
        TpmsStream.getInstance().tpmsStreamClose();

        //关闭obd串口，obd下电
        ObdGpioControl.powerOffObd();
        OBDStream.getInstance().obdStreamClose();

        LauncherUpgrade.installLauncherApk();
    }

    private void cancelAllSubscription() {
        logger.debug("cancelAllSubscription");
        if (flamoutControlSubscription != null) {
            flamoutControlSubscription.unsubscribe();
        }
        if (requestVersionInfoSubsCription != null) {
            requestVersionInfoSubsCription.unsubscribe();
        }
        if (obdInitSubsCription != null) {
            obdInitSubsCription.unsubscribe();
        }
        if (obdUpdateSubsCription != null) {
            obdUpdateSubsCription.unsubscribe();
        }
        if (tpmsInitSubsCription != null) {
            tpmsInitSubsCription.unsubscribe();
        }
        if (releaseWakeLockSubscription != null) {
            releaseWakeLockSubscription.unsubscribe();
        }
        if (updateObdSubscription != null) {
            updateObdSubscription.unsubscribe();
        }
    }
}
