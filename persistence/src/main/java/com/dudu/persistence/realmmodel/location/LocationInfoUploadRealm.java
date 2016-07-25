package com.dudu.persistence.realmmodel.location;

import io.realm.RealmObject;

/**
 * Created by dengjun on 2016/4/7.
 * Description :
 */
public class LocationInfoUploadRealm extends RealmObject{
//    @PrimaryKey
//    private String key;

    private String jsonString;

    public LocationInfoUploadRealm() {
    }

    public LocationInfoUploadRealm(String jsonString) {
        this.jsonString = jsonString;
//        key = KeyContants.LOCATION_INFO_UPLOAD;
    }

//    public String getKey() {
//        return key;
//    }
//
//    public void setKey(String key) {
//        this.key = key;
//    }

    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }
}
