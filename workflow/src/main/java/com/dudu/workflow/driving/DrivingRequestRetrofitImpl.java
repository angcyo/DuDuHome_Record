package com.dudu.workflow.driving;

import com.dudu.commonlib.CommonLib;
import com.dudu.persistence.driving.FaultCode;
import com.dudu.rest.common.IpUtils;
import com.dudu.rest.common.RetrofitServiceFactory;
import com.dudu.rest.model.DriveScoreRecordRequestBody;
import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.model.common.RequestResponse;
import com.dudu.rest.model.driving.requestargs.InquiryFaultRequestArgs;
import com.dudu.rest.model.driving.requestargs.PushAcceleratedTestDataRequestArgs;
import com.dudu.rest.model.driving.response.FaultCodeResponse;
import com.dudu.rest.model.driving.response.GetCarBrandResponse;
import com.dudu.rest.service.DrivingService;

import java.util.List;

import rx.Observable;

/**
 * 驾车相关请求Retrofit实现
 * Created by Eaway on 2016/2/17.
 */
public class DrivingRequestRetrofitImpl implements DrivingRequest {

    @Override
    public Observable<RequestResponse> pushAcceleratedTestData(String accTestTime, String testFeedId, String phone) {
        PushAcceleratedTestDataRequestArgs pushAcceleratedTestDataRequestArgs =
                new PushAcceleratedTestDataRequestArgs(CommonLib.getInstance().getObeId(), "mirror", accTestTime, testFeedId, phone);
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(DrivingService.TEST_MIRRORTOAPP, pushAcceleratedTestDataRequestArgs, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory.getDrivingService()
                .pushAcceleratedTestData(CommonLib.getInstance().getObeId(), requestBody);
    }

    @Override
    public Observable<RequestResponse> pushDrivingHabitsData(String startTime, String endTime) {
        DriveScoreRecordRequestBody drivingHabitsData = new DriveScoreRecordRequestBody();
        drivingHabitsData.setObeId(CommonLib.getInstance().getObeId());
        drivingHabitsData.setEndTime(endTime);
        drivingHabitsData.setStartTime(startTime);
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(DrivingService.DRIVESCORERECORD_CALCULATESCORE, drivingHabitsData, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory
                .getDrivingService()
                .pushDrivingHabits(CommonLib.getInstance().getObeId(), requestBody);
    }

    @Override
    public Observable<FaultCodeResponse> inquiryFault(String[] faultCodes, String carBrand) {
        String faultCode = "";
        for (int i = 0; i < faultCodes.length; i++) {
            faultCode += faultCodes[i] + ",";
        }
        if (faultCode.length() > 0) {
            faultCode.substring(0, faultCode.length() - 1);
        }
        InquiryFaultRequestArgs inquiryFaultRequestArgs = new InquiryFaultRequestArgs(faultCode, carBrand);
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(DrivingService.VEHICLESELFTEST_GETFAULTTREATMENT, inquiryFaultRequestArgs, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory
                .getDrivingService()
                .inquiryFault(CommonLib.getInstance().getObeId(), requestBody);
    }

    @Override
    public Observable<FaultCodeResponse> inquiryFault(List<FaultCode> faultCodeList, String carBrand) {
        String faultCode = "";
        if (faultCodeList != null && faultCodeList.size() > 0) {
            for (int i = 0; i < faultCodeList.size(); i++) {
                String[] faultCodes = faultCodeList.get(i).getFaultCode();
                for (int j = 0; j < faultCodes.length; j++) {
                    faultCode += faultCodes[j] + ",";
                }
            }
        }
        if (faultCode.length() > 0) {
            faultCode.substring(0, faultCode.length() - 1);
        }
        InquiryFaultRequestArgs inquiryFaultRequestArgs = new InquiryFaultRequestArgs(faultCode, carBrand);
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(DrivingService.VEHICLESELFTEST_GETFAULTTREATMENT, inquiryFaultRequestArgs, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory.getDrivingService().inquiryFault(CommonLib.getInstance().getObeId(), requestBody);
    }

    @Override
    public Observable<GetCarBrandResponse> getCarBrand() {
        return RetrofitServiceFactory
                .getDrivingService()
                .getCarBrand(CommonLib.getInstance().getObeId(), IpUtils.requestArgsToRequestBody(DrivingService.CAR_GETCARBRAND, null, CommonLib.getInstance().getObeId()));
    }
}
