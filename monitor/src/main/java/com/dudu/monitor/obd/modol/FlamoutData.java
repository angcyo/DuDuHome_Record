package com.dudu.monitor.obd.modol;

import com.dudu.monitor.utils.TimeUtils;

/**
 * Created by dengjun on 2015/11/26.
 * Description :
 */
public class FlamoutData {
    private int hotCarTime;       //热车时长
    private int idleTime;         // 怠速时长(min)

    private float times; //行驶时长(min)
    private float mileT;        //此次里程(km)

    private float idleFuel; //怠速油耗量
    private float idleFuelConsumption;  // 本次行驶耗油量

    private int maxrpm;            //最大发动机转速(rpm)
    private int maxspd;            //最大车速(km/h)

    private int accNum;   //本次急加速次数 Times
    private int SharpDownNum;   //本次急减速次数 Times

    private String createTime;    //采集时间


    public FlamoutData(String[] flamoutDataStringArray) {

        hotCarTime = (int)Float.parseFloat(flamoutDataStringArray[1]);
        idleTime = (int)Float.parseFloat(flamoutDataStringArray[2]);

        times = Float.parseFloat(flamoutDataStringArray[3]);
        mileT = Float.parseFloat(flamoutDataStringArray[4]);

        idleFuel = Float.parseFloat(flamoutDataStringArray[5]);
        idleFuelConsumption = Float.parseFloat(flamoutDataStringArray[6]);

        maxrpm = (int)Float.parseFloat(flamoutDataStringArray[7]);
        maxspd = (int)Float.parseFloat(flamoutDataStringArray[8]);

        accNum = (int)Float.parseFloat(flamoutDataStringArray[9]);
        SharpDownNum = (int)Float.parseFloat(flamoutDataStringArray[10]);

        createTime = TimeUtils.dateLongFormatString(System.currentTimeMillis(), TimeUtils.format1);;
    }


    public int getHotCarTime() {
        return hotCarTime;
    }

    public void setHotCarTime(int hotCarTime) {
        this.hotCarTime = hotCarTime;
    }

    public int getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
    }

    public float getTimes() {
        return times;
    }

    public void setTimes(float times) {
        this.times = times;
    }

    public float getMileT() {
        return mileT;
    }

    public void setMileT(float mileT) {
        this.mileT = mileT;
    }

    public float getIdleFuel() {
        return idleFuel;
    }

    public void setIdleFuel(float idleFuel) {
        this.idleFuel = idleFuel;
    }

    public float getIdleFuelConsumption() {
        return idleFuelConsumption;
    }

    public void setIdleFuelConsumption(float idleFuelConsumption) {
        this.idleFuelConsumption = idleFuelConsumption;
    }

    public int getMaxrpm() {
        return maxrpm;
    }

    public void setMaxrpm(int maxrpm) {
        this.maxrpm = maxrpm;
    }

    public int getMaxspd() {
        return maxspd;
    }

    public void setMaxspd(int maxspd) {
        this.maxspd = maxspd;
    }

    public int getAccNum() {
        return accNum;
    }

    public void setAccNum(int accNum) {
        this.accNum = accNum;
    }

    public int getSharpDownNum() {
        return SharpDownNum;
    }

    public void setSharpDownNum(int sharpDownNum) {
        SharpDownNum = sharpDownNum;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
