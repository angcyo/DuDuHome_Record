package com.dudu.voice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dudu.android.launcher.utils.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lxh on 2016-04-20 10:24.
 */
public class BtContactsBroadcast extends BroadcastReceiver{

    private Logger logger = LoggerFactory.getLogger("voice.btContacts");
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(Constants.BLUETOOTH_SYNC_PHONE_BOOK_END)){

            logger.trace("received bluetooth sync phone book end");
            VoiceManagerProxy.getInstance().updateNativeGrammar();
        }
    }
}
