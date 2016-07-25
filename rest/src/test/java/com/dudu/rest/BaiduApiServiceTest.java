package com.dudu.rest;

import com.dudu.rest.common.RetrofitServiceFactory;
import com.dudu.rest.model.BaiduWeatherResponse;
import com.dudu.rest.service.BaiduApiService;

import org.junit.Test;

import rx.observables.BlockingObservable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Administrator on 2016/5/5.
 */
public class BaiduApiServiceTest {

    @Test
    public void test_robberySwitch() throws InterruptedException {
        BlockingObservable<BaiduWeatherResponse> response = BaiduApiService.requestWeathers("深圳").toBlocking();
        BaiduWeatherResponse responseResult = response.first();
        assertThat(responseResult.retData.city).isEqualTo("深圳");
    }

    @Test
    public void test_getPortalVersion() throws InterruptedException {
        BlockingObservable<BaiduWeatherResponse> response = RetrofitServiceFactory.getBaiduWeatherService()
                .requestWeathers(BaiduApiService.APIKEY_VALUE, "深圳").toBlocking();
        BaiduWeatherResponse responseResult = response.first();
        assertThat(responseResult.retData.city).isEqualTo("深圳");
    }
}
