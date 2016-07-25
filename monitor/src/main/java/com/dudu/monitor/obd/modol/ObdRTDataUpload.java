package com.dudu.monitor.obd.modol;

/**
 * Created by dengjun on 2016/3/8.
 * Description :
 */
public class ObdRTDataUpload {
    private String a;                    //采集时间
    private float b;                        //车速

    private float c;                    //发动机负荷
    private float d;                //发动机冷却液

    private float e;                    //瞬时油耗
    private int f;                    //发动机转速

    private float g;                    //电瓶电压
    private int h;                    //汽车当前运行状态（1：运行 0：熄火）

    private float i;//平均油耗
    private float j;//本地里程

    private float k;//总里程
    private float l;//本次油耗量

    private float m;//累计油耗量
    private int n;//当前故障码数量

    private int o;//节气门开度

    public ObdRTDataUpload(ObdRTData obdRTData) {
        a = obdRTData.getCreateTime();
        b = obdRTData.getSpd();

        c = obdRTData.getEngLoad();
        d = obdRTData.getEngCoolant();

        e = obdRTData.getCuron();
        f = obdRTData.getEngSpd();

        g = obdRTData.getBatteryV();
        h = obdRTData.getRunState();

        i = obdRTData.getAvSumpON();
        j = obdRTData.getCurMileage();

        k = obdRTData.getTotalMileage();
        l = obdRTData.getCurFuelSum();

        m = obdRTData.getTotalFuelSum();
        n = obdRTData.getCurFaultCodeNum();

        o = obdRTData.getThrottlePercentage();
    }

    /* 用于测试*/
    public ObdRTDataUpload() {
        a = "20160315";
        b = 150;

        c = 20;
        d = 20;

        e = 20;
        f = 20;

        g = 20;
        h = 20;

        i = 20;
        j = 20;

        k = 20;
        l = 20;

        m = 20;
        n = 20;

        o = 20;
    }
}
