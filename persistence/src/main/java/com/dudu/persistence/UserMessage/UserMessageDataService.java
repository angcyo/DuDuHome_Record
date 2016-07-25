package com.dudu.persistence.UserMessage;

import rx.Observable;

/**
 * Created by Administrator on 2016/3/22.
 */
public interface UserMessageDataService {

    Observable<UserMessage> findUserMessage();

    Observable<UserMessage> saveUserMessage(UserMessage userMessage);
}
