package com.dudu.workflow.active;

import com.dudu.commonlib.CommonLib;
import com.dudu.rest.common.IpUtils;
import com.dudu.rest.common.RetrofitServiceFactory;
import com.dudu.rest.model.active.ActiveDevice;
import com.dudu.rest.model.active.ActiveRequestResponse;
import com.dudu.rest.model.active.CheckDeviceActive;
import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.service.ActiveService;

import rx.Observable;

/**
 * Created by dengjun on 2016/4/22.
 * Description :
 */
public class ActiveRequestRetrofitImpl implements ActiveRequest{
    @Override
    public Observable<ActiveRequestResponse> acticeDevice(ActiveDevice activeDevice) {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(ActiveService.ACTIVE_DEVICE, activeDevice, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory
                .getActiveService()
                .activeDevice(CommonLib.getInstance().getObeId(),requestBody);
    }

    @Override
    public Observable<ActiveRequestResponse> checkDeviceActive(CheckDeviceActive checkDeviceActive) {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(ActiveService.CHECK_ACTIVE, checkDeviceActive, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory
                .getActiveService()
                .checkActive(CommonLib.getInstance().getObeId(),requestBody);
    }
}
