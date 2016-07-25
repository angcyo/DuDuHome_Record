package com.dudu.service.location;

import com.dudu.persistence.realm.RealmCallBack;
import com.dudu.persistence.realm.MultiValueRealmService;
import com.dudu.persistence.realmmodel.location.LocationInfoUploadRealm;
import com.dudu.resource.location.LocationResource;
import com.dudu.resource.location.loc.LocationListener;
import com.dudu.resource.location.model.LocationInfo;
import com.dudu.resource.location.model.LocationInfoUpload;
import com.dudu.resource.resource.ResourceFactory;
import com.dudu.service.service.IService;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by dengjun on 2016/4/7.
 * Description :
 */
public class LocationService implements IService, LocationListener {
    private LocationResource locationResource;
    private List<String> locationInfoUploadList;

    private Logger log = LoggerFactory.getLogger("lbs.location");

    private MultiValueRealmService<LocationInfoUploadRealm> multiValueRealmService = new MultiValueRealmService<LocationInfoUploadRealm>(LocationInfoUploadRealm.class);
    private Subscription saveSubscription;

    @Override
    public void start() {
        locationInfoUploadList = Collections.synchronizedList(new ArrayList<String>());

        locationResource = ResourceFactory.getInstance().getResourceContainer().getInstance(LocationResource.class);
        locationResource.setLocationListener(this);
        locationResource.init();

        saveLocationPeriod();
    }

    @Override
    public void stop() {
        locationResource.release();
        locationInfoUploadList.clear();

        cancerSaveLocationPeriod();
    }

    @Override
    public void onLocationResult(Object locationInfo) {
        if (locationInfo instanceof LocationInfo){
            log.debug("定位数据：{}", new Gson().toJson(new LocationInfoUpload((LocationInfo)locationInfo)));
            locationInfoUploadList.add(new Gson().toJson(new LocationInfoUpload((LocationInfo)locationInfo)));
        }
    }



    private void saveLocationPeriod(){
        log.info("interval.io.create saveLocationAction");
        saveSubscription = Observable
                .interval(1, 30, TimeUnit.SECONDS, Schedulers.io())
                .subscribe(l->{
                    log.error("interval.io saveLocationAction");
                    if (locationInfoUploadList.size() > 0){
                        saveLocationAction();
                    }
                }, throwable -> {
                    log.error("interval.io 异常", throwable);
                });
    }

    private void saveLocationAction(){
        log.debug("保存位置信息");
        try {
            multiValueRealmService.save(new LocationInfoUploadRealm(getLocationInfoUpload().toJsonString()), new RealmCallBack<LocationInfoUploadRealm, Exception>() {
                @Override
                public void onRealm(LocationInfoUploadRealm result) {
                    if (result != null){
                        log.debug("保存的位置信息：{}", result.getJsonString());
                    }
                }

                @Override
                public void onError(Exception error) {

                }
            });

            /*realmServcie.find(new RealmCallBack<RealmResults<LocationInfoUploadRealm>>() {
                @Override
                public void onRealm(RealmResults<LocationInfoUploadRealm> result) {
                    Iterator<LocationInfoUploadRealm> iterator  = result.iterator();
                    while (iterator.hasNext()){
                        log.debug(" result size : {} 查找的位置信息：{}", result.size(), iterator.next().getJsonString());
                    }

                }
            });*/
        } catch (Exception e) {
            log.error("异常", e);
        }
    }

    private void cancerSaveLocationPeriod(){
        saveSubscription.unsubscribe();
        locationInfoUploadList.clear();
    }

    public JSONArray getLocationJSONArray(){
        JSONArray locationInfoArray = null;
        try {
            if (locationInfoUploadList.size() > 0) {
                locationInfoArray = new JSONArray();
                for (String locationInfoUpload : locationInfoUploadList) {
//                DuduLog.d("monitor-位置信息："+ gson.toJson(locationInfo).toString() );
                    if (locationInfoUpload != null) {
                        locationInfoArray.put(new JSONObject(locationInfoUpload));
                    }
                }
                locationInfoUploadList.clear();
            }
        } catch (JSONException e) {
            log.error("monitor-发送位置信息异常：" + e);
        }
        return locationInfoArray;
    }

    private  com.dudu.network.message.LocationInfoUpload getLocationInfoUpload(){
        return new com.dudu.network.message.LocationInfoUpload(getLocationJSONArray());
    }

}
