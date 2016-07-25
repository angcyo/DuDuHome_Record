package com.dudu.workflow.app;

import com.dudu.rest.model.CheckUpdateResponse;
import com.dudu.rest.model.GetCallSubAccountResponse;

import rx.Observable;

/**
 * Created by Administrator on 2016/3/22.
 */
public interface AppRequest {

    public Observable<CheckUpdateResponse> checkUpdate();
    public Observable<CheckUpdateResponse.AppUpdateInfo> checkObdUpdate();
    public Observable<CheckUpdateResponse.AppUpdateInfo> checkLauncherUpdate();
    public Observable<GetCallSubAccountResponse> getCallSubAccount();
}
