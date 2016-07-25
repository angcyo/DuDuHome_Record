package com.dudu.monitor.tirepressure;

import com.dudu.monitor.active.ActiveDeviceManage;
import com.dudu.monitor.tirepressure.model.TirePressureFactory;
import com.dudu.monitor.tirepressure.model.TirePressureUpload;
import com.dudu.network.NetworkManage;
import com.dudu.network.message.TirePressureDataUpload;
import com.dudu.persistence.realm.RealmCallBack;
import com.dudu.persistence.realmmodel.tirepressure.TirePressureDataRealm;
import com.dudu.workflow.tpms.TPMSFlow;
import com.dudu.workflow.tpms.TPMSInfo;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by dengjun on 2016/4/18.
 * Description :
 */
public class TirePressureManage {
    private Subscription tirePressureSubscription;

    private Logger log = LoggerFactory.getLogger("monitor.TirePressure");

    private static TirePressureManage mInstance = new TirePressureManage();

    public static TirePressureManage getInstance() {
        return mInstance;
    }

    private TirePressureManage() {

    }

    public void init() {
        log.info("初始化TirePressureManage");
        tirePressureSubscription = TPMSFlow
                .TPMSWarnInfoStream()
                .subscribeOn(Schedulers.io())
                .doOnNext(tpmsWarnInfo1 -> {
                    saveTirePressureData(tpmsWarnInfo1);
                })
                .map(tpmsWarnInfo -> new TirePressureUpload(tpmsWarnInfo))
                .subscribe(tirePressureUpload -> {
                    try {
                        uploadTirePressureData(tirePressureUpload);
                    } catch (Exception e) {
                        log.error("异常", e);
                    }
                }, throwable -> {
                    log.error("异常", throwable);
                });

       /* Observable
                .interval(5, 30, TimeUnit.SECONDS)
                .subscribe(l->{
                    test();
                },throwable -> {
                    log.error("异常", throwable);
                });*/
    }

    public void release() {
        if (tirePressureSubscription != null && !tirePressureSubscription.isUnsubscribed()) {
            tirePressureSubscription.unsubscribe();
        }
    }

    private void uploadTirePressureData(TirePressureUpload tirePressureUpload) {
        log.debug("胎压数据：{}", new Gson().toJson(tirePressureUpload));
        if (ActiveDeviceManage.getInstance().isDeviceActived()) {
            NetworkManage.getInstance().sendMessage(new TirePressureDataUpload(new Gson().toJson(tirePressureUpload)));
        }
    }

    private void saveTirePressureData(TPMSInfo tpmsWarnInfo) {
        TirePressureFactory.saveTirePressureData(tpmsWarnInfo, new RealmCallBack<TirePressureDataRealm, Exception>() {
            @Override
            public void onRealm(TirePressureDataRealm result) {
                log.debug("保存的胎压数据：位置：{}", result.getPostion());
            }

            @Override
            public void onError(Exception error) {

            }
        });
    }

    private void test() {
        uploadTirePressureData(new TirePressureUpload());
        TPMSInfo tpmsWarnInfo = new TPMSInfo();
        saveTirePressureData(tpmsWarnInfo);
    }
}
