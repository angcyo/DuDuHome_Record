package com.dudu.rest;

import com.dudu.commonlib.CommonLib;
import com.dudu.rest.common.IpUtils;
import com.dudu.rest.common.RetrofitServiceFactory;
import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.model.common.RequestResponse;
import com.dudu.rest.model.portal.GetPortalDownloadAddrArgs;
import com.dudu.rest.model.portal.PortalPopupNum;
import com.dudu.rest.model.portal.PortalVersionResponse;
import com.dudu.rest.model.portal.UpdatePortalResponse;
import com.dudu.rest.service.PortalService;

import org.junit.Test;

import rx.observables.BlockingObservable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by Administrator on 2016/4/22.
 */
public class PortalServiceTest {
    public static final String OBE_ID = "865415013389430";

    @Test
    public void test_uploadPortalPopupNum() throws InterruptedException {
        PortalPopupNum portalPopupNum = new PortalPopupNum(CommonLib.getInstance().getObeId(), 5);
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(PortalService.DONE_PORTAL_UPDATEWIFICONNECTTIMES, portalPopupNum, OBE_ID);
        BlockingObservable<RequestResponse> response = RetrofitServiceFactory.getPortalService()
                .uploadPortalPopupNum(OBE_ID, requestBody).toBlocking();
        RequestResponse responseResult = response.first();
        assertThat(responseResult.resultCode).isEqualTo(0);
    }

    @Test
    public void test_getPortalDownloadAddr() throws InterruptedException {
        GetPortalDownloadAddrArgs getPortalDownloadAddrArgs = new GetPortalDownloadAddrArgs("5");
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(PortalService.DONE_PORTAL_DOWNLOADPORTAL, getPortalDownloadAddrArgs, OBE_ID);
        BlockingObservable<UpdatePortalResponse> response = RetrofitServiceFactory.getPortalService()
                .getPortalDownloadAddr(OBE_ID, requestBody).toBlocking();
        UpdatePortalResponse responseResult = response.first();
        assertThat(responseResult.resultCode).isEqualTo(0);
    }

    @Test
    public void test_getPortalVersion() throws InterruptedException {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(PortalService.DONE_PORTAL_GETPORTALVERSION, null, OBE_ID);
        BlockingObservable<PortalVersionResponse> response = RetrofitServiceFactory.getPortalService()
                .getPortalVersion(OBE_ID, requestBody).toBlocking();
        PortalVersionResponse responseResult = response.first();
        assertThat(responseResult.resultCode).isEqualTo(0);
    }
}
