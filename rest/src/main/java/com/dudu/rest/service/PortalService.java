package com.dudu.rest.service;

import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.model.common.RequestResponse;
import com.dudu.rest.model.portal.PortalVersionResponse;
import com.dudu.rest.model.portal.UpdatePortalResponse;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Administrator on 2016/4/22.
 */
public interface PortalService {

    public static final String DONE_PORTAL_UPDATEWIFICONNECTTIMES = "/done/portal/updateWifiConnectTimes";
    public static final String DONE_PORTAL_DOWNLOADPORTAL = "/done/portal/downloadPortal";
    public static final String DONE_PORTAL_GETPORTALVERSION = "/done/portal/getPortalVersion";

    @POST("/done/portal/updateWifiConnectTimes/{businessId}/mirror")
    public Observable<RequestResponse> uploadPortalPopupNum(@Path("businessId") String businessId, @Body RequestBody requestBody);

    @POST("/done/portal/downloadPortal/{businessId}/mirror")
    public Observable<UpdatePortalResponse> getPortalDownloadAddr(@Path("businessId") String businessId, @Body RequestBody requestBody);

    @POST("/done/portal/getPortalVersion/{businessId}/mirror")
    public Observable<PortalVersionResponse> getPortalVersion(@Path("businessId") String businessId, @Body RequestBody requestBody);
}
