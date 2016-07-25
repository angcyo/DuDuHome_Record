package com.dudu.rest.service;

import com.dudu.rest.model.GetRobberyStatusResponse;
import com.dudu.rest.model.common.RequestBody;
import com.dudu.rest.model.common.RequestResponse;

import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

/**
 * 防劫相关接口
 * Created by Administrator on 2016/2/15.
 */
public interface RobberyService {
     public  final static String ROBBERY_GET_STATUS="/robbery/getStatus";

    public final static String UPLOAD_ROBBERY_SWITCH="/robbery/updateState";
    /**
     * 防劫相关的开关
     *
     * @param requestBody platform:mirror,obeId:imei码,robberySwitchs:开关状态(4个开关的值用逗号分开 如1,1,1,0)
     * @return
     */
    @POST("/robbery/updateState/{businessId}/mirror")
    public Observable<RequestResponse> robberySwitch(@Path("businessId") String businessId, @Body RequestBody requestBody);

    /**
     * 获取防劫各个开关的状态
     *
     * @param businessId imei码
     * @return
     */
    @POST("/robbery/getStatus/{businessId}/mirror")
    public Observable<GetRobberyStatusResponse> getRobberyState(@Path("businessId") String businessId, @Body RequestBody requestBody);


}
