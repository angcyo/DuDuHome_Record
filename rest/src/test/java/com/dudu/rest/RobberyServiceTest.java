package com.dudu.rest;

import com.dudu.commonlib.CommonLib;
import com.dudu.persistence.RobberyMessage.RobberyMessage;
import com.dudu.rest.common.IpUtils;
import com.dudu.rest.common.RetrofitServiceFactory;
import com.dudu.rest.model.GetRobberyStatusResponse;
import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.model.common.RequestResponse;
import com.dudu.rest.model.SetRobberySwitchStateRequest;
import com.dudu.rest.service.DrivingService;
import com.dudu.rest.service.RobberyService;

import org.junit.Test;

import rx.observables.BlockingObservable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Administrator on 2016/4/7.
 */
public class RobberyServiceTest {
    public static final String OBE_ID = "865415013354588";

    @Test
    public void test_robberySwitch() throws InterruptedException {
        RobberyMessage robberyMessage=new RobberyMessage();
        robberyMessage.setRotatingSpeed("1500");
        robberyMessage.setCompleteTime("30");
        robberyMessage.setOperationNumber("3");
        robberyMessage.setRobberySwitch(true);
        SetRobberySwitchStateRequest setRobberySwitchStateRequest =
                new SetRobberySwitchStateRequest(robberyMessage);
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(RobberyService.UPLOAD_ROBBERY_SWITCH, setRobberySwitchStateRequest, OBE_ID);
        BlockingObservable<RequestResponse> response = RetrofitServiceFactory.getRobberyService()
                .robberySwitch(OBE_ID, requestBody).toBlocking();
        RequestResponse responseResult = response.first();
        assertThat(responseResult.resultCode).isEqualTo(0);
    }

    @Test
    public void test_getRobberyState() throws InterruptedException {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(RobberyService.ROBBERY_GET_STATUS, null, OBE_ID);
        BlockingObservable<GetRobberyStatusResponse> response = RetrofitServiceFactory.getRobberyService()
                .getRobberyState(OBE_ID, requestBody).toBlocking();
        GetRobberyStatusResponse responseResult = response.first();
        assertThat(responseResult.resultCode).isEqualTo(0);
    }
}
