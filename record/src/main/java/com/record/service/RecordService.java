package com.record.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;

import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.drivevideo.CheckSizeEvent;
import com.dudu.drivevideo.MsgEvent;
import com.dudu.drivevideo.rearcamera.utils.TempFileCheck;
import com.dudu.drivevideo.spaceguard.SpaceCheck;
import com.dudu.drivevideo.spaceguard.event.VideoSpaceEvent;
import com.record.MainActivity;
import com.record.control.ConvertControl;
import com.record.control.RecordCheck;
import com.record.control.RecordControl;
import com.record.control.RecordImpl;
import com.record.control.RecordTimeControl;
import com.record.control.WatermarkControl;
import com.record.event.RecordEvent;
import com.record.event.RecordFailedEvent;
import com.record.util.Debug;
import com.record.util.RUtil;
import com.record.view.RecordFailedWindow;
import com.record.view.SpaceTipWindow;

import de.greenrobot.event.EventBus;

public class RecordService extends Service {
    public static final String STOP_RECORD = "stop";//保存使能状态
    public static final String STOP_RECORD_NO = "stop_no";//不保存使能状态
    public static final String START_RECORD_NO = "start_no";
    public static final String START_RECORD = "start";
    public static final String ERROR_RECORD = "SD Card Not Found";
    public static final String ERROR_NO_DEVICE = "Device Not Found";
    public static final String GET_STATE = "state";

    private static int color = Color.YELLOW;

    RecordFailedWindow mRecordFailedWindow;
    private long commandTime = 0l;

    public static void start(Context context) {
        Intent intent = new Intent("com.record", Uri.parse("record:angcyo"));
        context.startService(intent);
    }

    public static void sendRecordState(Context context, String state) {
        InfoBean infoBean = new InfoBean();
        infoBean.record_state = state;
        Intent intent = new Intent("com.record.info", Uri.parse("info:" + infoBean.toString()));
        context.startService(intent);

        EventBus.getDefault().post(new MsgEvent("录像状态同步:" + state));
    }

    //启动界面,提示信息
    public static void startMainActivity(Context context) {
        final Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Debug.show(this, "Service onCreate", color);

        EventBus.getDefault().register(this);

        //初始化是否自动转换
        ConvertControl.init();

        //转换异常退出的h264文件
//        H264Check.start();

        //启用录像节点检查
        RecordCheck.instance(this.getApplicationContext());

        //水印
        WatermarkControl.initWatermark();

        //安装状态监测
        RecordControl.init(this);

        //开启磁盘空间管理服务.
        StorageServiceManager.getInstance().startStorageService();
        RecordImpl.instance().init();//初始化录像
        RecordTimeControl.setRecordTime();//初始化录像时长

//        RecordFailedHelper.showFailedDialog(this);

        mRecordFailedWindow = new RecordFailedWindow(this).setContent("录像异常中断，请重新连接车充恢复。");

        check8GCard();

        if (SpaceCheck.isRecordFail()) {
            mRecordFailedWindow.show();
        }
    }

    private void check8GCard() {
        if (SpaceCheck.isAbove8G()) {
            if (!SpaceCheck.canRecord()) {
                SpaceTipWindow.showTip(this, "存储空间不足,已停止录像");
            }
        } else {
            //没有8G
            startMainActivity(this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Debug.show(this, "Service onStartCommand", color);

        if (intent != null) {
            handleCommand(intent);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Debug.show(this, "Service onDestroy", color);

//        closeRecord();
        RecordCheck.instance(this.getApplicationContext()).quit();
        StorageServiceManager.getInstance().stopStorageService();

        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    private void handleCommand(Intent intent) {
        try {
            String dataString = intent.getDataString();
            String command = dataString.split("record:")[1];

            if (!TextUtils.isEmpty(command)) {
                onCommand(command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onCommand(String command) {
        EventBus.getDefault().post(new MsgEvent("收到命令:" + command));
        final long nowTime = System.currentTimeMillis();

        if ((nowTime - commandTime) < 500) {
            //命令过快
            updateRecordState();
            return;
        }
        commandTime = nowTime;

        if (command.contains(START_RECORD_NO)) {
            startRecord(false);
        } else if (command.contains(STOP_RECORD_NO)) {
            closeRecord(false);
        } else if (command.contains(START_RECORD)) {
            startRecord(true);
        } else if (command.contains(STOP_RECORD)) {
            closeRecord(true);
        } else if (command.contains(GET_STATE)) {
            updateRecordState();
        }
    }

    private void updateRecordState() {
        if (RecordControl.isRecord()) {
            sendRecordState(this, START_RECORD);
        } else {
            sendRecordState(this, STOP_RECORD);
        }
    }

    private void closeRecord(boolean saveState) {
        RecordImpl.instance().stopRecord();
        sendRecordState(this, STOP_RECORD);
        EventBus.getDefault().post(new RecordEvent(false));
        RecordControl.saveRecordState(this, 0);

        if (saveState) {
            RecordControl.setRecordEnable(false);
        }
    }

    /**
     * 开始录像, 录像检查
     */
    private void startRecord(boolean saveState) {
        //检查T卡
        if (FileUtil.isTFlashCardExists()) {
            //检查是否是8G以上容量的卡
            if (SpaceCheck.isAbove8G()) {
                //检查录像节点, 空间大小
                if (RUtil.canRecord() && SpaceCheck.canRecord()) {
                    RecordImpl.instance().startRecord();
                    sendRecordState(this, START_RECORD);
                    EventBus.getDefault().post(new RecordEvent(true));
                    RecordControl.saveRecordState(this, 1);
                    if (saveState) {
                        RecordControl.setRecordEnable(true);
                    }
                } else {
                    sendRecordState(this, ERROR_NO_DEVICE);
                    EventBus.getDefault().post(new RecordEvent(false));
                    RecordControl.saveRecordState(this, 0);
                    if (saveState) {
                        RecordControl.setRecordEnable(false);
                    }
                    startMainActivity(this);
                }
            } else {
                sendRecordState(this, ERROR_NO_DEVICE);
                EventBus.getDefault().post(new RecordEvent(false));
                if (saveState) {
                    RecordControl.setRecordEnable(false);
                }
                startMainActivity(this);
            }
        } else {
            sendRecordState(this, ERROR_RECORD);
            EventBus.getDefault().post(new RecordEvent(false));
            RecordControl.saveRecordState(this, 0);

            if (saveState) {
                RecordControl.setRecordEnable(false);
            }
            startMainActivity(this);
        }
    }

    /**
     * 空间不足事件
     */
    public void onEventMainThread(VideoSpaceEvent event) {
        SpaceTipWindow.showTip(this, "存储空间不足,已停止录像");
//        startMainActivity(this);
    }

    /**
     * 录像失败事件,比如摄像头被拔插(需要转换当前正在录制的文件)
     */
    public void onEventMainThread(RecordFailedEvent event) {
        Debug.show(this, "RecordFailedEvent 检查临时文件.", color);
        TempFileCheck.start();//检查临时文件
        startMainActivity(this);
    }

    /**
     * 调试信息
     */
    public void onEventMainThread(MsgEvent event) {
        Debug.show(this, event.getMsg());
    }

    /**
     * 录像文件大小5秒内,没有改变
     */
    public void onEventMainThread(CheckSizeEvent event) {
        Debug.show(this, "CheckSizeEvent:" + event.isSizeChange(), color);
        if (event.isSizeChange()) {
        } else {
//            RecordFailedHelper.showFailedDialog(this);
            mRecordFailedWindow.show();

//            SpaceCheck.createRecordFailFlag();

//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.exit(1);
//            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

}
