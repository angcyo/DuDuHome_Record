package com.dudu.persistence.driving;

import com.dudu.persistence.rx.RealmObservable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.realm.Realm;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/3/8.
 */
public class RealmFaultCodeService implements FaultCodeService {

    private Logger logger = LoggerFactory.getLogger("RealmFaultCodeService");

    @Override
    public Observable<FaultCode> findSwitch(int carCheckType) {
        return RealmObservable.object(new Func1<Realm, RealmFaultCode>() {
            @Override
            public RealmFaultCode call(Realm realm) {
                RealmFaultCode realmFaultCode = realm.where(RealmFaultCode.class)
                        .equalTo(FaultCode.CARCHECKTYPE_ROW, carCheckType)
                        .findFirst();
                if (realmFaultCode == null) {
                    realmFaultCode = new RealmFaultCode();
                    realmFaultCode.setCarCheckType(carCheckType);
                }
                return realmFaultCode;
            }
        }).map(new Func1<RealmFaultCode, FaultCode>() {
            @Override
            public FaultCode call(RealmFaultCode realmResults) {
                FaultCode faultCode = new FaultCode(realmResults);
                logger.debug(faultCode.toString());
                return faultCode;
            }
        });
    }

    @Override
    public Observable<FaultCode> saveSwitch(int type, String codes) {
        return RealmObservable.object(new Func1<Realm, RealmFaultCode>() {
            @Override
            public RealmFaultCode call(Realm realm) {
                RealmFaultCode realmSwitchMessage = new RealmFaultCode();
                realmSwitchMessage.setFaultCode(codes);
                realmSwitchMessage.setCarCheckType(type);
                return realm.copyToRealmOrUpdate(realmSwitchMessage);
            }
        }).map(new Func1<RealmFaultCode, FaultCode>() {
            @Override
            public FaultCode call(RealmFaultCode realmUser) {
                // map to UI object
                return new FaultCode(realmUser);
            }
        });
    }
}
