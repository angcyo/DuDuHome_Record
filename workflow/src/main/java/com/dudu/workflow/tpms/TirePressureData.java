package com.dudu.workflow.tpms;

/**
 * Created by Administrator on 2016/5/2.
 */
public class TirePressureData {

    private boolean isEmpty = false;

    private int postion;   //轮胎位置
    private int sensorID;  //传感器ID
    private float pressure; //压力 Bar
    private int temperature; //温度 摄氏度
    private int gasLeaks; //0:正常, 1:急漏气, 2:正常, 3:加气
    private boolean battery; //0:正常, 1:低压
    private boolean noData;  //0:正常, 1:30分钟未收到发射器到数据
    private boolean barometerHigh; //气压高
    private boolean barometerLow;  //气压低
    private boolean temperatureHigh; //0:正常, 1:温度高

    public TirePressureData(int postion) {
        this.postion = postion;
        isEmpty = true;
    }

    public TirePressureData(int postion, int sensorID, float pressure, int temperature, int gasLeaks, boolean battery, boolean noData, boolean barometerHigh, boolean barometerLow, boolean temperatureHigh) {
        this.postion = postion;
        this.sensorID = sensorID;
        this.pressure = pressure;
        this.temperature = temperature;
        this.gasLeaks = gasLeaks;
        this.battery = battery;
        this.noData = noData;
        this.barometerHigh = barometerHigh;
        this.barometerLow = barometerLow;
        this.temperatureHigh = temperatureHigh;
    }

    @Override
    public String toString() {
        return "TirePressureData{" +
                "postion=" + postion +
                ", sensorID=" + sensorID +
                ", pressure=" + pressure +
                ", temperature=" + temperature +
                ", gasLeaks=" + gasLeaks +
                ", battery=" + battery +
                ", noData=" + noData +
                ", barometerHigh=" + barometerHigh +
                ", barometerLow=" + barometerLow +
                ", temperatureHigh=" + temperatureHigh +
                '}';
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    public int getPostion() {
        return postion;
    }

    public void setPostion(int postion) {
        this.postion = postion;
    }

    public int getSensorID() {
        return sensorID;
    }

    public void setSensorID(int sensorID) {
        this.sensorID = sensorID;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getGasLeaks() {
        return gasLeaks;
    }

    public void setGasLeaks(int gasLeaks) {
        this.gasLeaks = gasLeaks;
    }

    public boolean isBattery() {
        return battery;
    }

    public void setBattery(boolean battery) {
        this.battery = battery;
    }

    public boolean isNoData() {
        return noData;
    }

    public void setNoData(boolean noData) {
        this.noData = noData;
    }

    public boolean isBarometerHigh() {
        return barometerHigh;
    }

    public void setBarometerHigh(boolean barometerHigh) {
        this.barometerHigh = barometerHigh;
    }

    public boolean isBarometerLow() {
        return barometerLow;
    }

    public void setBarometerLow(boolean barometerLow) {
        this.barometerLow = barometerLow;
    }

    public boolean isTemperatureHigh() {
        return temperatureHigh;
    }

    public void setTemperatureHigh(boolean temperatureHigh) {
        this.temperatureHigh = temperatureHigh;
    }
}
