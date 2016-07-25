package com.dudu.workflow.portal;

import com.dudu.rest.model.common.RequestResponse;
import com.dudu.rest.model.portal.PortalVersionResponse;
import com.dudu.rest.model.portal.UpdatePortalResponse;

import rx.Observable;

/**
 * Created by Administrator on 2016/4/22.
 */
public interface PortalRequest {

    public Observable<RequestResponse> uploadPortalPopupNum(int periodPortalCount);

    public Observable<UpdatePortalResponse> getPortalDownloadAddr(String version);

    public Observable<PortalVersionResponse> getPortalVersion();
}
