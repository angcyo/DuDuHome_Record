package com.dudu.android.launcher.ui.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.blur.CameraControl;
import com.blur.SurfaceWindow;
import com.dudu.aios.ui.activity.MainRecordActivity;
import com.dudu.aios.ui.activity.OtherApkActivity;
import com.dudu.aios.ui.utils.InstallerUtils;
import com.dudu.aios.ui.utils.Rx;
import com.dudu.aios.ui.view.SpinnerItem;
import com.dudu.android.hideapi.SystemPropertiesProxy;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.broadcast.ACCReceiver;
import com.dudu.android.launcher.utils.CarStatusUtils;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.android.launcher.utils.WifiApAdmin;
import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.ToastUtils;
import com.dudu.drivevideo.frontcamera.FrontCameraManage;
import com.dudu.drivevideo.rearcamera.RearCameraManage;
import com.dudu.drivevideo.service.FrontDriveVideoService;
import com.dudu.drivevideo.spaceguard.StorageSpaceService;
import com.dudu.drivevideo.utils.FileUtil;
import com.dudu.drivevideo.utils.UsbControl;
import com.dudu.monitor.obd.ObdManage;
import com.dudu.monitor.obdUpdate.ObdUpdateService;
import com.dudu.monitor.obdUpdate.config.ObdUpdateCmd;
import com.dudu.monitor.repo.SensorManage;
import com.dudu.monitor.tirepressure.TirePressureManage;
import com.dudu.navi.event.NaviEvent;
import com.dudu.network.NetworkManage;
import com.dudu.network.utils.IPConfig;
import com.dudu.persistence.rx.RealmManage;
import com.dudu.rest.model.driving.response.GetCarBrandResponse;
import com.dudu.service.GpsNmeaManager;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.workflow.common.DataFlowFactory;
import com.dudu.workflow.common.RequestFactory;
import com.dudu.workflow.obd.CarLock;
import com.dudu.workflow.obd.OBDStream;
import com.dudu.workflow.obd.ObdGpioControl;
import com.dudu.workflow.push.ReceiverDataFlow;
import com.dudu.workflow.push.model.PushParams;
import com.dudu.workflow.push.model.ReceiverPushData;
import com.dudu.workflow.tpms.TPMSFlow;
import com.dudu.workflow.tpms.TPMSInfo;
import com.dudu.workflow.tpms.TpmsStream;

import org.slf4j.LoggerFactory;
import org.wysaid.camera.CameraInstance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lxh on 2016/1/2.
 */
public class DebugActivity extends Activity implements View.OnClickListener {

    private EditText editText_ip;

    private EditText editText_port;

    private EditText editText_Testip;

    private EditText editText_Testport;

    private EditText editText_UserName;

    private TextView tpmsPrint;

    private Button btn_save;

    private Button btn_reset;

    private Button btnBack;

    private Button btnOpenGsp;

    private Button btnOpenMap;

    private Button btnOpenCamera;

    private Button btnOpenMusic;

    private Button btnOpenAdb;

    private Button btnCloseWifi;

    private Button btnCloseVideo;

    private Button btnStartVideo;

    private Button btnCloseVoice;

    private Button btnOpenFactoryTest;

    private Button btnOpenUCBrower;

    private Button btnOPenRecord;

    private Button btnOpenDial;

    private Button btnOpenOtherApk;
    private Spinner carTypeCodeSpinner;

    private RadioGroup radioGroup;
    private RadioButton radioBtnFormal, radioBtnTest;

    private IPConfig ipConfig;

    private boolean isTest = true;

    private String ip, testIP;

    private int port, testPort;

    private HashMap<Integer, String> obdCarMap;

    /* @Override
     protected View getChildView() {
         return LayoutInflater.from(this).inflate(R.layout.ip_congfig_layout, null);
     }*/
    private Subscription tpmsSub = null;
    private Subscription tpmsPairSub = null;
    private Subscription setCarTypeSubscription;

    public static boolean isMultiMic() {
        boolean ret = false;
        String mic = com.dudu.android.hideapi.SystemPropertiesProxy.getInstance().get("persist.sys.mic.multi", "0");
        if (TextUtils.equals(mic, "0")) {
            ret = false;
        } else if (TextUtils.equals(mic, "1")) {
            ret = true;
        }

        return ret;
    }

    public static boolean isLogcatEnable() {
        boolean ret = false;
        String enable = com.dudu.android.hideapi.SystemPropertiesProxy.getInstance().get("persist.sys.logcat.enable", "0");
        if (TextUtils.equals(enable, "0")) {
            ret = false;
        } else if (TextUtils.equals(enable, "1")) {
            ret = true;
        }

        return ret;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug_layout);
        initView(savedInstanceState);
        initListener();
    }

    public void initView(Bundle savedInstanceState) {
        ipConfig = IPConfig.getInstance();

        editText_ip = (EditText) findViewById(R.id.ip_edt);

        editText_port = (EditText) findViewById(R.id.port_edt);

        editText_Testip = (EditText) findViewById(R.id.test_ip_edt);

        editText_Testport = (EditText) findViewById(R.id.edt_testPort);

        editText_UserName = (EditText) findViewById(R.id.edt_username);

        btn_save = (Button) findViewById(R.id.btn_ip_save);

        btn_reset = (Button) findViewById(R.id.btn_ip_reset);

        btnOpenGsp = (Button) findViewById(R.id.openGps);

        btnOpenMap = (Button) findViewById(R.id.openRMap);

        radioGroup = (RadioGroup) findViewById(R.id.ip_radioGroup);

        radioBtnFormal = (RadioButton) findViewById(R.id.radioBtnFormal);

        radioBtnTest = (RadioButton) findViewById(R.id.radioBtnTest);

        btnBack = (Button) findViewById(R.id.back_button);

        btnOpenCamera = (Button) findViewById(R.id.button_open_camera);

        btnOpenMusic = (Button) findViewById(R.id.button_open_music);

        btnOpenAdb = (Button) findViewById(R.id.openAdb);

        btnCloseVideo = (Button) findViewById(R.id.closeVideo);
        btnStartVideo = (Button) findViewById(R.id.startVideo);

        btnCloseVoice = (Button) findViewById(R.id.closeVoice);

        btnCloseWifi = (Button) findViewById(R.id.closeWifi);

        btnOpenFactoryTest = (Button) findViewById(R.id.openFactoryTest);

        btnOpenUCBrower = (Button) findViewById(R.id.open_UC_browser);

        btnOPenRecord = (Button) findViewById(R.id.open_record);

        btnOpenDial = (Button) findViewById(R.id.open_dial);

        btnOpenOtherApk = (Button) findViewById(R.id.openOtherApk);

        initMicView((TextView) findViewById(R.id.buttonMic));
        initLogcatEnable((TextView) findViewById(R.id.buttonLogcat));
        initStreamView((TextView) findViewById(R.id.buttonLibStream));
        initRecordView((TextView) findViewById(R.id.buttonStartRecorder));

        carTypeCodeSpinner = (Spinner) findViewById(R.id.cartypecode);
        List<SpinnerItem> spinnerItems = new ArrayList<SpinnerItem>();
        obdCarMap = new HashMap<>();

        obdCarMap.put(0, "标准");
        obdCarMap.put(1, "路虎");
        obdCarMap.put(2, "丰田");
        obdCarMap.put(3, "奔驰");
        obdCarMap.put(4, "宝马");
        obdCarMap.put(5, "福特");
        obdCarMap.put(6, "通用");
        obdCarMap.put(7, "本田");
        obdCarMap.put(8, "起亚");
        obdCarMap.put(9, "大众");
        obdCarMap.put(10, "现代");
        obdCarMap.put(11, "马自达");
        obdCarMap.put(12, "日产");
        obdCarMap.put(13, "沃尔沃");
        obdCarMap.put(14, "吉普");
        obdCarMap.put(15, "长城");
        obdCarMap.put(16, "海马");
        obdCarMap.put(17, "标致");

        for (Object o : obdCarMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Integer key = (Integer) entry.getKey();
            String val = String.valueOf(entry.getValue());
            spinnerItems.add(new SpinnerItem(key, val));
        }

        ArrayAdapter<SpinnerItem> adapter = new ArrayAdapter<SpinnerItem>(this,
                android.R.layout.simple_spinner_item, spinnerItems);

        carTypeCodeSpinner.setAdapter(adapter);

        isTest = ipConfig.isTest_Server();
        if (isTest) {
            radioBtnTest.setChecked(true);
        } else {
            radioBtnFormal.setChecked(true);
        }

        editText_ip.setText(ipConfig.getServerIP());
        editText_Testip.setText(ipConfig.getTestServerIP());
        editText_port.setText(ipConfig.getServerPort() + "");
        editText_Testport.setText(ipConfig.getTestServerPort() + "");

        tpmsPrint = (TextView) findViewById(R.id.tpms_print);
    }

    public void initListener() {

        btn_save.setOnClickListener(this);

        btn_reset.setOnClickListener(this);

        btnBack.setOnClickListener(this);

        btnOpenGsp.setOnClickListener(this);

        btnOpenMap.setOnClickListener(this);

        btnOpenMusic.setOnClickListener(this);

        btnOpenCamera.setOnClickListener(this);

        btnOpenAdb.setOnClickListener(this);

        btnCloseVoice.setOnClickListener(this);

        btnCloseWifi.setOnClickListener(this);


        btnOpenFactoryTest.setOnClickListener(this);

        btnOpenUCBrower.setOnClickListener(this);

        btnOPenRecord.setOnClickListener(this);

        btnOpenDial.setOnClickListener(this);

        btnOpenOtherApk.setOnClickListener(this);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioBtnFormal) {
                    isTest = false;
                } else if (checkedId == R.id.radioBtnTest) {
                    isTest = true;
                }
            }
        });

        findViewById(R.id.btn_gpsNmea_open).setOnClickListener(this);
        findViewById(R.id.btn_gpsNmea_close).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ip_reset:
                reset();
                break;
            case R.id.btn_ip_save:
                changeIp();
                break;
            case R.id.back_button:
                startActivity(new Intent(DebugActivity.this, MainRecordActivity.class));
                finish();
                break;
            case R.id.openRMap:
                openMap();
                break;
            case R.id.openGps:
                openGps();
                break;
            case R.id.button_open_camera:
                openCamera();
                break;
            case R.id.button_open_music:
                openMusic();
                break;
            case R.id.openAdb:
                openAdb();
                break;
            case R.id.closeVoice:
                closeVoice();
                break;
            case R.id.closeWifi:
                closeWifi();
                break;
            case R.id.openFactoryTest:
                openFactoryTest();
                break;
            case R.id.open_UC_browser:
                oepnBrower();
                break;
            case R.id.open_record:
                openRecord();
                break;
            case R.id.open_dial:
                openDial();
                break;
            case R.id.openOtherApk:
                openOtherApk();
                break;
            case R.id.btn_gpsNmea_open:
                GpsNmeaManager.getInstance().addGpsNmeaListener();
                break;
            case R.id.btn_gpsNmea_close:
                GpsNmeaManager.getInstance().removeGpsNmeaListener();
                break;
        }
    }

    public void openVoice(View v) {
        VoiceManagerProxy.getInstance().onInit();
    }

    private void openOtherApk() {
        startActivity(new Intent(this, OtherApkActivity.class));
    }

    public void obdGeneral(View view) {
        try {
            SpinnerItem spinnerItem = (SpinnerItem) carTypeCodeSpinner.getSelectedItem();
            setCarTypeSubscription = OBDStream.getInstance().OBDSetCarType()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result -> {
                                setCarTypeSubscription.unsubscribe();
                                ToastUtils.showTip("设置车型为" + spinnerItem.getValue() + "成功");
                            },
                            throwable -> {
                                setCarTypeSubscription.unsubscribe();
                                ToastUtils.showTip("设置车型为" + spinnerItem.getValue() + "失败，请重试");
                            });
            OBDStream.getInstance().exec("ATSETVEHICLE=" + spinnerItem.getID());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openDial() {
        InstallerUtils.openApp(this, "com.android.dialer");
        EventBus.getDefault().post(NaviEvent.FloatButtonEvent.SHOW);
    }

    private void oepnBrower() {
        InstallerUtils.openApp(this, "com.UCMobile");
        EventBus.getDefault().post(NaviEvent.FloatButtonEvent.SHOW);
    }

    private void openRecord() {
        InstallerUtils.openApp(this, "com.android.soundrecorder");
        EventBus.getDefault().post(NaviEvent.FloatButtonEvent.SHOW);
    }

    private void openFactoryTest() {
        PackageManager packageManager = getPackageManager();
        startActivity(new Intent(packageManager.getLaunchIntentForPackage("com.qualcomm.factory")));
        EventBus.getDefault().post(NaviEvent.FloatButtonEvent.SHOW);
    }

    private void closeWifi() {
        WifiApAdmin.closeWifiAp(this);
        btnCloseWifi.setText("热点已关闭");
    }

    private void closeVoice() {
        VoiceManagerProxy.getInstance().onDestroy();
        btnCloseVoice.setText("语音已关闭");
    }

    private void openAdb() {
        com.dudu.android.hideapi.SystemPropertiesProxy.getInstance().
                set(this, "persist.sys.usb.config", "diag,serial_smd,rmnet_bam,adb");
        UsbControl.setToClient();
    }

    private void openMusic() {
        InstallerUtils.openApp(this, "com.tencent.qqmusic");
        EventBus.getDefault().post(NaviEvent.FloatButtonEvent.SHOW);
    }

    private void openCamera() {
        FrontDriveVideoService.getInstance().release();
        CameraInstance.getInstance().stopCamera();
        InstallerUtils.openApp(this, "com.android.camera2");
        EventBus.getDefault().post(NaviEvent.FloatButtonEvent.SHOW);
    }

    private void openGps() {
        InstallerUtils.openApp(this, "com.chartcross.gpstestplus");
        EventBus.getDefault().post(NaviEvent.FloatButtonEvent.SHOW);
    }

    private void openMap() {
        InstallerUtils.openApp(this, "org.gyh.rmaps");
        EventBus.getDefault().post(NaviEvent.FloatButtonEvent.SHOW);
    }

    private void changeIp() {

        if (TextUtils.isEmpty(editText_ip.getText().toString())
                || TextUtils.isEmpty(editText_port.getText().toString())
                || TextUtils.isEmpty(editText_Testip.getText().toString())
                || TextUtils.isEmpty(editText_Testport.getText().toString())) {
            return;
        }

        ip = editText_ip.getText().toString();
        port = Integer.parseInt(editText_port.getText().toString());
        testIP = editText_Testip.getText().toString();
        testPort = Integer.parseInt(editText_Testport.getText().toString());
        String userName = editText_UserName.getText().toString();

        ReceiverDataFlow.getInstance().reConnect();

        if (ipConfig.changeConfig(ip, testIP, port, testPort, isTest)) {
            //修改成功

            if (isTest) {
                ip = testIP;
                port = testPort;
            }
            Observable.timer(5, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
                @Override
                public void call(Long aLong) {
                    NetworkManage.getInstance().getConnectionParam().setPort(port);
                    NetworkManage.getInstance().getConnectionParam().setHost(ip);
                    NetworkManage.getInstance().reConnect();
                }
            }, throwable -> Log.e("DebugActivity", "changeIp: ", throwable));
        }
        startActivity(new Intent(DebugActivity.this, MainRecordActivity.class));
        finish();
    }

    private void reset() {
        editText_ip.setText(ipConfig.getServerIP());
        editText_port.setText(ipConfig.getServerPort() + "");
        editText_Testip.setText(ipConfig.getTestServerIP());
        editText_Testport.setText(ipConfig.getTestServerPort() + "");
        if (isTest) {
            radioBtnTest.setChecked(true);
        } else {
            radioBtnFormal.setChecked(true);
        }
    }

    public void enterSetting(View view) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings"));
        startActivity(intent);
        EventBus.getDefault().post(NaviEvent.FloatButtonEvent.SHOW);
    }

    public void startFactory(View view) {
        Observable
                .just(stopFunctions())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(l -> {
                    PackageManager packageManager = getPackageManager();
                    startActivity(new Intent(packageManager.getLaunchIntentForPackage("com.qualcomm.factory")));
                }, throwable -> {
                });
    }

    private boolean stopFunctions() {
        ACCReceiver.isFactroyIng = true;
        //关闭语音
//        VoiceManagerProxy.getInstance().stopUnderstanding();
//        VoiceManagerProxy.getInstance().stopWakeup();
        VoiceManagerProxy.getInstance().onDestroy();

        //关闭Portal
        com.dudu.android.hideapi.SystemPropertiesProxy.getInstance().set(this, "persist.sys.nodog", "stop");

        onStopFrontVideo(null);


        SurfaceWindow.hideWindowDialog();

        //关闭热点
        WifiApAdmin.closeWifiAp(this);

        //stop bluetooth
        ObdManage.getInstance().release();
        OBDStream.getInstance().obdStreamClose();
        TirePressureManage.getInstance().release();
        TpmsStream.getInstance().tpmsStreamClose();


        FrontCameraManage.getInstance().release();
        RearCameraManage.getInstance().release();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().post(NaviEvent.FloatButtonEvent.HIDE);
    }

    public void switchUSBCamera(View view) {
        UsbControl.setToHost();
//        ObservableFactory.getInstance().getCommonObservable().startRearPreview();
//        startActivity(new Intent(DebugActivity.this, MainRecordActivity.class));
//        finish();
    }

    public void switchADB(View view) {
        UsbControl.setToClient();
//        ObservableFactory.getInstance().getCommonObservable().stopRearPreview();
//        startActivity(new Intent(DebugActivity.this, MainRecordActivity.class));
//        finish();
    }

    public void onStopFrontVideo(View view) {
//        btnCloseVideo.setText("录像已关闭");
        LoggerFactory.getLogger("video.frontdrivevideo").info("关闭前置录像");
        FrontCameraManage.getInstance().stopRecord();
    }

    public void onStartFrontVideo(View view) {
//        btnStartVideo.setText("录像已开启");
        LoggerFactory.getLogger("video.frontdrivevideo").info("开启前置录像");
        FrontCameraManage.getInstance().startRecord();
    }

    public void onInitFontCamera(View view) {
        FrontCameraManage.getInstance().init();
    }

    public void onReleaseFontCamera(View view) {
        FrontCameraManage.getInstance().release();
    }

    public void startUSBCameraRecord(View view) {
        //后门开启后置摄像头
        ToastUtils.showToast("开启后置录像");
        RearCameraManage.getInstance().startRecord();
    }

    public void stopUSBCameraRecord(View view) {
        if (view != null) {
            ToastUtils.showToast("停止后置录像");
        }
        RearCameraManage.getInstance().stopRecord();
    }

    /**
     * 清理realm数据库
     */
    public void cleanRealm(View view) {
        RealmManage.cleanRealm();
        cleanVideo(view);
        cleanPhoto(view);
    }

    public void testActivity(View view) {
        Context context = view.getContext();
        context.startActivity(new Intent(context, MonitorActivity.class));
    }

    /**
     * 清理前后置视频,包括缩略图
     */
    public void cleanVideo(View view) {
        Rx.base("", s -> {
            FileUtil.delectAllFiles(new File("/storage/sdcard1/dudu/video"));
            FileUtil.delectAllFiles(new File("/storage/sdcard1/dudu/thumbnail"));
            FileUtil.delectAllFiles(new File("/storage/sdcard1/dudu/frontVideo"));
            FileUtil.delectAllFiles(new File("/storage/sdcard1/dudu/frontVideoThumbnail"));

            FileUtil.delectAllFiles(new File("/storage/sdcard1/dudu/rearVideo"));
            FileUtil.delectAllFiles(new File("/storage/sdcard1/dudu/rearVideoThumbnail"));
            return "";
        });
    }

    /**
     * 清理前后置图片
     */
    public void cleanPhoto(View view) {
        Rx.base("", s -> {
            FileUtil.delectAllFiles(new File("/storage/sdcard1/dudu/photos"));
            FileUtil.delectAllFiles(new File("/storage/sdcard1/dudu/frontPicture"));
            return "";
        });
    }

    @Override
    protected void onDestroy() {
        if (tpmsSub != null) tpmsSub.unsubscribe();
        if (tpmsPairSub != null) tpmsPairSub.unsubscribe();
        super.onDestroy();
    }

    public void tpms6601(View view) {
        TPMSFlow.TPMSPairStart(TPMSInfo.POSITION.RIGHT_FRONT);
    }

    public void tpms6602(View view) {
        TPMSFlow.TPMSPairStart(TPMSInfo.POSITION.LEFT_FRONT);
    }

    public void tpms6603(View view) {
        TPMSFlow.TPMSPairStart(TPMSInfo.POSITION.RIGHT_BACK);
    }

    public void tpms6604(View view) {
        TPMSFlow.TPMSPairStart(TPMSInfo.POSITION.LEFT_BACK);
    }

    public void tpms11(View view) {
        byte[] cmd = {(byte) 0xAA, 0x41, (byte) 0xA1, 0x07, 0x11, 0x00};
        TpmsStream.getInstance().write(cmd);
    }

    public void tpmsSub(View view) {
        tpmsSub = TPMSFlow.TPMSWarnInfoStream()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tpmsWarnInfo -> {
                    Log.d("TPMS", "info: " + tpmsWarnInfo);
                    tpmsPrint.append(tpmsWarnInfo.toString());
                }, throwable -> Log.e("DebugActivity", "tpmsSub: ", throwable));

        tpmsPairSub = TPMSFlow.TPMSPairStream()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    Log.d("TPMS", "pair: " + pair);
                    tpmsPrint.append(pair + "对码成功\n");
                }, throwable -> Log.e("DebugActivity", "tpmsSub: ", throwable));

    }

    public void tpmsUnSub(View view) {
        if (tpmsSub != null) tpmsSub.unsubscribe();
        if (tpmsPairSub != null) tpmsPairSub.unsubscribe();
    }

    public void switchDSDS(View view) {
        SystemPropertiesProxy.getInstance().set(this, "persist.radio.multisim.config", "dsds");
    }

    public void switchSSSS(View view) {
        SystemPropertiesProxy.getInstance().set(this, "persist.radio.multisim.config", "ssss");
    }

    public void obdfwVersion(View view) {
        ToastUtils.showTip("读取obd版本号：" + ObdUpdateService.getInstance().getObdVersion());
    }

    public void obdReset(View view) {
        OBDStream.getInstance().obdStreamClose();
        ObdUpdateCmd.resetObdChip();
        OBDStream.getInstance().init();
        ObdManage.getInstance().init();
        try {
            OBDStream.getInstance().exec("ATRON");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fdOpen(View view) {
        CarLock.lockCar();
    }

    public void fdClose(View view) {
        CarLock.unlockCar();
    }

    public void onReboot(View view) {
        com.dudu.android.hideapi.SystemPropertiesProxy.getInstance().set(this, "persist.sys.boot", "reboot");
    }

    public void onCrash(View view) {
        int a = 10;
        int b = a / 0;
    }

    public void onFireOn(View view) {
        CarStatusUtils.isDemo = true;
        CarStatusUtils.isDemoFired = true;

        Intent intent = new Intent("android.intent.action.ACC_ON");
        intent.putExtra("fired", true);
        sendBroadcast(intent);
//        finish();
    }

    public void onFireOff(View view) {
        CarStatusUtils.isDemo = true;
        CarStatusUtils.isDemoFired = false;

        Intent intent = new Intent("android.intent.action.ACC_ON");
        intent.putExtra("fired", false);
        sendBroadcast(intent);
//        finish();
    }

    public void closeFireMoni(View view) {
        CarStatusUtils.isDemo = false;
        CarStatusUtils.isDemoFired = false;
    }

    public void onQueryACCVol(View view) {
        OBDStream.getInstance().accVoltageStream()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                            ToastUtils.showTip("ACC电压:" + s);
                        }
                        , Throwable::printStackTrace);
        try {
            OBDStream.getInstance().exec("ATGETVOL");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onQueryOBDCarModel(View view) {
        OBDStream.getInstance().OBDGetCarType()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                            ToastUtils.showTip("OBD已设置车型:" + obdCarMap.get(Integer.parseInt(s)));
                        }
                        , Throwable::printStackTrace);
        try {
            OBDStream.getInstance().exec("ATGETVEHICLE");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onQueryActive(View view) {
        RequestFactory
                .getActiveRequest()
                .checkDeviceActive(new com.dudu.rest.model.active.CheckDeviceActive())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(requestResponse -> {
                    if (requestResponse != null) {
                        if (requestResponse.resultCode == 40019) {
                            ToastUtils.showToast("设备已经激活了");
                        } else {
                            ToastUtils.showTip(requestResponse.resultMsg);
                        }
                    }
                }, throwable -> {
                    ToastUtils.showTip("检查出错，请查看网络");
                });
    }

    public void onQueryCarType(View view) {
        RequestFactory.getDrivingRequest()
                .getCarBrand()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getCarBrandResponse -> {
                    if (getCarBrandResponse != null) {
                        switch (getCarBrandResponse.result.audit_state) {
                            case GetCarBrandResponse.AUDIT_STATE_UNAUDITED:
                                ToastUtils.showToast(R.string.fault_code_clear_fail_upload_license);
                                break;
                            case GetCarBrandResponse.AUDIT_STATE_AUDITING:
                                ToastUtils.showToast(R.string.fault_code_clear_fail_wait_checking);
                                break;
                            case GetCarBrandResponse.AUDIT_STATE_AUDITED:
                                ToastUtils.showToast(getCarBrandResponse.result.brand);
                                DataFlowFactory
                                        .getUserMessageFlow()
                                        .saveCarType(getCarBrandResponse.result);
                                break;
                            case GetCarBrandResponse.AUDIT_STATE_REJECT:
                                ToastUtils.showToast(R.string.fault_code_clear_fail_reject);
                                break;
                        }
                    }
                }, throwable -> {
                    ToastUtils.showToast("检查出错，请查看网络");
                });
    }

    public void onQueryBLValue(View view) {
        ToastUtils.showToast("背光值:" + Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0));
    }

    public void onQueryLightValue(View view) {
        SensorManage.getInstance(this).initLightSensorManage();
        ToastUtils.showToast("光感强度:" + SensorManage.getInstance(this).getLux());
    }

    public void onBluetoothPhonePullBook(View view) {
        //广播给蓝牙服务获取通讯录
        Intent intent = new Intent(Constants.BLUETOOTH_PULL_PHONE_BOOK_BEGIN);
        sendBroadcast(intent);
    }

    public void testBackCarPreview(View view) {
        test(0, true);
        test(30, false);
    }

    private void test(int timeDelaySeconds, boolean backFlag) {
        Observable
                .timer(timeDelaySeconds, TimeUnit.SECONDS, Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(l -> {
                    sendBackCarBroadcast(CommonLib.getInstance().getContext(), backFlag);
                }, throwable -> Log.e("DebugActivity", "test:", throwable));
    }

    private void sendBackCarBroadcast(Context context, boolean backFlag) {
        Intent intent = new Intent("android.intent.action.ACC_BL");
        intent.putExtra("backed", backFlag);
        context.sendBroadcast(intent);
    }

    public void obdRTClose(View view) {
        try {
            OBDStream.getInstance().exec("ATROFF");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void obdRTOpen(View view) {
        try {
            OBDStream.getInstance().exec("ATRON");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveVoice(View view) {
        LauncherApplication.getContext().setNeedSaveVoice(true);
        VoiceManagerProxy.getInstance().onDestroy();
        VoiceManagerProxy.getInstance().onInit();
    }

    public void queryObdVersionInfo(View view) {
        ObdUpdateService.getInstance().delayQueryServerVersion(0);
    }

    public void obdDownload(View view) {
        if (ObdUpdateService.getInstance().isUpdateIng()) {
            ToastUtils.showToast("obdBin正在升级，请等待升级完成");
        }
        ObdUpdateService.getInstance().updateObdBin();
    }

    public void hardUpdateObdBin(View view) {
        ObdUpdateService.getInstance().hardUpdateObdbin();
    }

    public void sendPushMessage(View view) {
        ReceiverPushData receiverPushData = new ReceiverPushData();
        receiverPushData.resultCode = 0;
        ReceiverPushData.ReceivedDataResult receivedDataResult = new ReceiverPushData.ReceivedDataResult();
        receivedDataResult.method = PushParams.LAUNCHER_UPGRADE;
        receiverPushData.result = receivedDataResult;

        EventBus.getDefault().post(receiverPushData);
    }

    public void onSetD02(View view) {
        com.dudu.android.hideapi.SystemPropertiesProxy.getInstance().set(this, "persist.sys.model", "d02");
        onCrash(null);
    }

    public void onSetD03(View view) {
        com.dudu.android.hideapi.SystemPropertiesProxy.getInstance().set(this, "persist.sys.model", "d03");
        onCrash(null);
    }

    public void setGaodeminiMap(View view) {
        com.dudu.android.hideapi.SystemPropertiesProxy.getInstance().set(this, "persist.sys.amap.ver", "amap");
    }

    public void initMicView(TextView view) {
        if (isMultiMic()) {
            view.setText("关闭Mic复用(重启生效)");
        } else {
            view.setText("启用Mic复用(重启生效)");
        }
    }

    public void initLogcatEnable(TextView view) {
        if (isLogcatEnable()) {
            view.setText("关闭Logcat");
        } else {
            view.setText("开启Logcat");
        }
    }

    public void initStreamView(TextView view) {
        if (CameraControl.isStream()) {
            view.setText("关闭推流");
        } else {
            view.setText("开启推流");
        }
    }

    public void initRecordView(TextView view) {
        if (FrontCameraManage.getInstance().isRecording()) {
            view.setText("关闭前置录像");
        } else {
            view.setText("开启前置录像");
        }
    }

    public void setMicMulti(View view) {
        if (isMultiMic()) {
            com.dudu.android.hideapi.SystemPropertiesProxy.getInstance().set(this, "persist.sys.mic.multi", "0");
            ((TextView) view).setText("开启Mic复用(重启生效)");
        } else {
            com.dudu.android.hideapi.SystemPropertiesProxy.getInstance().set(this, "persist.sys.mic.multi", "1");
            ((TextView) view).setText("关闭Mic复用(重启生效)");
        }
    }

    public void setLogcatEnable(View view) {
        if (isLogcatEnable()) {
            com.dudu.android.hideapi.SystemPropertiesProxy.getInstance().set(this, "persist.sys.logcat.enable", "0");
            ((TextView) view).setText("开启Logcat");
        } else {
            com.dudu.android.hideapi.SystemPropertiesProxy.getInstance().set(this, "persist.sys.logcat.enable", "1");
            ((TextView) view).setText("关闭Logcat");
        }
    }

    public void setLibStream(View view) {
//        if (CameraControl.isStream()) {
//            CameraControl.instance().setStreamState(false);
//            ((TextView) view).setText("开启推流");
//        } else {
//            CameraControl.instance().setStreamState(true);
//            ((TextView) view).setText("关闭推流");
//        }
        StreamActivity.launch(this);
    }

    public void setStartRecorder(View view) {
        if (FrontCameraManage.getInstance().isRecording()) {
            FrontCameraManage.getInstance().stopRecord();
            ((TextView) view).setText("开启前置录像");
        } else {
            FrontCameraManage.getInstance().startRecord();
            ((TextView) view).setText("关闭前置录像");
        }
    }

    public void setGaodeAuto(View view) {
        com.dudu.android.hideapi.SystemPropertiesProxy.getInstance().set(this, "persist.sys.amap.ver", "auto");
    }

    public void obdSleep(View view) {
        try {
            OBDStream.getInstance().exec("ATENTERSLEEP");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openWebSocket(View view) {
        ReceiverDataFlow.getInstance().init();
    }

    public void closeWebSocket(View view) {
        ReceiverDataFlow.getInstance().release();
    }

    public void initNetwork(View view) {
        NetworkManage.getInstance().init();
    }

    public void releaseNetwork(View view) {
        NetworkManage.getInstance().release();
    }

    public void obdWake(View view) {
        ObdGpioControl.wakeObd();
    }

    public void obdPowerOn(View view) {
        ObdGpioControl.powerOnObd();
    }

    public void obdPowerOff(View view) {
        ObdGpioControl.powerOffObd();
    }


    public void testStorageSpace(View view) {
        StorageSpaceService.getInstance().testStorageSpaceService();
    }
}
