package com.dudu.rest;

import com.dudu.rest.common.IpUtils;
import com.dudu.rest.common.RetrofitServiceFactory;
import com.dudu.rest.model.CheckUpdateResponse;
import com.dudu.rest.model.GetCallSubAccountResponse;
import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.service.AppService;

import org.junit.Test;

import rx.observables.BlockingObservable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Administrator on 2016/4/19.
 */
public class AppServiceTest {

    public static final String OBE_ID = "865415013354596";

    @Test
    public void test_robberySwitch() throws InterruptedException {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(AppService.UPGRADE_CHECK, null, OBE_ID);
        BlockingObservable<CheckUpdateResponse> response = RetrofitServiceFactory.getAppService().checkUpdate(OBE_ID, requestBody).toBlocking();
        CheckUpdateResponse responseResult = response.first();
        assertThat(responseResult.resultCode).isEqualTo(0);
    }


    @Test
    public void test_getCallSubAccount() throws InterruptedException {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(AppService.GET_CALL_SUB_ACCOUNT, null, OBE_ID);
        BlockingObservable<GetCallSubAccountResponse> response = RetrofitServiceFactory.getAppService().getCallSubAccount(OBE_ID, requestBody).toBlocking();
        GetCallSubAccountResponse responseResult = response.first();
        System.out.print(responseResult.toString());
        assertThat(responseResult.resultCode).isEqualTo(0);
    }
}
