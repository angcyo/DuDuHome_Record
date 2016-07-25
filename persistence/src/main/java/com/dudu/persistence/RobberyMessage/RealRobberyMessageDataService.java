package com.dudu.persistence.RobberyMessage;

import android.util.Log;

import com.dudu.commonlib.CommonLib;
import com.dudu.persistence.rx.RealmObservable;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/4/21.
 */
public class RealRobberyMessageDataService implements RobberyMessageDataService {
    private static final String TAG = "RealRobberyMessage";

    public RealRobberyMessageDataService() {
    }

    @Override
    public Observable<RobberyMessage> findRobberyMessage() {
        return RealmObservable.object(new Func1<Realm, RealRobberyMessage>() {
            @Override
            public RealRobberyMessage call(Realm realm) {
                RealmResults<RealRobberyMessage> robberyMessageList = realm.where(RealRobberyMessage.class).findAll();
                if (robberyMessageList.size() > 0) {
                    Log.v(TAG, "从数据库获取防劫的信息成功");
                    return robberyMessageList.first();
                } else {
                    RealRobberyMessage realRobberyMessage = new RealRobberyMessage();
                    realRobberyMessage.setObied(CommonLib.getInstance().getObeId());
                    return realRobberyMessage;
                }
            }
        }).map(new Func1<RealRobberyMessage, RobberyMessage>() {
            @Override
            public RobberyMessage call(RealRobberyMessage realRobberyMessage) {
                return robberyMessageFromReal(realRobberyMessage);
            }

        });
    }

    @Override
    public Observable<RobberyMessage> saveRobberyMessage(final RobberyMessage robberyMessage) {
        return RealmObservable.object(new Func1<Realm, RealRobberyMessage>() {
            @Override
            public RealRobberyMessage call(Realm realm) {
                RealRobberyMessage realRobberyMessage = new RealRobberyMessage();
                realRobberyMessage.setObied(robberyMessage.getObied());
                realRobberyMessage.setRobberySwitch(robberyMessage.isRobberySwitch());
                realRobberyMessage.setRotatingSpeed(robberyMessage.getRotatingSpeed());
                realRobberyMessage.setOperationNumber(robberyMessage.getOperationNumber());
                realRobberyMessage.setCompleteTime(robberyMessage.getCompleteTime());
                return realm.copyToRealmOrUpdate(realRobberyMessage);
            }
        }).map(new Func1<RealRobberyMessage, RobberyMessage>() {
            @Override
            public RobberyMessage call(RealRobberyMessage realRobberyMessage) {
                return new RobberyMessage(realRobberyMessage);
            }
        });
    }

    private RobberyMessage robberyMessageFromReal(RealRobberyMessage realRobberyMessage) {
        RobberyMessage robberyMessage = new RobberyMessage();
        robberyMessage.setObied(realRobberyMessage.getObied());
        robberyMessage.setRobberySwitch(realRobberyMessage.isRobberySwitch());
        robberyMessage.setRotatingSpeed(realRobberyMessage.getRotatingSpeed());
        robberyMessage.setOperationNumber(realRobberyMessage.getOperationNumber());
        robberyMessage.setCompleteTime(realRobberyMessage.getCompleteTime());
        return robberyMessage;
    }
}
