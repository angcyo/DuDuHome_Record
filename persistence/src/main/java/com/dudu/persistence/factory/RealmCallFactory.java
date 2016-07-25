package com.dudu.persistence.factory;

import com.dudu.persistence.realm.MultiValueRealmService;
import com.dudu.persistence.realm.RealmCallBack;
import com.dudu.persistence.realm.RealmObjectQueryCondition;
import com.dudu.persistence.realm.RealmQueryCondition;
import com.dudu.persistence.realm.RealmResultsQueryCondition;
import com.dudu.persistence.realm.SingleValueRealmServcie;
import com.dudu.persistence.realmmodel.picture.PictureEntityRealm;
import com.dudu.persistence.realmmodel.tirepressure.TirePressureDataRealm;
import com.dudu.persistence.realmmodel.video.VideoEntity;
import com.dudu.persistence.realmmodel.video.VideoEntityRealm;
import com.dudu.persistence.rx.RealmManage;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;

/**
 * Created by dengjun on 2016/4/12.
 * Description :
 */
public class RealmCallFactory {
    /* 同步保存录制视频信息方法*/
    public static void saveVideoInfo(boolean cameraFlag, String absolutePath, RealmCallBack realmCallBack) {
        MultiValueRealmService<VideoEntityRealm> multiValueRealmService = new MultiValueRealmService<VideoEntityRealm>(VideoEntityRealm.class);
        multiValueRealmService.save(RealmModelFactory.createVideoEntityRealm(cameraFlag, absolutePath), realmCallBack);
    }

    /* 同步保存录制视频信息方法*/
    public static void saveVideoInfo(boolean cameraFlag, String absolutePath, String thumbnailAbsolutePath, RealmCallBack realmCallBack) {
        MultiValueRealmService<VideoEntityRealm> multiValueRealmService = new MultiValueRealmService<VideoEntityRealm>(VideoEntityRealm.class);
        multiValueRealmService.save(RealmModelFactory.createVideoEntityRealm(cameraFlag, absolutePath, thumbnailAbsolutePath), realmCallBack);
    }

    /* 异步保存录制视频信息方法*/
    public static void saveVideoInfoAsync(boolean cameraFlag, String absolutePath, RealmCallBack realmCallBack) {
        MultiValueRealmService<VideoEntityRealm> multiValueRealmService = new MultiValueRealmService<VideoEntityRealm>(VideoEntityRealm.class);
        multiValueRealmService.saveAsync(RealmModelFactory.createVideoEntityRealm(cameraFlag, absolutePath), realmCallBack);
    }


    /* 查询录像信息*/
    public static void queryVideoInfo(RealmQueryCondition<RealmResults<VideoEntityRealm>> queryCondition, RealmCallBack realmCallBack) {
        MultiValueRealmService<VideoEntityRealm> multiValueRealmService = new MultiValueRealmService<VideoEntityRealm>(VideoEntityRealm.class);
        multiValueRealmService.findMulti(queryCondition, realmCallBack);
    }


    /* 查询录像信息*/
    public static void queryVideoEntityInfo(RealmQueryCondition<RealmResults<VideoEntityRealm>> queryCondition, RealmCallBack realmCallBack) {
        MultiValueRealmService<VideoEntityRealm> multiValueRealmService = new MultiValueRealmService<VideoEntityRealm>(VideoEntityRealm.class);
        multiValueRealmService.findMulti(queryCondition, new RealmCallBack<RealmResults<VideoEntityRealm>, Exception>() {

            @Override
            public void onRealm(RealmResults<VideoEntityRealm> results) {
                if (realmCallBack != null) {
                    realmCallBack.onRealm(RealmModelFactory.getVideoEntityListFromRealmResult(results));
                }
            }

            @Override
            public void onError(Exception error) {
                if (realmCallBack != null) {
                    realmCallBack.onRealm(error);
                }
            }
        });
    }

    /* 查询录像信息  异步方式 适合在UI中调用, */
    public static void queryVideoEntityInfoAsync(RealmQueryCondition<RealmResults<VideoEntityRealm>> queryCondition, RealmCallBack realmCallBack) {
        MultiValueRealmService<VideoEntityRealm> multiValueRealmService = new MultiValueRealmService<VideoEntityRealm>(VideoEntityRealm.class);
        multiValueRealmService.findMultiAsync(queryCondition, new RealmCallBack<RealmResults<VideoEntityRealm>, Exception>() {

            @Override
            public void onRealm(RealmResults<VideoEntityRealm> results) {
                if (realmCallBack != null) {
                    realmCallBack.onRealm(RealmModelFactory.getVideoEntityListFromRealmResult(results));
                }
            }

            @Override
            public void onError(Exception error) {
                if (realmCallBack != null) {
                    realmCallBack.onRealm(error);
                }
            }
        });
    }

    /* 查询录像信息  异步方式 适合在UI中调用, */
    public static void queryVideo(boolean isFace, RealmCallBack<ArrayList<VideoEntity>, Exception> realmCallBack) {
        tran(realm -> {
            try {
                RealmResults<VideoEntityRealm> videoEntityRealms = realm.where(VideoEntityRealm.class).equalTo("cameraFlag", isFace).findAll();
                realmCallBack.onRealm(RealmModelFactory.getVideoEntityListFromRealmResult(videoEntityRealms));
            } catch (Exception e) {
                realmCallBack.onError(e);
            }
        });
    }

    /**
     * 查询录像信息  异步方式 适合在UI中调用, 请注意,回调是在后台线程调用执行的
     */
    public static void queryVideoAsync(boolean isFace, RealmCallBack<ArrayList<VideoEntity>, Exception> realmCallBack) {
        tranAsync(realm -> {
            RealmResults<VideoEntityRealm> videoEntityRealms = realm.where(VideoEntityRealm.class).equalTo("cameraFlag", isFace).findAll();
            realmCallBack.onRealm(RealmModelFactory.getVideoEntityListFromRealmResult(videoEntityRealms));
        }, new Realm.Transaction.Callback() {
            @Override
            public void onError(Exception e) {
                realmCallBack.onError(e);
            }
        });
    }

    public static void queryVideoInfoOne(RealmQueryCondition<VideoEntityRealm> queryCondition, RealmCallBack realmCallBack) {
        MultiValueRealmService<VideoEntityRealm> multiValueRealmService = new MultiValueRealmService<VideoEntityRealm>(VideoEntityRealm.class);
        multiValueRealmService.find(queryCondition, realmCallBack);
    }


    /* 查询录像信息  异步方式 适合在UI中调用*/
    public static void queryVideoInfoAsync(RealmQueryCondition<RealmResults<VideoEntityRealm>> queryCondition, RealmCallBack realmCallBack) {
        MultiValueRealmService<VideoEntityRealm> multiValueRealmService = new MultiValueRealmService<VideoEntityRealm>(VideoEntityRealm.class);
        multiValueRealmService.findMultiAsync(queryCondition, realmCallBack);
    }

    public static void removeOneVideo(RealmQueryCondition<VideoEntityRealm> queryCondition, RealmCallBack realmCallBack) {
        MultiValueRealmService<VideoEntityRealm> multiValueRealmService = new MultiValueRealmService<VideoEntityRealm>(VideoEntityRealm.class);
        multiValueRealmService.removeOne(queryCondition, realmCallBack);
    }


    /* 修改录像信息*/
    public static void modifyVideoInfo(RealmQueryCondition<VideoEntityRealm> queryCondition, RealmCallBack realmCallBack) {
        MultiValueRealmService<VideoEntityRealm> multiValueRealmService = new MultiValueRealmService<VideoEntityRealm>(VideoEntityRealm.class);
        multiValueRealmService.modify(queryCondition, realmCallBack);
    }

    /* 删除录像信息*/
    public static void deleteVideoInfo(RealmQueryCondition<VideoEntityRealm> queryCondition, RealmCallBack realmCallBack) {
        MultiValueRealmService<VideoEntityRealm> multiValueRealmService = new MultiValueRealmService<VideoEntityRealm>(VideoEntityRealm.class);
        multiValueRealmService.removeOne(queryCondition, realmCallBack);
    }


    /* 同步保存拍照照片信息方法*/
    public static void savePictureInfo(boolean cameraFlag, String absolutePath, RealmCallBack realmCallBack) {
        MultiValueRealmService<PictureEntityRealm> multiValueRealmService = new MultiValueRealmService<PictureEntityRealm>(PictureEntityRealm.class);
        multiValueRealmService.save(RealmModelFactory.createPictureEntityRealm(cameraFlag, absolutePath), realmCallBack);
    }

    /* 异步保存拍照照片信息方法*/
    public static void savePictureInfoAysnc(boolean cameraFlag, String absolutePath, RealmCallBack realmCallBack) {
        MultiValueRealmService<PictureEntityRealm> multiValueRealmService = new MultiValueRealmService<PictureEntityRealm>(PictureEntityRealm.class);
        multiValueRealmService.saveAsync(RealmModelFactory.createPictureEntityRealm(cameraFlag, absolutePath), realmCallBack);
    }

    /* 查询照片信息  异步方式 适合在UI中调用, */
    public static void queryPictureEntityInfoAsync(RealmQueryCondition<RealmResults<PictureEntityRealm>> queryCondition, RealmCallBack realmCallBack) {
        MultiValueRealmService<PictureEntityRealm> multiValueRealmService = new MultiValueRealmService<PictureEntityRealm>(PictureEntityRealm.class);
        multiValueRealmService.findMultiAsync(queryCondition, new RealmCallBack<RealmResults<PictureEntityRealm>, Exception>() {

            @Override
            public void onRealm(RealmResults<PictureEntityRealm> results) {
                if (realmCallBack != null) {
                    realmCallBack.onRealm(RealmModelFactory.getPictureEntityListFromRealResuls(results));
                }
            }

            @Override
            public void onError(Exception error) {
                if (realmCallBack != null) {
                    realmCallBack.onRealm(error);
                }
            }
        });
    }

    /* 修改照片信息*/
    public static void modifyPictureInfo(RealmQueryCondition<PictureEntityRealm> queryCondition, RealmCallBack realmCallBack) {
        MultiValueRealmService<PictureEntityRealm> multiValueRealmService = new MultiValueRealmService<PictureEntityRealm>(PictureEntityRealm.class);
        multiValueRealmService.modify(queryCondition, realmCallBack);
    }


    /* 删除照片信息*/
    public static void deletePictureInfo(RealmQueryCondition<PictureEntityRealm> queryCondition, RealmCallBack realmCallBack) {
        MultiValueRealmService<PictureEntityRealm> multiValueRealmService = new MultiValueRealmService<PictureEntityRealm>(PictureEntityRealm.class);
        multiValueRealmService.removeOne(queryCondition, realmCallBack);
    }

    /* 同步查找胎压数据方法*/
    public static void findTirePressureDataSync(RealmCallBack realmCallBack, int position) {
        SingleValueRealmServcie<TirePressureDataRealm> singleValueRealmServcie = new SingleValueRealmServcie<TirePressureDataRealm>(TirePressureDataRealm.class);
        singleValueRealmServcie.find(new RealmObjectQueryCondition<TirePressureDataRealm>() {
            @Override
            public TirePressureDataRealm onCondition(RealmQuery realmQuery) {
                return (TirePressureDataRealm) realmQuery
                        .equalTo(TirePressureDataRealm.POSTION, position)
                        .findFirst();
            }
        }, realmCallBack);
    }

    /* 异步查找胎压数据方法*/
    public static void findTirePressureDataAsync(RealmCallBack realmCallBack) {
        SingleValueRealmServcie<TirePressureDataRealm> singleValueRealmServcie = new SingleValueRealmServcie<TirePressureDataRealm>(TirePressureDataRealm.class);
        singleValueRealmServcie.findAsync(realmCallBack);
    }

    /* 同步查找所有胎压数据方法*/
    public static void findAllTirePressureDatasSync(RealmCallBack<RealmResults<TirePressureDataRealm>, Exception> realmCallBack) {
        MultiValueRealmService<TirePressureDataRealm> singleValueRealmServcie = new MultiValueRealmService<TirePressureDataRealm>(TirePressureDataRealm.class);
        singleValueRealmServcie.findMulti(new RealmResultsQueryCondition<TirePressureDataRealm>() {
            @Override
            public RealmResults<TirePressureDataRealm> onCondition(RealmQuery realmQuery) {
                return realmQuery.findAll();
            }
        }, realmCallBack);
    }

    /* 同步查找所有胎压数据方法*/
    public static Observable<RealmResults<TirePressureDataRealm>> findAllTirePressureDatasSync() {
        SingleValueRealmServcie<TirePressureDataRealm> singleValueRealmServcie = new SingleValueRealmServcie<TirePressureDataRealm>(TirePressureDataRealm.class);
        return singleValueRealmServcie.findAll();
    }

    /* 同步查找胎压数据方法*/
    public static void saveTirePressureDataSync(TirePressureDataRealm tirePressureDataRealm, RealmCallBack realmCallBack) {
        SingleValueRealmServcie<TirePressureDataRealm> singleValueRealmServcie = new SingleValueRealmServcie<TirePressureDataRealm>(TirePressureDataRealm.class);
        singleValueRealmServcie.save(tirePressureDataRealm, realmCallBack);
    }

    /**
     * 同步请求,自动commitTransaction,自动cancelTransaction
     */
    public static void tran(final OnOperate operate) {
        if (operate != null) {
            RealmManage.getRealm().executeTransaction(operate::on);
        }
    }

    public static void findAllVideo(boolean cameraFlag, RealmCallBack<ArrayList<VideoEntity>, Exception> realmCallBack) {
        RealmCallFactory.tran(realm -> {
            RealmResults<VideoEntityRealm> videoEntityRealms = realm.where(VideoEntityRealm.class).equalTo("cameraFlag", cameraFlag).findAll();
            if (realmCallBack != null) {
                realmCallBack.onRealm(RealmModelFactory.getVideoEntityListFromRealmResult(videoEntityRealms));
            }
        });
    }


    public static void tranAsync(final OnOperate operate, final Realm.Transaction.Callback callback) {
        tranAsync(operate);
    }

    public static void tranAsync(final OnOperate operate) {
        if (operate != null) {
            RealmManage.getRealm().executeTransaction(realm -> operate.on(realm));
        }
    }


    public interface OnOperate {
        void on(Realm realm);
    }
}
