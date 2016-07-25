package com.dudu.drivevideo.utils;

import android.text.TextUtils;

import ch.qos.logback.core.android.SystemPropertiesProxy;

/**
 * Created by dengjun on 2016/5/21.
 * Description :
 */
public class AudioUtils {
    public static boolean isMultiMic() {
        boolean ret = false;
        String mic = SystemPropertiesProxy.getInstance().get("persist.sys.mic.multi", "0");

        if (TextUtils.equals(mic, "0")) {
            ret = false;
        } else if (TextUtils.equals(mic, "1")) {
            ret = true;
        }

        return ret;
    }
}
