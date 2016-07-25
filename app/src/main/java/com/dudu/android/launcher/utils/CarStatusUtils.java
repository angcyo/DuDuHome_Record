package com.dudu.android.launcher.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.dudu.android.launcher.LauncherApplication;
import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.File.KeyConstants;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by lxh on 15/12/30.
 */
public class CarStatusUtils {
    private static String CARSTATUS_SP = "CARSTATUS_SP";

    private static String CARSTATUS = "CARSTATUS";

    private static Logger logger = LoggerFactory.getLogger("obd.CarStatusUtils");

    public static boolean isDemo = false;

    public static boolean isDemoFired;

    public static void saveCarStatus(boolean status) {
        SharedPreferences sp = LauncherApplication.getContext()
                .getSharedPreferences(CARSTATUS_SP, Context.MODE_PRIVATE);

        sp.edit().putBoolean(CARSTATUS, status).commit();
    }

    public static boolean isCarOnline() {
        SharedPreferences sp = LauncherApplication.getContext()
                .getSharedPreferences(CARSTATUS_SP, Context.MODE_PRIVATE);
        return sp.getBoolean(CARSTATUS, true);
    }

    public static void saveCarIsFire(boolean fired) {
        SharedPreferencesUtil.putBooleanValue(CommonLib.getInstance().getContext(), KeyConstants.KEY_IS_FIRED, fired);
    }

    public static boolean isCarFired() {
        return SharedPreferencesUtil.getBooleanValue(CommonLib.getInstance().getContext(), KeyConstants.KEY_IS_FIRED, false);
    }

    public static Observable<Boolean> isFired() {
        return Observable.just(isCarFiredByFile())
                .subscribeOn(Schedulers.newThread());
    }

    public static boolean isCarFiredByFile() {
        if (isDemo) return isDemoFired;

        boolean fired = false;
        if (new File("/sys/bus/platform/devices/obd_gpio.68/acc_on_int").exists()) {
            final String filename = "/sys/bus/platform/devices/obd_gpio.68/acc_on_int";
            FileReader reader = null;
            try {
                reader = new FileReader(filename);
                char[] buf = new char[15];
                int n = reader.read(buf);
                if (n > 1) {
                    fired = 0 != Integer.parseInt(new String(buf, n - 2, 1));
                }
                logger.debug("ACC ON STATE: " + fired);
            } catch (IOException ex) {
                logger.error("Couldn't read ACC on state from " + filename + ": " + ex);
            } catch (NumberFormatException ex) {
                logger.error("Couldn't read ACC on state from " + filename + ": " + ex);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                    }
                }
            }
        }
        return fired;
    }

    public static void saveWifiIsAvailable(boolean open) {
        SharedPreferencesUtil.putBooleanValue(CommonLib.getInstance().getContext(), KeyConstants.KEY_WIFI_IS_AVAILABLE, open);
    }

    public static boolean isWifiAvailable() {
        return SharedPreferencesUtil.getBooleanValue(CommonLib.getInstance().getContext(), KeyConstants.KEY_WIFI_IS_AVAILABLE, false);
    }
}
