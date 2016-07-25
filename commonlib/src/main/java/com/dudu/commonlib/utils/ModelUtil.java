package com.dudu.commonlib.utils;

import ch.qos.logback.core.android.SystemPropertiesProxy;

/**
 * Created by lxh on 2016-05-15 15:18.
 */
public class ModelUtil {
    public static final String MODEL_D02 = "d02";

    public static final String MODEL_D03 = "d03";

    public static String getModel() {
        return SystemPropertiesProxy.getInstance().get("persist.sys.model", MODEL_D03);
    }


    public static boolean needVip() {
        if (getModel().equals(MODEL_D02))
            return true;
        return false;
    }

}
