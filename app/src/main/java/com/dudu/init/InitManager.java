package com.dudu.init;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.StrictMode;

import com.blur.SoundPlayManager;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.exception.CrashHandler;
import com.dudu.android.launcher.service.BluetoothService;
import com.dudu.android.launcher.service.FloatBackButtonService;
import com.dudu.android.launcher.utils.AgedUtils;
import com.dudu.android.launcher.utils.CarStatusUtils;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.android.launcher.utils.StatusBarManager;
import com.dudu.android.launcher.utils.Utils;
import com.dudu.android.launcher.utils.WifiApAdmin;
import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.VersionTools;
import com.dudu.drivevideo.frontcamera.FrontCameraManage;
import com.dudu.event.DeviceEvent;
import com.dudu.map.GaodeMapAppUtil;
import com.dudu.monitor.repo.location.LocationManage;
import com.dudu.navi.NavigationManager;
import com.dudu.persistence.rx.RealmManage;
import com.dudu.service.GpsNmeaManager;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.weather.WeatherStream;
import com.dudu.workflow.common.CommonParams;
import com.dudu.workflow.common.DataFlowFactory;
import com.dudu.workflow.common.ObservableFactory;
import com.dudu.workflow.push.ReceiverDataFlow;
import com.networkbench.agent.impl.NBSAppAgent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import ch.qos.logback.core.android.SystemPropertiesProxy;
import de.greenrobot.event.EventBus;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by 赵圣琪 on 2015/11/24.
 */
public class InitManager {

    private static InitManager mInstance;

    private static Logger logger = LoggerFactory.getLogger("init.manager");
    private Subscription releaseCameraSubscription;

    private int log_step = 0;

    private Context mContext;

    private HandlerThread mInitThread;

    private Handler mInitHandler;
    private boolean entered;

    private InitManager() {

        mContext = LauncherApplication.getContext();

        entered = false;

        mInitThread = new HandlerThread("init thread");
        mInitThread.start();

        mInitHandler = new Handler(mInitThread.getLooper());
    }

    public static InitManager getInstance() {
        if (mInstance == null) {
            mInstance = new InitManager();
        }

        return mInstance;
    }

    public synchronized boolean init() {
        logger.debug("[init][{}] init call:{}", log_step++, entered);
        if (entered) return true;
        logger.debug("[init][{}]初始化", log_step++);
        entered = true;
        NBSAppAgent.setLicenseKey("820f83526db145d6974a3fe22ae7ac7a").withLocationServiceEnabled(true).start(mContext);

        mInitHandler.post(new Runnable() {
            @Override
            public void run() {
                appInit(LauncherApplication.getContext());
                initOthers();
            }
        });

        return true;
    }

    public void unInit() {
        logger.debug("程序崩溃，释放语音资源");
        VoiceManagerProxy.getInstance().stopSpeaking();
        VoiceManagerProxy.getInstance().stopUnderstanding();
        VoiceManagerProxy.getInstance().onDestroy();
        //chad add
//        stopBluetoothService();//不主动停止蓝牙服务，保持后台长期运行
        mInitThread.quitSafely();

        if (NavigationManager.getInstance(CommonLib.getInstance().getContext()).isNavigatining()) {
            GaodeMapAppUtil.exitGapdeApp();
        }
    }

    /**
     * 开启悬浮按钮服务
     */
    private void startFloatButtonService() {
        Intent i = new Intent(mContext, FloatBackButtonService.class);
        mContext.startService(i);
    }

    /**
     * 开启蓝牙电话服务
     */
    private void startBluetoothService() {
//        stopBluetoothService();
        Intent intent = new Intent(mContext, BluetoothService.class);
        mContext.startService(intent);
    }

    /**
     * 停止蓝牙电话服务
     * chad add
     */
    private void stopBluetoothService() {
        Intent intent = new Intent(mContext, BluetoothService.class);
        mContext.stopService(intent);
    }

    /**
     * 打开蓝牙
     */
    private void openBlueTooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(
                Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }
        }
    }

    /**
     * 工厂检测,暂不做检测
     */
    private boolean checkBTFT() {
        SystemPropertiesProxy sps = SystemPropertiesProxy.getInstance();
        boolean need_bt = !"1".equals(sps.get("persist.sys.bt", "0"));
        boolean need_ft = !"1".equals(sps.get("persist.sys.ft", "0"));
        Intent intent;
        PackageManager packageManager = mContext.getPackageManager();
        intent = packageManager.getLaunchIntentForPackage("com.qualcomm.factory");
        if (intent != null) {
            //close wifi ap for ft test
            WifiApAdmin.closeWifiAp(mContext);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return false;
        } else {
            initOthers();
            return true;
        }
    }

    private void initOthers() {
        logger.info("launcher版本名：{}", VersionTools.getAppVersion(CommonLib.getInstance().getContext()));
        logger.info("launcher版本号：{}", VersionTools.getAppVersionCode(CommonLib.getInstance().getContext()));

        // 关闭ADB调试端口
        if (!Utils.isDemoVersion(mContext)) {
            com.dudu.android.hideapi.SystemPropertiesProxy.getInstance().set(mContext,
                    "persist.sys.usb.config", "charging");
        }


        //卸载残留的老化软件
        AgedUtils.uninstallAgedApk(mContext);

        GpsNmeaManager.getInstance();

        rx.Observable.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    logger.debug("[init][{}]启动GPS Nmea采集");

                    GpsNmeaManager.getInstance().addGpsNmeaListener();
                }, throwable -> logger.error("GpsNmeaManager", throwable));

        rx.Observable.timer(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    logger.debug("[init][{}]打开蓝牙", log_step++);
                    openBlueTooth();
                }, throwable -> logger.error("initOthers", throwable));

        logger.debug("[init][{}]启动监听服务", log_step++);

        logger.debug("[init][{}]启动悬浮返回按钮服务", log_step++);
        startFloatButtonService();

        logger.debug("[init][{}]开启蓝牙电话服务", log_step++);
        startBluetoothService();

        NavigationManager.getInstance(LauncherApplication.getContext()).initNaviManager();

        StatusBarManager.getInstance().initBarStatus();

        screenOnOrOff();

        VoiceManagerProxy.getInstance().onInit();
    }

    private void screenOnOrOff() {
        EventBus.getDefault().post(new DeviceEvent.Screen(DeviceEvent.ON));
    }

    public void bootInit(Context context) {
        laucherApplicationInit(context);
    }

    public void appInit(Context context) {
        laucherApplicationInit(context);
    }

    public void laucherApplicationInit(Context context) {
        RealmManage.init(context);

        if (Constants.DEBUG_STRICT_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyDialog()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build());
        }

        CrashHandler crashHandler = CrashHandler.getInstance();

        // 注册crashHandler
        crashHandler.init(context);

        DataFlowFactory.getInstance().init();

        CommonParams.getInstance().init();
        ObservableFactory.init();

        ReceiverDataFlow.getInstance().init();

        SoundPlayManager.init(context, R.raw.take_photo2);//初始化拍照音效

        StatusBarManager.getInstance().initBarStatus();

        screenOnControl();

        WeatherStream.getInstance().startService();

        CarFireManager.getInstance().doIfFired();

    }

    public static void screenOnControl() {
//            CameraControl.instance().updateState();
        FrontCameraManage.getInstance().init();
        FrontCameraManage.getInstance().startPreview();
        LocationManage.getInstance().init();
    }

    public void screenOffControl() {
        if (releaseCameraSubscription != null) {
            releaseCameraSubscription.unsubscribe();
        }

        releaseCameraSubscription = CarStatusUtils.isFired()
                .filter(fired -> !fired)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(notFired -> {
                            LoggerFactory.getLogger("video.ScreenReceiver").debug("即将释放Camera和MediaRecorder资源");
//                                CameraControl.instance().setState(CameraControl.STATE_RELEASE);
                            FrontCameraManage.getInstance().release();
                            LocationManage.getInstance().release();
                        }
                        , throwable -> logger.error("obdWorkStart", throwable));
    }

}
