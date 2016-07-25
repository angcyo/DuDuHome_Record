package com.dudu.workflow.app;

import com.dudu.commonlib.CommonLib;
import com.dudu.rest.common.IpUtils;
import com.dudu.rest.common.RetrofitServiceFactory;
import com.dudu.rest.model.CheckUpdateResponse;
import com.dudu.rest.model.GetCallSubAccountResponse;
import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.service.AppService;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/3/22.
 */
public class AppRequestRetrofitImpl implements AppRequest {

    public static AppRequestRetrofitImpl mInstance = new AppRequestRetrofitImpl();

    public static AppRequestRetrofitImpl getInstance() {
        return mInstance;
    }

    @Override
    public Observable<CheckUpdateResponse> checkUpdate() {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(AppService.UPGRADE_CHECK, null, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory.getAppService()
                .checkUpdate(CommonLib.getInstance().getObeId(), requestBody)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }


    @Override
    public Observable<GetCallSubAccountResponse> getCallSubAccount() {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(AppService.GET_CALL_SUB_ACCOUNT, null, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory.getAppService()
                .getCallSubAccount(CommonLib.getInstance().getObeId(), requestBody)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<CheckUpdateResponse.AppUpdateInfo> checkObdUpdate() {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(AppService.UPGRADE_CHECK, null, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory.getAppService()
                .checkUpdate(CommonLib.getInstance().getObeId(), requestBody)
                .map(checkUpdateResponse -> {
                    if (checkUpdateResponse != null && checkUpdateResponse.result != null) {
                        int size = checkUpdateResponse.result.size();
                        for (int i = 0; i < size; i++) {
                            CheckUpdateResponse.AppUpdateInfo appUpdateInfo = checkUpdateResponse.result.get(i);
                            if ("4".equals(appUpdateInfo.upgradeType)) {
                                return appUpdateInfo;
                            }
                        }
                    }
                    return null;
                });
    }

    @Override
    public Observable<CheckUpdateResponse.AppUpdateInfo> checkLauncherUpdate() {
        RequestBody requestBody = IpUtils.requestArgsToRequestBody(AppService.UPGRADE_CHECK, null, CommonLib.getInstance().getObeId());
        return RetrofitServiceFactory.getAppService()
                .checkUpdate(CommonLib.getInstance().getObeId(), requestBody)
                .map(checkUpdateResponse -> {
                    if (checkUpdateResponse != null && checkUpdateResponse.result != null) {
                        int size = checkUpdateResponse.result.size();
                        for (int i = 0; i < size; i++) {
                            CheckUpdateResponse.AppUpdateInfo appUpdateInfo = checkUpdateResponse.result.get(i);
                            if ("2".equals(appUpdateInfo.upgradeType)) {
                                return appUpdateInfo;
                            }
                        }
                    }
                    return null;
                });
    }
}
