package com.dudu.workflow.common;

import com.dudu.commonlib.CommonLib;
import com.dudu.persistence.UserMessage.UserMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/2/16.
 */
public class CommonParams {

    private static CommonParams mInstance = new CommonParams();

    private Logger logger = LoggerFactory.getLogger("CommonParams");

    private UserMessage user = new UserMessage();

    public static CommonParams getInstance() {
        return mInstance;
    }

    public void init() {
        DataFlowFactory.getUserMessageFlow().obtainUserMessage()
                .subscribeOn(Schedulers.newThread())
                .subscribe(user -> {
                    CommonParams.this.user = user;
                }, error -> {
                    logger.error("init", error);
                });

    }

    public UserMessage getUserMessage() {
        if (user == null) {
            user = new UserMessage();
            user.setObeId(Long.parseLong(CommonLib.getInstance().getObeId()));
        }
        return user;
    }
}

