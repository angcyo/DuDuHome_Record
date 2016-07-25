package com.dudu.rest;

import com.dudu.rest.common.IpUtils;
import com.dudu.rest.common.RetrofitServiceFactory;
import com.dudu.rest.model.GetGuardStatusResponse;
import com.dudu.rest.model.SetGuardSwitchStateRequest;
import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.model.common.RequestResponse;
import com.dudu.rest.service.GuardService;

import org.junit.Test;

import rx.observables.BlockingObservable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Administrator on 2016/4/18.
 */
public class GuardServiceTest {
    public static final String OBE_ID = "865415013354596";

    @Test
    public void test_setThiefSwitchState() throws InterruptedException {
        SetGuardSwitchStateRequest setGuardSwitchStateRequest = new SetGuardSwitchStateRequest("1", OBE_ID);
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(GuardService.THEFT_SETTHIEFSWITCHSTATE, setGuardSwitchStateRequest, OBE_ID);
        BlockingObservable<RequestResponse> response = RetrofitServiceFactory.getGuardService().setThiefSwitchState(OBE_ID, requestBody).toBlocking();
        RequestResponse responseResult = response.first();
        assertThat(responseResult.resultCode).isEqualTo(0);
    }

    @Test
    public void test_getStatus() throws InterruptedException {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(GuardService.THEFT_GETSTATUS, null, OBE_ID);
        BlockingObservable<GetGuardStatusResponse> response = RetrofitServiceFactory.getGuardService().getStatus(OBE_ID, requestBody).toBlocking();
        RequestResponse responseResult = response.first();
        assertThat(responseResult.resultCode).isEqualTo(0);
    }
}
