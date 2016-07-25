package com.dudu.workflow.obd;

import com.dudu.persistence.driving.FaultCode;

import java.util.ArrayList;
import java.util.List;

import static com.dudu.workflow.obd.CarCheckType.ECM;

/**
 * Created by Administrator on 2016/5/23.
 */
public class FaultCodeFlow {

    public static String getVehicleConstants(int faultType) {
        switch (faultType) {
            case FaultCode.ABS:
                return VehicleConstants.VEHICLE_ABS;
            case FaultCode.ECM:
                return VehicleConstants.VEHICLE_ENG;
            case FaultCode.SRS:
                return VehicleConstants.VEHICLE_SRS;
            case FaultCode.TCM:
                return VehicleConstants.VEHICLE_GEA;
            case FaultCode.WSB:
                return VehicleConstants.VEHICLE_WSB;
            default:
                return "";
        }
    }

    public static CarCheckType getCarCheckType(String faultType) {
        switch (faultType) {
            case VehicleConstants.VEHICLE_ABS:
                return CarCheckType.ABS;
            case VehicleConstants.VEHICLE_ENG:
                return ECM;
            case VehicleConstants.VEHICLE_SRS:
                return CarCheckType.SRS;
            case VehicleConstants.VEHICLE_GEA:
                return CarCheckType.TCM;
            case VehicleConstants.VEHICLE_WSB:
                return CarCheckType.WSB;
            default:
                return CarCheckType.ALL;
        }
    }

    public static String getShowConstants(String faultType) {
        switch (faultType) {
            case VehicleConstants.VEHICLE_ABS:
                return VehicleConstants.VEHICLE_ABS_CH;
            case VehicleConstants.VEHICLE_ENG:
                return VehicleConstants.VEHICLE_ENG_CH;
            case VehicleConstants.VEHICLE_SRS:
                return VehicleConstants.VEHICLE_SRS_CH;
            case VehicleConstants.VEHICLE_GEA:
                return VehicleConstants.VEHICLE_GEA_CH;
            case VehicleConstants.VEHICLE_WSB:
                return VehicleConstants.VEHICLE_WSB_CH;
            default:
                return "";
        }
    }



    /**
     * 根据故障码获取故障部件
     *
     * @param faultCodeList
     * @return
     */
    public static String[] getFaultCode(List<FaultCode> faultCodeList) {
        String[] faultCodeTypes = new String[faultCodeList.size()];
        if (faultCodeList.size() > 0) {
            for (int i = 0; i < faultCodeList.size(); i++) {
                FaultCode faultCode = faultCodeList.get(i);
                faultCodeTypes[i] = FaultCodeFlow.getVehicleConstants(faultCode.getCarCheckType());
            }
        }
        return faultCodeTypes;
    }

    /**
     * 将胎压添加到故障类型
     *
     * @param faultCodes
     * @return
     */
    public static String[] addWsbFaultCode(String[] faultCodes) {
        String[] faultCodeTypes = new String[faultCodes.length + 1];
        if (faultCodes.length > 0) {
            for (int i = 0; i < faultCodes.length; i++) {
                faultCodeTypes[i] = faultCodes[i];
            }
        }
        faultCodeTypes[faultCodes.length] = VehicleConstants.VEHICLE_WSB;
        return faultCodeTypes;
    }

    public static List<Integer> initCarTypes() {
        List<Integer> allFaultTypes = new ArrayList<Integer>();
        allFaultTypes.add(FaultCode.ECM);
        allFaultTypes.add(FaultCode.TCM);
        allFaultTypes.add(FaultCode.ABS);
        allFaultTypes.add(FaultCode.SRS);
        return allFaultTypes;
    }
}
