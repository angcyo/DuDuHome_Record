package com.dudu.rest;

import com.dudu.rest.common.IpUtils;
import com.dudu.rest.common.RetrofitServiceFactory;
import com.dudu.rest.model.DriveScoreRecordRequestBody;
import com.dudu.rest.model.driving.response.FaultCodeResponse;
import com.dudu.rest.model.driving.response.GetCarBrandResponse;
import com.dudu.rest.model.driving.requestargs.InquiryFaultRequestArgs;
import com.dudu.rest.model.driving.requestargs.PushAcceleratedTestDataRequestArgs;
import com.dudu.rest.model.common.RequestArgs;
import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.model.common.RequestResponse;
import com.dudu.rest.service.DrivingService;

import org.junit.Test;

import rx.observables.BlockingObservable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Administrator on 2016/3/5.
 */
public class DrivingServiceTest {

    public static final String OBE_ID = "865415013389501";
    public static final String PHONE = "15820757371";
    public static final String MIRROR = "mirror";


    @Test
    public void test_inquiryFault() throws InterruptedException {
        String code = "P2090";
        String car = "丰田";
        InquiryFaultRequestArgs inquiryFaultRequestArgs = new InquiryFaultRequestArgs(code, car);
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(DrivingService.VEHICLESELFTEST_GETFAULTTREATMENT, inquiryFaultRequestArgs, OBE_ID);
        BlockingObservable<FaultCodeResponse> response = RetrofitServiceFactory.getDrivingService().inquiryFault(OBE_ID, requestBody).toBlocking();
        FaultCodeResponse responseResult = response.first();
        assertThat(responseResult.resultCode).isEqualTo(0);
    }

    @Test
    public void test_pushAcceleratedTestData() throws InterruptedException {
        String testResult = "1";
        String testFeedId = "9fb36ab7-d521-42fb-92f2-c590d5f043b8";
        PushAcceleratedTestDataRequestArgs pushAcceleratedTestDataRequestBody = new PushAcceleratedTestDataRequestArgs(OBE_ID, MIRROR, testResult, testFeedId, PHONE);
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(DrivingService.TEST_MIRRORTOAPP, pushAcceleratedTestDataRequestBody, OBE_ID);
        BlockingObservable<RequestResponse> response = RetrofitServiceFactory.getDrivingService()
                .pushAcceleratedTestData(OBE_ID, requestBody).toBlocking();
        assertThat(response.first().resultCode).isEqualTo(0);
    }

    @Test
    public void test_pushDrivingHabits() throws InterruptedException {
        DriveScoreRecordRequestBody drivingHabitsData = new DriveScoreRecordRequestBody();
        drivingHabitsData.setObeId(OBE_ID);
        drivingHabitsData.setEndTime("1459307180");
        drivingHabitsData.setStartTime("1459325180");
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(DrivingService.DRIVESCORERECORD_CALCULATESCORE, drivingHabitsData, OBE_ID);
        BlockingObservable<RequestResponse> response = RetrofitServiceFactory.getDrivingService()
                .pushDrivingHabits(OBE_ID, requestBody).toBlocking();
        assertThat(response.first().resultCode).isEqualTo(0);
    }

    @Test
    public void test_getCarBrand() throws InterruptedException {
        RequestArgs requestArgs = new RequestArgs(DrivingService.CAR_GETCARBRAND, null);
        RequestBody requestBody = IpUtils.requestArgsEncrypt(requestArgs, OBE_ID);
        BlockingObservable<GetCarBrandResponse> response =
                RetrofitServiceFactory.getDrivingService().getCarBrand(OBE_ID, requestBody).toBlocking();
        GetCarBrandResponse result = response.first();
        assertThat(result.resultCode).isEqualTo(0);
    }

}
