package com.dudu.workflow.active;

import com.dudu.rest.model.active.ActiveDevice;
import com.dudu.rest.model.active.ActiveRequestResponse;
import com.dudu.rest.model.active.CheckDeviceActive;

import rx.Observable;

/**
 * Created by dengjun on 2016/4/22.
 * Description :
 */
public interface ActiveRequest {
    public Observable<ActiveRequestResponse> acticeDevice(ActiveDevice activeDevice);

    public Observable<ActiveRequestResponse> checkDeviceActive(CheckDeviceActive  checkDeviceActive);
}
