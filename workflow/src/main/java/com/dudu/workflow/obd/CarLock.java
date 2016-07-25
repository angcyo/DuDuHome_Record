package com.dudu.workflow.obd;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.dudu.android.libble.BleConnectMain;
import com.dudu.commonlib.utils.shell.ShellExe;

import org.scf4a.ConnSession;
import org.scf4a.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;

public class CarLock {
    private static Logger log = LoggerFactory.getLogger("CarLock");
    private static CarLock ourInstance = new CarLock();

    public static CarLock getInstance() {
        return ourInstance;
    }

    public CarLock() {
    }

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mWriteChara;
    private BluetoothDevice mBluetoothDevice;

    @Deprecated
    public void init(Context context) {
        ConnSession.getInstance();
        BleConnectMain.getInstance().init(context);
        EventBus.getDefault().register(this);
        EventBus.getDefault().post(new Event.StartScanner(Event.ConnectType.BLE));
    }

    public void uninit(Context context) {
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(Event.BackScanResult event) {
        if (event.getType() != Event.ConnectType.BLE)
            return;
        BluetoothDevice device = event.getDevice();
        log.debug("ble try Connect {}[{}]", device.getName(), device.getAddress());
        EventBus.getDefault().post(new Event.Connect(device.getAddress(), Event.ConnectType.BLE, false));
    }


    public void onEvent(Event.BLEInit event) {
        log.debug("ble BLEInit");
        mBluetoothGatt = event.getBluetoothGatt();
        mWriteChara = event.getWriteChara();

        mBluetoothDevice = event.getDevice();
        final String devAddr = mBluetoothDevice.getAddress();
    }

    public static final String LOCK_CAR = "echo 1 > /sys/bus/platform/devices/obd_gpio.68/anti_burglary_enable";
    public static final String UNLOCK_CAR = "echo 0 > /sys/bus/platform/devices/obd_gpio.68/anti_burglary_enable";

    public static void lockCar() {
        log.debug("lockCar");
        ShellExe.execShellCmd(LOCK_CAR);
    }

    public static void unlockCar() {
        log.debug("unlockCar");
        ShellExe.execShellCmd(UNLOCK_CAR);
    }
}
