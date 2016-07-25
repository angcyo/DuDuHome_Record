package com.dudu.monitor;

import com.amap.api.location.AMapLocation;
import com.dudu.monitor.active.ActiveDeviceManage;
import com.dudu.monitor.event.CarStatus;
import com.dudu.monitor.flow.FlowManage;
import com.dudu.monitor.obd.ObdManage;
import com.dudu.monitor.portal.PortalManage;
import com.dudu.monitor.repo.location.LocationManage;
import com.dudu.monitor.tirepressure.TirePressureManage;
import com.dudu.monitor.valueobject.LocationInfo;
import com.dudu.monitor.valueobject.SensorData;

import java.util.List;

/**
 * Created by dengjun on 2015/11/25.
 * Description :
 */
public class Monitor {
    private static Monitor instance = null;

    private LocationManage mLocationManage;
    private ObdManage obdManage;
    private ActiveDeviceManage activeDeviceManage;

    public static Monitor getInstance() {
        if (instance == null) {
            synchronized (Monitor.class) {
                if (instance == null) {
                    instance = new Monitor();
                }
            }
        }
        return instance;
    }

    private Monitor() {
        mLocationManage = LocationManage.getInstance();
//        mSensorManage = SensorManage.getInstance(mContext);
        activeDeviceManage = ActiveDeviceManage.getInstance();

        obdManage = ObdManage.getInstance();
    }

    public void startWork() {
        mLocationManage.startSendLocation();
        activeDeviceManage.init();

        FlowManage.getInstance().init();
        PortalManage.getInstance().init();

        TirePressureManage.getInstance().init();
    }

    public void stopWork() {
        mLocationManage.release();
        activeDeviceManage.release();

        FlowManage.getInstance().release();
        PortalManage.getInstance().release();

        TirePressureManage.getInstance().release();
    }

    //获取高德定位未过滤位置数据
    public AMapLocation getCurrentLocation() {
        return mLocationManage.getCurrentLocation();
    }

    //获取当前的位置信息，已过滤
    public LocationInfo getCurLocation() {
        return mLocationManage.getCurLocation();
    }

    public List<SensorData> getSensorDataList(int sensorType) {
        /*if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            return mSensorManage.getmAcceSensorList();
        } else if (sensorType == Sensor.TYPE_GYROSCOPE) {
            return mSensorManage.getmGyroscopSensorList();
        }*/
        return null;
    }

    //获取当前车速
    public int getCurSpeed() {
        return obdManage.getCurSpeed();
    }

    //获取当前转速
    public float getCurRpm() {
        return obdManage.getCurRpm();
    }

    public float getCur_batteryV() {
        return obdManage.getCur_batteryV();
    }

    /* 设备是否激活了*/
    public boolean isDeviceActived() {
        if (activeDeviceManage.getActiveState() == ActiveDeviceManage.ACTIVE_OK) {
            return true;
        } else {
            return false;
        }
    }

    public CarStatus getCarStatus() {
        return obdManage.getCarStatus();
    }
}
