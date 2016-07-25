package com.dudu.monitor.obd.modol;

import com.dudu.monitor.utils.TimeUtils;

/**
 * Created by dengjun on 2016/3/8.
 * Description :
 */
public class ObdRTData {
    private String createTime;                    //采集时间
    private float spd;                        //车速

    private float engLoad;                    //发动机负荷
    private float engCoolant;                //发动机冷却液

    private float curon;                    //瞬时油耗
    private int engSpd;                    //发动机转速

    private float batteryV;                    //电瓶电压
    private int runState;                    //汽车当前运行状态（1：运行 0：熄火）

    private float AvSumpON;//平均油耗
    private float curMileage;//本次里程

    private float totalMileage;//总里程
    private float curFuelSum;//本次油耗量

    private float totalFuelSum;//累计油耗量
    private int curFaultCodeNum;//当前故障码数量

    private int throttlePercentage;//节气门开度


    public ObdRTData(String[] obdRtDataStringArray) {
        createTime = TimeUtils.dateLongFormatString(System.currentTimeMillis(), TimeUtils.format1);

        spd = Float.parseFloat(obdRtDataStringArray[3]);

        engLoad = (int)Float.parseFloat(obdRtDataStringArray[5]);
        engCoolant = Float.parseFloat(obdRtDataStringArray[6]);

        curon = Float.parseFloat(obdRtDataStringArray[7]);
        engSpd = (int)Float.parseFloat(obdRtDataStringArray[2]);

        batteryV= Float.parseFloat(obdRtDataStringArray[1]);
        runState = 1;

        AvSumpON = Float.parseFloat(obdRtDataStringArray[8]);
        curMileage = Float.parseFloat(obdRtDataStringArray[9]);

        totalMileage = Float.parseFloat(obdRtDataStringArray[10]);
        curFuelSum = Float.parseFloat(obdRtDataStringArray[11]);

        totalFuelSum = Float.parseFloat(obdRtDataStringArray[12]);
        curFaultCodeNum = Integer.parseInt(obdRtDataStringArray[13]);

        throttlePercentage = (int)Float.parseFloat(obdRtDataStringArray[4]);
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public float getSpd() {
        return spd;
    }

    public void setSpd(float spd) {
        this.spd = spd;
    }

    public float getEngLoad() {
        return engLoad;
    }

    public void setEngLoad(float engLoad) {
        this.engLoad = engLoad;
    }

    public float getEngCoolant() {
        return engCoolant;
    }

    public void setEngCoolant(float engCoolant) {
        this.engCoolant = engCoolant;
    }

    public float getCuron() {
        return curon;
    }

    public void setCuron(float curon) {
        this.curon = curon;
    }

    public int getEngSpd() {
        return engSpd;
    }

    public void setEngSpd(int engSpd) {
        this.engSpd = engSpd;
    }

    public float getBatteryV() {
        return batteryV;
    }

    public void setBatteryV(float batteryV) {
        this.batteryV = batteryV;
    }

    public int getRunState() {
        return runState;
    }

    public void setRunState(int runState) {
        this.runState = runState;
    }

    public float getAvSumpON() {
        return AvSumpON;
    }

    public void setAvSumpON(float avSumpON) {
        AvSumpON = avSumpON;
    }

    public float getCurMileage() {
        return curMileage;
    }

    public void setCurMileage(float curMileage) {
        this.curMileage = curMileage;
    }

    public float getTotalMileage() {
        return totalMileage;
    }

    public void setTotalMileage(float totalMileage) {
        this.totalMileage = totalMileage;
    }

    public float getCurFuelSum() {
        return curFuelSum;
    }

    public void setCurFuelSum(float curFuelSum) {
        this.curFuelSum = curFuelSum;
    }

    public float getTotalFuelSum() {
        return totalFuelSum;
    }

    public void setTotalFuelSum(float totalFuelSum) {
        this.totalFuelSum = totalFuelSum;
    }

    public int getCurFaultCodeNum() {
        return curFaultCodeNum;
    }

    public void setCurFaultCodeNum(int curFaultCodeNum) {
        this.curFaultCodeNum = curFaultCodeNum;
    }

    public int getThrottlePercentage() {
        return throttlePercentage;
    }

    public void setThrottlePercentage(int throttlePercentage) {
        this.throttlePercentage = throttlePercentage;
    }
}
