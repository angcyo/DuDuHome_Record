package com.dudu.monitor.repo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.dudu.monitor.event.CarStatus;
import com.dudu.monitor.utils.TimeUtils;
import com.dudu.monitor.valueobject.SensorData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dengjun on 2015/11/25.
 * Description :
 */
public class SensorManage implements SensorEventListener {
    private static SensorManage instance = null;
    private final Sensor mLigthSensor;

    private SensorManager mSensorManager;
    private Sensor mAcceSensor; // 加速度传感器;
    private Sensor mGyroscopSensor; // 陀螺仪

    private List<SensorData> mGyroscopSensorList;
    private List<SensorData> mAcceSensorList;
    private float lux;

    public static SensorManage getInstance(Context context) {
        if (instance == null) {
            synchronized (SensorManage.class) {
                if (instance == null) {
                    instance = new SensorManage(context);
                }
            }
        }
        return instance;
    }

    private SensorManage(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mAcceSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscopSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mLigthSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mGyroscopSensorList = Collections.synchronizedList(new ArrayList<SensorData>());
        mAcceSensorList = Collections.synchronizedList(new ArrayList<SensorData>());
    }

    public void initSensorManage() {
        mSensorManager.registerListener(this, mAcceSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscopSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mLigthSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void initLightSensorManage() {
        mSensorManager.registerListener(this, mLigthSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unInitSensorManage() {
        mSensorManager.unregisterListener(this);
        if (!mGyroscopSensorList.isEmpty()) {
            mGyroscopSensorList.clear();
        }
        if (!mAcceSensorList.isEmpty()) {
            mAcceSensorList.clear();
        }
    }

    public void onEvent(CarStatus event) {
        switch (event) {
            case OFFLINE:
                unInitSensorManage();
                break;
            case ONLINE:
                initSensorManage();
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            mAcceSensorList.add(jenerateSensorData(event));
        } else if (sensorType == Sensor.TYPE_GYROSCOPE) {
            mGyroscopSensorList.add(jenerateSensorData(event));
        } else if (sensorType == Sensor.TYPE_LIGHT) {
            float acc = event.accuracy;
            //获取光线强度
            lux = event.values[0];
        }

    }

    private SensorData jenerateSensorData(SensorEvent event) {
        SensorData sensorData = new SensorData();
        sensorData.mX = event.values[0];
        sensorData.mY = event.values[1];
        sensorData.mZ = event.values[2];
        sensorData.mCurrentTime = TimeUtils.getDateString("yyyyMMddHHmmss");
        return sensorData;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /* 释放资源*/
    public void release() {
        mSensorManager.unregisterListener(this);
        instance = null;
    }

    public List<SensorData> getmAcceSensorList() {
        return mAcceSensorList;
    }

    public List<SensorData> getmGyroscopSensorList() {
        return mGyroscopSensorList;
    }

    public float getLux() {
        return lux;
    }
}
