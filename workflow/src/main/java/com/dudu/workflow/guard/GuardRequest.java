package com.dudu.workflow.guard;

import com.dudu.rest.model.GetGuardStatusResponse;
import com.dudu.rest.model.common.RequestResponse;

import rx.Observable;

/**
 * Created by Administrator on 2016/2/16.
 */
public interface GuardRequest {

    public Observable<RequestResponse> checkLockCar(boolean lock);

    /**获取防盗状态*/
    public Observable<GetGuardStatusResponse> getLockStatus();
    /**
     * 获取验证码
     * */

    public Observable<RequestResponse> obtainVerificationCode();

    /**
     * 验证验证码
     * */

    public Observable<RequestResponse> verificationVerificationCode(String password,String codes);

    /**
     *设置数字密码
     * */
    public Observable<RequestResponse> setDigitPassword(String digitState,String digitPassword);
}
