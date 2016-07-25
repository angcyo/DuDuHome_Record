package com.dudu.workflow.portal;

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

import rx.Observable;

/**
 * Created by Administrator on 2016/4/22.
 */
public class PortalRequestRetrofitImpl implements PortalRequest {

    public Observable<RequestResponse> uploadPortalPopupNum(int portalCount) {
        PortalPopupNum portalPopupNum = new PortalPopupNum(CommonLib.getInstance().getObeId(), portalCount);
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(PortalService.DONE_PORTAL_UPDATEWIFICONNECTTIMES, portalPopupNum, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory.getPortalService()
                .uploadPortalPopupNum(CommonLib.getInstance().getObeId(), requestBody);
    }

    public Observable<UpdatePortalResponse> getPortalDownloadAddr(String version) {
        GetPortalDownloadAddrArgs getPortalDownloadAddrArgs = new GetPortalDownloadAddrArgs(version);
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(PortalService.DONE_PORTAL_DOWNLOADPORTAL, getPortalDownloadAddrArgs, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory.getPortalService()
                .getPortalDownloadAddr(CommonLib.getInstance().getObeId(), requestBody);
    }

    public Observable<PortalVersionResponse> getPortalVersion() {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(PortalService.DONE_PORTAL_GETPORTALVERSION, null, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory.getPortalService()
                .getPortalVersion(CommonLib.getInstance().getObeId(), requestBody);
    }
}
