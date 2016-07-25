package com.dudu.android.launcher.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.utils.BtPhoneUtils;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.android.launcher.utils.DeviceIDUtil;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;
import com.dudu.event.DeviceEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.greenrobot.event.EventBus;
import wld.btphone.bluetooth.aidl.PbapService;

/**
 * Created by Administrator on 2016/1/20.
 */
public class BluetoothService extends Service {

    private BluetoothAdapter mAdapter;

    private PbapService mPbapService;

    private List<String> mDevices;//已经配对的设备地址

    Set<BluetoothDevice> mFondDevices = new HashSet<>();//发现附近的设备
    private Logger logger = LoggerFactory.getLogger("phone.BluetoothService");

    private int log_step;

    @Override
    public void onCreate() {
        super.onCreate();

        logger.debug("[{}]BluetoothService onCreate()...",log_step++);
        registerReceiverForBluetoothPhone();
        initService();
    }

    private void initService(){

        initBlueAdapter();
        onBindService();
    }
    /**
     * 初始化蓝牙适配器
     */
    private void initBlueAdapter(){
        mAdapter = BluetoothAdapter.getDefaultAdapter();

        if (null != mAdapter) {
            if(!mAdapter.isEnabled()){
                mAdapter.enable();
            }
            if (mAdapter.isDiscovering()) {
                mAdapter.cancelDiscovery();
            }
        }

        //设置蓝牙可见性 全天
        int time = 0;//24 * 60 * 60 * 1000;
        BtPhoneUtils.setDiscoverableTimeout(time);

        //修改蓝牙设备名称
        String imei = DeviceIDUtil.getIMEI(LauncherApplication.getContext());
        if(!TextUtils.isEmpty(imei) && imei.length()>3){
            String temp = "AIO Car "+imei.substring(imei.length()-4,imei.length());
            BtPhoneUtils.setBluetoothDeviceName(temp);
        }
    }

    /**
     * 获取绑定的蓝牙设备地址
     */
    private void getBondedDevices() {
        if (null == mDevices) {
            mDevices = new ArrayList<>();
        } else {
            mDevices.clear();
        }
        if (null != mAdapter) {
            Set<BluetoothDevice> bondedDevices = mAdapter.getBondedDevices();
            if (null != bondedDevices && bondedDevices.size() > 0) {

                for (BluetoothDevice device : bondedDevices) {
                    logger.debug("已配对设备： " + device.getAddress());
                    mDevices.add(device.getAddress());
                }

                //开始查找附近的蓝牙设备
                startFindDevices();
            } else {
                logger.debug("[phone][{}]已配对设备：无", log_step++);
                connectLastDevice();
            }
        }
    }

    private void startFindDevices(){
        if (null != mAdapter) {
            logger.debug("[phone][{}]启动蓝牙搜索附近的设备", log_step++);
            mAdapter.startDiscovery();
        }
    }
    /**
     * 判断该蓝牙设备地址是否存在于已配对设备地址列表里
     *
     * @param address
     * @return
     */
    private boolean existBtAddress(String address) {
        if (null != mDevices) {
            for (String dev : mDevices) {
                if (dev.equals(address)) {
                    logger.debug("最后一次连接的蓝牙设备在已配对设备列表中");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * BindService 绑定蓝牙电话底层服务
     */
    private void onBindService() {
        Intent intent = new Intent("wld.btphone.bluetooth.ProfileService");
        bindService(intent, mPbapServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.debug("[{}]BluetoothService onStartCommand().",log_step++);

        return START_STICKY;//super.onStartCommand(intent, flags, startId);//START_REDELIVER_INTENT
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.debug("[{}]BluetoothService onDestroy().",log_step++);

        try {
            unbindService(mPbapServiceConnection);
            unregisterReceiver(mBluetoothPhoneReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private ServiceConnection mPbapServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPbapService = PbapService.Stub.asInterface(service);
            logger.debug("[{}]连接蓝牙电话底层服务成功.mPbapService:" + mPbapService,log_step++);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (null != mAdapter) {
                        if (!mAdapter.isEnabled()) {
                            mAdapter.enable();
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    getBondedDevices();
                    //设置蓝牙可见性
                    int time = 0;//24 * 60 * 60 * 1000;
                    BtPhoneUtils.setDiscoverableTimeout(time);
//                    //缓存本次蓝牙地址作为下次自动连接默认地址
//                    String lastAddr = SharedPreferencesUtil.getStringValue(LauncherApplication.getContext(),
//                            Constants.KEY_LAST_BLUETOOTH_CLIENT_ADDRESS,"");
//                    if(!TextUtils.isEmpty(lastAddr)){
//                        setDevice(lastAddr);
//                    }
                }
            }).start();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            mPbapService = null;
            logger.debug("[{}]蓝牙电话底层服务已断开.",log_step++);
        }
    };

    /**
     * 注册广播接收器
     */
    private void registerReceiverForBluetoothPhone() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BLUETOOTH_SHOW_CONNECT_FAIL);
        intentFilter.addAction(Constants.BLUETOOTH_SHOW_WAITDIALOG);
        intentFilter.addAction(Constants.BLUETOOTH_DISMISS_WAITDIALOG);
        intentFilter.addAction(Constants.BLUETOOTH_PULL_PHONE_BOOK);
        intentFilter.addAction(Constants.BLUETOOTH_ACL_DISCONNECTED);
        intentFilter.addAction(Constants.ACTION_NEW_BLUETOOTH_DEVICE);
        intentFilter.addAction(Constants.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(Constants.ACTION_AG_CALL_CHANGED);
        intentFilter.addAction(Constants.ACTION_BLUETOOTH_PHONE_END);
        intentFilter.addAction(Constants.BLUETOOTH_DEL_PHONE_BOOK_BEGIN);
        intentFilter.addAction(Constants.BLUETOOTH_DEL_PHONE_BOOK_END);
        intentFilter.addAction(Constants.BLUETOOTH_INSERT_PHONE_BOOK_BEGIN);
        intentFilter.addAction(Constants.BLUETOOTH_INSERT_PHONE_BOOK_END);
        intentFilter.addAction(Constants.BLUETOOTH_PULL_PHONE_BOOK_BEGIN);
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mBluetoothPhoneReceiver, intentFilter);
    }

    private BroadcastReceiver mBluetoothPhoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            logger.debug("[phone][{}]接收到蓝牙连接状态改变广播：" + action, log_step++);

            if (action.equals(Constants.BLUETOOTH_SHOW_CONNECT_FAIL)) {
                logger.debug("[phone][{}]连接失败删除旧通讯录...", log_step++);
                BtPhoneUtils.deleteContacts(context);
            } else if (action.equals(Constants.BLUETOOTH_SHOW_WAITDIALOG)) {

            } else if (action.equals(Constants.BLUETOOTH_ACL_DISCONNECTED)) {

//                if(BtPhoneUtils.btCallState == BtPhoneUtils.CALL_STATE_ACTIVE){
//                    Intent intentBtEnd = new Intent(Constants.ACTION_BLUETOOTH_PHONE_END);
//                    context.sendBroadcast(intentBtEnd);
//                }
            } else if (action.equals(Constants.BLUETOOTH_PULL_PHONE_BOOK)) {

            } else if (action.equals(Constants.ACTION_CONNECTION_STATE_CHANGED)) {
                int prevState = intent.getIntExtra(Constants.EXTRA_PREVIOUS_STATE, 0);
                int state = intent.getIntExtra(Constants.EXTRA_STATE, 0);

                BtPhoneUtils.connectionState = state;
                logger.debug("[phone][{}] prevState: " + prevState + " state: " + state, log_step++);

                if (state == BluetoothProfile.STATE_CONNECTED) {

                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null) {

                        logger.debug("[phone][{}]device name: " + device.getName() + " device address: " +
                                device.getAddress() + " device type: " + device.getType(), log_step++);

                        //缓存本次蓝牙地址作为下次自动连接默认地址
                        SharedPreferencesUtil.putStringValue(context, Constants.KEY_LAST_BLUETOOTH_CLIENT_ADDRESS,
                                device.getAddress());

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    logger.debug("[phone][{}]蓝牙通讯录同步前设置设备", log_step++);
                                    setDevice(device.getAddress());
                                    Thread.sleep(2000);
                                    BtPhoneUtils.isNeedLoadContacts = true;
                                    //延时去发起同步通讯录
                                    pullPhoneBook();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();

                        EventBus.getDefault().post(new DeviceEvent.BluetoothState(DeviceEvent.ON));
                    }
                }else if(prevState==BluetoothProfile.STATE_CONNECTED && state == BluetoothProfile.STATE_DISCONNECTED){
                    if(BtPhoneUtils.btCallState != BtPhoneUtils.CALL_STATE_TERMINATED){
                        stopBtCalling();
                    }
                    EventBus.getDefault().post(new DeviceEvent.BluetoothState(DeviceEvent.OFF));
                }
            } else if (action.equals(Constants.BLUETOOTH_DEL_PHONE_BOOK_BEGIN)) {
                logger.debug("[phone][{}]开始删除旧通讯录...", log_step++);
            } else if (action.equals(Constants.BLUETOOTH_DEL_PHONE_BOOK_END)) {
                logger.debug("[phone][{}]删除旧通讯录完成.", log_step++);
            } else if (action.equals(Constants.BLUETOOTH_PULL_PHONE_BOOK_BEGIN)) {
                logger.debug("[phone][{}]手动触发获取蓝牙通讯录...", log_step++);
                BtPhoneUtils.isNeedLoadContacts = true;
                pullPhoneBook();
            } else if (action.equals(Constants.BLUETOOTH_INSERT_PHONE_BOOK_BEGIN)) {
                logger.debug("[phone][{}]同步完成开始保存通讯录...", log_step++);
            } else if (action.equals(Constants.BLUETOOTH_INSERT_PHONE_BOOK_END)) {
                logger.debug("[phone][{}]保存通讯录完成.", log_step++);
                Intent syncEnd = new Intent(Constants.BLUETOOTH_SYNC_PHONE_BOOK_END);
                context.sendBroadcast(syncEnd);
            }else if(action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)){
                //开始设置静音模式
                AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
                //静音模式
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                logger.debug("[phone][{}]蓝牙配对请求.", log_step++);
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    try {
                        logger.debug("[phone][{}]请求设备"+device.getName()+" 的蓝牙地址"+device.getAddress(), log_step++);
                        //自动确认配对
                        device.setPairingConfirmation(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //延时恢复声音正常模式
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        //声音模式
                        if(null!=audioManager){
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        }
                    }
                }).start();
            }else if(BluetoothDevice.ACTION_FOUND.equals(action)){
                //发现附近的蓝牙设备
                if(null!=intent){
                    BluetoothDevice device = intent
                            .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // 搜索到的是已经绑定的蓝牙设备
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        logger.debug("[phone][{}]蓝牙搜索发现已经绑定的蓝牙设备name:"+device.getName()+",addr:"+device.getAddress(), log_step++);
                        mFondDevices.add(device);
                    }
                }
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                if (null != mAdapter) {
                    mAdapter.cancelDiscovery();


                    //搜索完成
                    connectLastDevice();
                }
            }
        }
    };

    /**
     * 首先连接在配对设备列表中上次连接过的设备，如果没有再尝试直接连接上次连接过的设备*/
    private void connectLastDevice(){
        String lastAddr = SharedPreferencesUtil.getStringValue(getApplicationContext(), Constants.KEY_LAST_BLUETOOTH_CLIENT_ADDRESS, "");
        for (BluetoothDevice device : mFondDevices) {
            if (device.getAddress().equals(lastAddr)) {
                logger.debug("[phone][{}]蓝牙搜索发现上次配对的设备", log_step++);

                setDevice(device.getAddress());
                return;
            }
        }

        int headset = mAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        logger.debug("BluetoothProfile.HEADSET:"+headset);
        if(headset !=  BluetoothProfile.STATE_CONNECTED && !TextUtils.isEmpty(lastAddr)){
            setDevice(lastAddr);
        }
    }

    private void stopBtCalling(){
        Intent intent = new Intent(Constants.ACTION_BLUETOOTH_PHONE_END);
        sendBroadcast(intent);
    }
    private void setDevice(String address) {

        if(!BluetoothAdapter.checkBluetoothAddress(address)){
            logger.debug("蓝牙地址无效");
            return;
        }
        Intent intent = new Intent(Constants.BLUETOOTH_SET_DEVICE);
        sendBroadcast(intent);
        try {
            if (null != mPbapService) {
                mPbapService.setDeviceStub(address);
            }else{
                Intent intent2 = new Intent(Constants.BLUETOOTH_PULL_PHONE_BOOK_FAILED);
                sendBroadcast(intent2);
            }
        } catch (RemoteException e) {
            logger.error("[{}]蓝牙通讯录设置设备失败...",log_step++);
            Intent intent3 = new Intent(Constants.BLUETOOTH_PULL_PHONE_BOOK_FAILED);
            sendBroadcast(intent3);
        }
    }

    private synchronized void pullPhoneBook() {
        try {
            if (null != mPbapService) {
                mPbapService.PullphonebookStub();
            }else{
                Intent intent2 = new Intent(Constants.BLUETOOTH_PULL_PHONE_BOOK_FAILED);
                sendBroadcast(intent2);
            }
        } catch (RemoteException e) {
            Intent intent3 = new Intent(Constants.BLUETOOTH_PULL_PHONE_BOOK_FAILED);
            sendBroadcast(intent3);
            logger.error("[{}]蓝牙获取通讯录失败...",log_step++);
        }
    }
}
