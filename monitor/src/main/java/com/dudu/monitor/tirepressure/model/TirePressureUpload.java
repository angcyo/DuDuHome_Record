package com.dudu.monitor.tirepressure.model;

import com.dudu.workflow.tpms.TPMSInfo;

/**
 * Created by dengjun on 2016/4/18.
 * Description :
 */
public class TirePressureUpload {
    private int a;            //轮胎位置
    private int b;            //传感器ID

    private float c;            //压力 Bar
    private int d;            //温度 摄氏度

    private int e;            //0:正常, 1:急漏气, 2:正常, 3:加气
    private boolean f;        //电池低压

    private boolean g;        // 无数据
    private boolean h;        //气压高

    private boolean i;        //气压低
    private boolean j;        //0:正常, 1:温度高


    public TirePressureUpload() {
        a = 1;
        b = 1;
        c = 1;
        d = 1;
        e = 1;
        f = false;
        g = false;
        h = false;
        i = false;
        j = false;
    }

    public TirePressureUpload(TPMSInfo tpmsWarnInfo) {
        a = tpmsWarnInfo.getPostion();
        b = tpmsWarnInfo.getSensorID();
        c = tpmsWarnInfo.getPressure();
        d = tpmsWarnInfo.getTemperature();
        e = tpmsWarnInfo.getGasLeaks();
        f = tpmsWarnInfo.isBattery();
        g = tpmsWarnInfo.isNoData();
        h = tpmsWarnInfo.isBarometerHigh();
        i = tpmsWarnInfo.isBarometerLow();
        j = tpmsWarnInfo.isTemperatureHigh();

    }

}
