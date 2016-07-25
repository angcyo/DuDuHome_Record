package com.dudu.workflow.robbery;

import com.dudu.persistence.RobberyMessage.RobberyMessage;
import com.dudu.rest.model.GetRobberyStatusResponse;
import com.dudu.rest.model.common.RequestResponse;

import rx.Observable;

/**
 * Created by Administrator on 2016/2/16.
 */
public interface RobberyRequest {

    public Observable<Integer> getCarInsuranceAuthState();
    public Observable<GetRobberyStatusResponse> getRobberyState();
    public Observable<RequestResponse> settingAntiRobberyMode(RobberyMessage robberyMessage);

}
