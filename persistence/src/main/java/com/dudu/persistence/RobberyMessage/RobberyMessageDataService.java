package com.dudu.persistence.RobberyMessage;

import com.dudu.persistence.UserMessage.UserMessage;
import com.dudu.persistence.realm.RealmObjectQueryCondition;

import rx.Observable;

/**
 * Created by Administrator on 2016/4/21.
 */
public interface RobberyMessageDataService {
    Observable<RobberyMessage> findRobberyMessage();

    Observable<RobberyMessage> saveRobberyMessage(RobberyMessage robberyMessage);
}
