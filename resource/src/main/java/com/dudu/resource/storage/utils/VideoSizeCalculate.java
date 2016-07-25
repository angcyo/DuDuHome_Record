package com.dudu.resource.storage.utils;

import com.dudu.persistence.factory.RealmCallFactory;
import com.dudu.persistence.realm.RealmCallBack;
import com.dudu.persistence.realmmodel.video.VideoEntityRealm;

import io.realm.RealmResults;

/**
 * Created by dengjun on 2016/4/14.
 * Description :
 */
public class VideoSizeCalculate {
    private float allVideoTotalSizeMb = 0;

    public float getAllVideoTotalSizeMb() {
        RealmCallFactory.queryVideoInfo(realmQuery -> realmQuery.findAll(),
                new RealmCallBack<RealmResults<VideoEntityRealm>, Exception>() {
                    @Override
                    public void onRealm(RealmResults<VideoEntityRealm> results) {
                        for (VideoEntityRealm videoEntityRealm : results) {
                            allVideoTotalSizeMb += Float.valueOf(videoEntityRealm.getFileSize());
                        }
                    }

                    @Override
                    public void onError(Exception error) {

                    }
                });
        return allVideoTotalSizeMb;
    }

    public void reset() {
        allVideoTotalSizeMb = 0;
    }
}
