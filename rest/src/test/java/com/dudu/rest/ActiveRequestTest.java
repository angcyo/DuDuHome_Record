package com.dudu.rest;

import com.dudu.commonlib.utils.DataJsonTranslation;
import com.dudu.rest.common.IpUtils;
import com.dudu.rest.common.RetrofitServiceFactory;
import com.dudu.rest.model.active.ActiveRequestResponse;
import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.service.ActiveService;

import org.junit.Test;

import rx.observables.BlockingObservable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by dengjun on 2016/4/22.
 * Description :
 */
public class ActiveRequestTest {
    private static final String OBE_ID = "865415013354679";

    @Test
    public void test_robberySwitch() throws InterruptedException {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(ActiveService.ACTIVE_DEVICE, new com.dudu.rest.model.active.ActiveDevice(), OBE_ID);
        BlockingObservable<ActiveRequestResponse> response = RetrofitServiceFactory.getActiveService().activeDevice(OBE_ID, requestBody).toBlocking();
        ActiveRequestResponse responseResult = response.first();
        System.out.print("ActiveDevice ------"+ DataJsonTranslation.objectToJson(responseResult));
        assertThat(responseResult.resultCode).isEqualTo(0);
    }
}
