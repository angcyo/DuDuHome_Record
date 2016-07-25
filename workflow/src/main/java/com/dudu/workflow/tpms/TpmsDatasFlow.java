package com.dudu.workflow.tpms;

import com.dudu.persistence.factory.RealmCallFactory;
import com.dudu.persistence.realm.RealmCallBack;
import com.dudu.persistence.realmmodel.tirepressure.TirePressureDataRealm;
import com.dudu.rest.model.driving.response.FaultCodeDetailMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;

/**
 * Created by Administrator on 2016/5/2.
 */
public class TpmsDatasFlow {
    private static Logger logger = LoggerFactory.getLogger("car.TpmsDatasFlow");

    public static TirePressureData transformDatasFromRealm(TirePressureDataRealm realmRealmResult) {
        return new TirePressureData(realmRealmResult.getPostion()
                , realmRealmResult.getSensorID()
                , realmRealmResult.getPressure()
                , realmRealmResult.getTemperature()
                , realmRealmResult.getGasLeaks()
                , realmRealmResult.isBattery()
                , realmRealmResult.isNoData()
                , realmRealmResult.isBarometerHigh()
                , realmRealmResult.isBarometerLow()
                , realmRealmResult.isTemperatureHigh());
    }

    public static List<TirePressureData> transformDatasFromRealm(RealmResults<TirePressureDataRealm> realmRealmResults) {
        List<TirePressureData> tirePressureDataList = new ArrayList<>();
        if (realmRealmResults != null) {
            for (TirePressureDataRealm tirePressureDataRealm : realmRealmResults) {
                tirePressureDataList.add(transformDatasFromRealm(tirePressureDataRealm));
            }
        }
        return tirePressureDataList;
    }


    public static void findAllTirePressureDatas(TpmsDataCallBack dataCallBack) {
        RealmCallFactory.findAllTirePressureDatasSync(new RealmCallBack<RealmResults<TirePressureDataRealm>, Exception>() {
            @Override
            public void onRealm(RealmResults<TirePressureDataRealm> result) {
                List<TirePressureData> unrightTirePressureDatas =
                        getUnrightTirePressureDatas(TpmsDatasFlow.transformDatasFromRealm(result));
                dataCallBack.onDatas(unrightTirePressureDatas);
            }

            @Override
            public void onError(Exception error) {
                dataCallBack.onError(error);
            }
        });
    }

    public static List<TirePressureData> getUnrightTirePressureDatas(List<TirePressureData> tirePressureDataList) {
        logger.debug("getUnrightTirePressureDatas");
        List<TirePressureData> lastTirePressureDataList = new ArrayList<>();
        List<TirePressureData> tireEmptyDataList = initTireEmptyDatas();
        if (tirePressureDataList != null && tirePressureDataList.size() > 0) {
            for (TirePressureData faultCodeDetailMessage : tirePressureDataList) {
                logger.debug(faultCodeDetailMessage.toString());
                if (!checkTirePressureDataIsRight(faultCodeDetailMessage)) {
                    lastTirePressureDataList.add(faultCodeDetailMessage);
                }
            }
        }
        for (TirePressureData tireEmptyData : tireEmptyDataList) {
            boolean dataHasAdded = checkFaultCodeHasAdded(tireEmptyData, tirePressureDataList);
            if (!dataHasAdded) {
                lastTirePressureDataList.add(tireEmptyData);
            }
        }
        return lastTirePressureDataList;
    }

    private static boolean checkTirePressureDataIsRight(TirePressureData tirePressureDataRealm) {
        logger.debug("checkTirePressureDataRealmIsRight");
        boolean isRight = tirePressureDataRealm != null
                && (tirePressureDataRealm.getGasLeaks() == 0 || tirePressureDataRealm.getGasLeaks() == 2)
                && !tirePressureDataRealm.isBattery()
                && !tirePressureDataRealm.isNoData()
                && !tirePressureDataRealm.isBarometerHigh()
                && !tirePressureDataRealm.isBarometerLow()
                && !tirePressureDataRealm.isTemperatureHigh();
        return isRight;
    }

    private static boolean checkFaultCodeHasAdded(TirePressureData flagData, List<TirePressureData> tirePressureDataList) {
        if (tirePressureDataList != null && tirePressureDataList.size() > 0) {
            for (TirePressureData tirePressureData : tirePressureDataList) {
                if (tirePressureData.getPostion() == flagData.getPostion()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<TirePressureData> initTireEmptyDatas() {
        List<TirePressureData> faultCodeStringList = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            TirePressureData tirePressureData = new TirePressureData(i);
            faultCodeStringList.add(tirePressureData);
        }
        return faultCodeStringList;
    }

    public static List<FaultCodeDetailMessage> tirePressureDataRealmsToFaultCodeDetailMessages(List<TirePressureData> tirePressureDatas) {
        List<FaultCodeDetailMessage> faultCodeDetailMessages = new ArrayList<>();
        for (TirePressureData tirePressureData : tirePressureDatas) {
            faultCodeDetailMessages.add(tirePressureDataRealmToFaultCodeDetailMessage(tirePressureData));
        }
        return faultCodeDetailMessages;
    }

    public static String getTireFaultCode(int position) {
        String faultCode = "";
        switch (position) {
            case 1:
                faultCode = TpmsConstants.WSB01;
                break;
            case 2:
                faultCode = TpmsConstants.WSB02;
                break;
            case 3:
                faultCode = TpmsConstants.WSB03;
                break;
            case 4:
                faultCode = TpmsConstants.WSB04;
                break;
        }
        return faultCode;
    }

    public static boolean isTireFaultType(String faultCode) {
        return faultCode.equals(TpmsConstants.WSB01)
                || faultCode.equals(TpmsConstants.WSB02)
                || faultCode.equals(TpmsConstants.WSB03)
                || faultCode.equals(TpmsConstants.WSB04);
    }

    public static FaultCodeDetailMessage tirePressureDataRealmToFaultCodeDetailMessage(TirePressureData tirePressureData) {
        String faultCode = getTireFaultCode(tirePressureData.getPostion());
        FaultCodeDetailMessage faultCodeDetailMessage = new FaultCodeDetailMessage(faultCode);
        faultCodeDetailMessage.faultInfo = getFaultDescription(tirePressureData);
        faultCodeDetailMessage.dataIsEmpty = tirePressureData.isEmpty();
        return faultCodeDetailMessage;
    }

    public static String getFaultDescription(TirePressureData tirePressureData) {
        StringBuilder faultDescription = new StringBuilder();
        faultDescription.append(getTireChinese(tirePressureData.getPostion()));
        if (tirePressureData.isEmpty()) {
            String description = faultDescription.append("传感器可能有故障，请检查").toString();
            return description;
        }

        switch (tirePressureData.getGasLeaks()) {
            case 1:
                faultDescription.append("急漏气,");
                break;
            case 3:
                faultDescription.append("加气,");
                break;
        }
        if (tirePressureData.isBattery()) {
            faultDescription.append("电压过低,");
        }
        if (tirePressureData.isBarometerLow()) {
            faultDescription.append("气压过低,");
        }
        if (tirePressureData.isBarometerHigh()) {
            faultDescription.append("气压过高,");
        }
        if (tirePressureData.isTemperatureHigh()) {
            faultDescription.append("温度过高,");
        }
        if (tirePressureData.isNoData()) {
            faultDescription.append("30分钟内无数据");
        }
        String faultDescriptionString = faultDescription.toString().trim();
        if (faultDescriptionString.endsWith(",")) {
            return faultDescriptionString.substring(0, faultDescriptionString.lastIndexOf(","));
        }
        return faultDescriptionString;
    }

    public static String getTireChinese(int position) {
        switch (position) {
            case 1:
                return TpmsConstants.RIGHT_FRONT_TYRE_CHINESE;
            case 2:
                return TpmsConstants.LEFT_FRONT_TYRE_CHINESE;
            case 3:
                return TpmsConstants.RIGHT_BACK_TYRE_CHINESE;
            case 4:
                return TpmsConstants.LEFT_BACK_TYRE_CHINESE;
        }
        return TpmsConstants.NOKNOW_TYRE_CHINESE;
    }

    public static String getTireChinese(String position) {
        switch (position) {
            case TpmsConstants.WSB01:
                return TpmsConstants.RIGHT_FRONT_TYRE_CHINESE;
            case TpmsConstants.WSB02:
                return TpmsConstants.LEFT_FRONT_TYRE_CHINESE;
            case TpmsConstants.WSB03:
                return TpmsConstants.RIGHT_BACK_TYRE_CHINESE;
            case TpmsConstants.WSB04:
                return TpmsConstants.LEFT_BACK_TYRE_CHINESE;
        }
        return TpmsConstants.NOKNOW_TYRE_CHINESE;
    }

    public static TPMSInfo.POSITION getTPMSInfoPOSITION(String position) {
        switch (position) {
            case TpmsConstants.WSB01:
                return TPMSInfo.POSITION.RIGHT_FRONT;
            case TpmsConstants.WSB02:
                return TPMSInfo.POSITION.LEFT_FRONT;
            case TpmsConstants.WSB03:
                return TPMSInfo.POSITION.RIGHT_BACK;
            case TpmsConstants.WSB04:
                return TPMSInfo.POSITION.LEFT_BACK;
        }
        return TPMSInfo.POSITION.UNKNOW;
    }

    public static String getTirePairingCompleteWarning(TPMSInfo.POSITION position) {
        StringBuilder warningStringBuilder = new StringBuilder();
        warningStringBuilder.append(getTireChinese(position.value()));
        warningStringBuilder.append("对码成功");
        return warningStringBuilder.toString();
    }

    public static String getTirePairingTimoutWarning(String position) {
        StringBuilder warningStringBuilder = new StringBuilder();
        warningStringBuilder.append(getTireChinese(position));
        warningStringBuilder.append("对码超时");
        return warningStringBuilder.toString();
    }
}
