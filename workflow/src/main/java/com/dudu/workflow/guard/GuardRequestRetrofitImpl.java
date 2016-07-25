package com.dudu.workflow.guard;

import com.dudu.commonlib.CommonLib;
import com.dudu.rest.common.IpUtils;
import com.dudu.rest.common.RetrofitServiceFactory;
import com.dudu.rest.model.GetGuardStatusResponse;
import com.dudu.rest.model.SetDigitPasswordRequest;
import com.dudu.rest.model.SetGuardSwitchStateRequest;
import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.model.common.RequestResponse;
import com.dudu.rest.model.driving.requestargs.VerificationVerificationCodeArgs;
import com.dudu.rest.service.GuardService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/2/16.
 */
public class GuardRequestRetrofitImpl implements GuardRequest {

    private Logger logger = LoggerFactory.getLogger("GuardRequestRetrofitImpl");

    @Override
    public Observable<RequestResponse> checkLockCar(boolean lock) {
        logger.debug("lockCar:" + CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory.getGuardService()
                .setThiefSwitchState(
                        CommonLib.getInstance().getObeId(),
                        IpUtils.requestArgsToRequestBody(GuardService.THEFT_SETTHIEFSWITCHSTATE
                                , new SetGuardSwitchStateRequest(lock ? "1" : "0", CommonLib.getInstance().getObeId())
                                , CommonLib.getInstance().getObeId()))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<GetGuardStatusResponse> getLockStatus() {

        RequestBody requestBody = IpUtils.requestArgsToRequestBody(GuardService.THEFT_GETSTATUS, null, CommonLib.getInstance().getObeId());
        logger.debug("getLockStatus:--requestBody:" + requestBody.params);
        return RetrofitServiceFactory.getGuardService()
                .getStatus(
                        CommonLib.getInstance().getObeId(),
                        requestBody)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<RequestResponse> obtainVerificationCode() {
        logger.debug("obtainVerificationCode" + CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory.getGuardService().obtainVerificationCode(CommonLib.getInstance().getObeId())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<RequestResponse> verificationVerificationCode(String password, String codes) {
        logger.debug("verificationVerificationCode:  password" + password + "    codes:" + codes);
        return RetrofitServiceFactory.getGuardService().verificationVerificationCode(new VerificationVerificationCodeArgs(CommonLib.getInstance().getObeId(), password, codes))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<RequestResponse> setDigitPassword(String digitState, String digitPassword) {
        return RetrofitServiceFactory.getGuardService().setDigitPassword(new SetDigitPasswordRequest(CommonLib.getInstance().getObeId(), digitState, digitPassword))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }


}
