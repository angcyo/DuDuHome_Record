package com.dudu.monitor.tirepressure.model;

import com.dudu.persistence.factory.RealmCallFactory;
import com.dudu.persistence.realm.RealmCallBack;
import com.dudu.persistence.realmmodel.tirepressure.TirePressureDataRealm;
import com.dudu.workflow.tpms.TPMSInfo;

/**
 * Created by dengjun on 2016/4/19.
 * Description :
 */
public class TirePressureFactory {
    public static TirePressureDataRealm createTirePressureDataRealm(TPMSInfo tpmsWarnInfo){
        TirePressureDataRealm tirePressureDataRealm = new TirePressureDataRealm();
        tirePressureDataRealm.setPostion(tpmsWarnInfo.getPostion());
        tirePressureDataRealm.setSensorID(tpmsWarnInfo.getSensorID());
        tirePressureDataRealm.setPressure(tpmsWarnInfo.getPressure());
        tirePressureDataRealm.setTemperature(tpmsWarnInfo.getTemperature());
        tirePressureDataRealm.setGasLeaks(tpmsWarnInfo.getGasLeaks());
        tirePressureDataRealm.setBattery(tpmsWarnInfo.isBattery());
        tirePressureDataRealm.setNoData(tpmsWarnInfo.isNoData());
        tirePressureDataRealm.setBarometerHigh(tpmsWarnInfo.isBarometerHigh());
        tirePressureDataRealm.setBarometerLow(tpmsWarnInfo.isBarometerLow());
        tirePressureDataRealm.setTemperatureHigh(tpmsWarnInfo.isTemperatureHigh());
        return tirePressureDataRealm;
    }

    public static void saveTirePressureData(TPMSInfo tpmsWarnInfo, RealmCallBack realmCallBack){
        RealmCallFactory.saveTirePressureDataSync(createTirePressureDataRealm(tpmsWarnInfo), realmCallBack);
    }
}
