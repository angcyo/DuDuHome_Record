package com.dudu.workflow.robbery;

import com.dudu.commonlib.CommonLib;
import com.dudu.persistence.RobberyMessage.RobberyMessage;
import com.dudu.rest.common.IpUtils;
import com.dudu.rest.common.RetrofitServiceFactory;
import com.dudu.rest.model.GetRobberyStatusResponse;
import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.model.common.RequestResponse;
import com.dudu.rest.model.SetRobberySwitchStateRequest;
import com.dudu.rest.service.DrivingService;
import com.dudu.rest.service.RobberyService;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/2/16.
 */
public class RobberyRequestRetrofitImpl implements RobberyRequest {

    @Override
    public Observable<Integer> getCarInsuranceAuthState() {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(DrivingService.DRIVESCORERECORD_CALCULATESCORE, null, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory.getRobberyService().getRobberyState(CommonLib.getInstance().getObeId(), requestBody)
                .map(getRobberyStatusResponse -> Integer.valueOf(getRobberyStatusResponse.result.audit_state))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<GetRobberyStatusResponse> getRobberyState() {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(RobberyService.ROBBERY_GET_STATUS, null,CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory.getRobberyService()
                .getRobberyState(CommonLib.getInstance().getObeId(), requestBody)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());

    }


    @Override
    public Observable<RequestResponse> settingAntiRobberyMode(RobberyMessage robberyMessage) {
        SetRobberySwitchStateRequest setRobberySwitchStateRequest =
                new SetRobberySwitchStateRequest(robberyMessage);
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(RobberyService.UPLOAD_ROBBERY_SWITCH, setRobberySwitchStateRequest, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory.getRobberyService()
                .robberySwitch(CommonLib.getInstance().getObeId(), requestBody)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

}

