package com.dudu.persistence.UserMessage;

import android.util.Log;

import com.dudu.commonlib.CommonLib;
import com.dudu.persistence.rx.RealmObservable;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by luo zha on 2016/3/22.
 */
public class RealUserMessageDataService implements UserMessageDataService {

    public RealUserMessageDataService() {

    }

    @Override
    public Observable<UserMessage> findUserMessage() {
        return RealmObservable.object(new Func1<Realm, RealUserMessage>() {
            @Override
            public RealUserMessage call(Realm realm) {
                RealmResults<RealUserMessage> userMessageList = realm.where(RealUserMessage.class).findAll();
                if (userMessageList.size() > 0) {
                    Log.v("UserMessageDataService", "从数据库获取密码成功");
                    return userMessageList.first();
                } else {
                    RealUserMessage realUserMessage = new RealUserMessage();
                    realUserMessage.setObeId(Long.parseLong(CommonLib.getInstance().getObeId()));
                    realUserMessage.setCarType(0);
                    return realUserMessage;
                }
            }
        }).map(new Func1<RealUserMessage, UserMessage>() {
            @Override
            public UserMessage call(RealUserMessage realUserMessage) {
                return userMessageFromReal(realUserMessage);
            }
        });
    }

    private UserMessage userMessageFromReal(RealUserMessage realUserMessage) {
        UserMessage usermessage = new UserMessage();
        usermessage.setObeId(realUserMessage.getObeId());
        usermessage.setGesturePassword(realUserMessage.getGesturePassword());
        usermessage.setGesturePasswordSwitchState(realUserMessage.isGesturePasswordSwitchState());
        usermessage.setDigitPassword(realUserMessage.getDigitPassword());
        usermessage.setDigitPasswordSwitchState(realUserMessage.isDigitPasswordSwitchState());
        usermessage.setAudit_state(realUserMessage.getAuditState());
        usermessage.setCarType(realUserMessage.getCarType());
        usermessage.setCarTypeName(realUserMessage.getCarTypeName());
        usermessage.setVehicleModel(realUserMessage.getVehicleModel());
        usermessage.setCarTypeSetted(realUserMessage.isCarTypeSetted());
        return usermessage;
    }

    @Override
    public Observable<UserMessage> saveUserMessage(final UserMessage userMessage) {


        return RealmObservable.object(new Func1<Realm, RealUserMessage>() {
            @Override
            public RealUserMessage call(Realm realm) {
                RealUserMessage realUserMessage = new RealUserMessage();
                realUserMessage.setObeId(userMessage.getObeId());
                realUserMessage.setGesturePassword(userMessage.getGesturePassword());
                realUserMessage.setGesturePasswordSwitchState(userMessage.isGesturePasswordSwitchState());
                realUserMessage.setDigitPassword(userMessage.getDigitPassword());
                realUserMessage.setDigitPasswordSwitchState(userMessage.isDigitPasswordSwitchState());
                realUserMessage.setAuditState(userMessage.getAudit_state());
                realUserMessage.setCarType(userMessage.getCarType());
                realUserMessage.setCarTypeName(userMessage.getCarTypeName());
                realUserMessage.setVehicleModel(userMessage.getVehicleModel());
                realUserMessage.setCarTypeSetted(userMessage.isCarTypeSetted());
                return realm.copyToRealmOrUpdate(realUserMessage);
            }
        }).map(new Func1<RealUserMessage, UserMessage>() {
            @Override
            public UserMessage call(RealUserMessage realUserMessage) {
                return new UserMessage(realUserMessage);
            }
        });
    }
}
