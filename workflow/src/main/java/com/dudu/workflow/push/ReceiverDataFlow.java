package com.dudu.workflow.push;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2016/2/22.
 */
public class ReceiverDataFlow {
    private static ReceiverDataFlow mInstance = new ReceiverDataFlow();
    private PushService pushService;

    private Logger log = LoggerFactory.getLogger("workFlow.webSocket");

    public static ReceiverDataFlow getInstance(){
        return mInstance;
    }

    private ReceiverDataFlow(){
        pushService = new PushService();
    }



    public void init(){
        pushService.init();
    }

    public void release(){
        pushService.release();
    }


    public void reConnect(){
        pushService.reConnectWebSocket(0);
    }
}
