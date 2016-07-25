package com.dudu.commonlib.utils;

import android.content.Context;
import android.content.Intent;

/**
 * Created by dengjun on 2016/5/9.
 * Description :
 */
public class BroadcastUtils {
    public static void sendBroadcast(Context context, String action){
        context.sendBroadcast(new Intent(action));
    }
}
