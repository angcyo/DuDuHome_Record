package com.dudu.android.launcher.utils;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.dudu.android.hideapi.SystemPropertiesProxy;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;

public class WifiApAdmin {
    public static final String KEY_WIFI_AP_STATE = "wifi_ap_state";
    public static final String KEY_WIFI_AP_SSID = "wifi_ssid";
    public static final String KEY_WIFI_AP_PASSWORD = "wifi_password";

    public static final String DEFAULT_SSID = "DuDu-AIO";
    public static final String DEFAULT_PASSWORD = "DuDu888888";

    //便携式热点的状态为：10---正在关闭；11---已关闭；12---正在开启；13---已开启

    public static final int WIFI_AP_CLOSING = 10;

    public static final int WIFI_AP_CLOSED = 11;

    public static final int WIFI_AP_OPENING = 12;

    public static final int WIFI_AP_OPENED = 13;

    private static final String WIFI_CONF_DIRECTORY = "nodogsplash";

    private static final String WIFI_CONF_NAME = "nodogsplash.conf";

    private static WifiManager mWifiManager = null;

    private static Logger log_init = LoggerFactory.getLogger("init.wifi.ap");

    private static WifiSettingStateCallback wifiSettingStateCallback;

    /**
     * 设置WIFI热点密码
     *
     * @param password
     */
    public static boolean setWifiApPassword(Context context, String password) {
        log_init.debug("热点:设置密码");
        SharedPreferencesUtils.putStringValue(context, KEY_WIFI_AP_PASSWORD, password);
        return startWifiAp(context, SharedPreferencesUtils.getStringValue(context,
                KEY_WIFI_AP_SSID, DEFAULT_SSID), password);
    }

    private static boolean isWifiConfiged(Context context) {
        File dir = new File(FileUtils.getExternalStorageDirectory(), WIFI_CONF_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, WIFI_CONF_NAME);
        if (!file.exists()) {
            log_init.debug("热点配置文件不存在");
            return false;
        }
        log_init.debug("热点配置文件存在");
        return true;
    }

    private static boolean initWifiConfig(Context context) {
        File dir = new File(FileUtils.getExternalStorageDirectory(), WIFI_CONF_DIRECTORY);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        log_init.debug("创建热点配置文件");
        File file = new File(dir, WIFI_CONF_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();

                InputStream isAsset = context.getAssets().open(WIFI_CONF_NAME);

                FileUtils.copyFileToSd(isAsset, file);
            } catch (IOException e) {
                log_init.debug("创建热点配置文件：失败", e);
                return false;
            }
        }
        return true;
    }

    public static boolean initWifiApState(final Context context) {
        log_init.debug("热点初始化");
        if (isWifiConfiged(context)) {
            //如果开机已经打开ap，则不再打开
            if (isWifiApEnabled(context)) {
                log_init.debug("热点已经打开");
                startPortal(context);
                return true;
            }
        } else {
            initWifiConfig(context);
            if (isWifiApEnabled(context)) {
                log_init.debug("热点已经打开，需要先关闭");
                closeWifiAp(context);
            }
        }
        delayStartWifiAp(context);
        return true;
    }

    private static void delayStartWifiAp(Context context) {
        closeWifi();
        String ssid = SharedPreferencesUtils.getStringValue(context, KEY_WIFI_AP_SSID, DEFAULT_SSID);
        String password = SharedPreferencesUtils.getStringValue(context, KEY_WIFI_AP_PASSWORD, DEFAULT_PASSWORD);
        startWifiAp(context, ssid, password);
    }

    private static void delayStartWifiAp(Context context, String ssid, String password) {
        closeWifi();
        log_init.debug("打开热点");
        startWifiAp(context, ssid, password);
    }

    public static void startPortal(final Context context) {
        log_init.debug("打开Portal服务");
        SystemPropertiesProxy.getInstance().set(context, "persist.sys.nodog", "start");

        rx.Observable.timer(10, TimeUnit.SECONDS)
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(final Long aLong) {
                        log_init.info("portal打开状态：{}", SystemPropertiesProxy.getInstance().get("persist.sys.nodog", "stop"));
                    }
                }, throwable -> log_init.error("startPortal", throwable));
    }

    private static void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            log_init.debug("关闭wifi");
            mWifiManager.setWifiEnabled(false);
        }
    }

    public static boolean startWifiAp(Context context) {
        log_init.debug("读取配置，然后打开热点");
        String ssid = SharedPreferencesUtils.getStringValue(context, KEY_WIFI_AP_SSID, DEFAULT_SSID);
        String password = SharedPreferencesUtils.getStringValue(context, KEY_WIFI_AP_PASSWORD, DEFAULT_PASSWORD);
        return startWifiAp(context, ssid, password);

    }

    public static void uploadWifiApConfiguration(Context context, String ssid, String password) {
        initWifiApConfiguration(context, ssid, password);
    }

    private static void initWifiApConfiguration(Context context, String ssid, String password) {
        if (!isWifiConfiged(context)) {
            initWifiConfig(context);
        }
        if (isWifiApEnabled(context)) {
            log_init.debug("热点已经打开，需要先关闭");
            closeWifiAp(context);
        }
        delayStartWifiAp(context, ssid, password);
    }

    private static boolean startWifiAp(final Context context, String ssid,
                                       String password) {
        mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        if (startWifiAp(ssid, password)) {
            SharedPreferencesUtil.putBooleanValue(context, KEY_WIFI_AP_STATE, true);
        } else {
            log_init.error("打开热点调用失败，退出");
            return false;
        }
        Observable.timer(2, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                if (isWifiApEnabled(context)) {
                    log_init.debug("打开热点成功，保存状态");
                    startPortal(context);
                    /*if (wifiSettingStateCallback != null) {
                        wifiSettingStateCallback.onWifiStateChanged(true);
                    }*/
                } else {
                    log_init.error("打开热点失败，Portal也没启动，退出");
                  /*  if (wifiSettingStateCallback != null) {
                        wifiSettingStateCallback.onWifiStateChanged(false);
                    }*/
                }
            }
        }, throwable -> log_init.error("startWifiAp", throwable));
        return true;
    }

    private static boolean startWifiAp(String ssid, String password) {
        log_init.debug("反射打开热点{}", ssid);
        Method method;
        try {
            method = mWifiManager.getClass().getMethod("setWifiApEnabled",
                    WifiConfiguration.class, boolean.class);
            WifiConfiguration netConfig = new WifiConfiguration();
            netConfig.SSID = ssid;
            if (password == null || (password != null && password.isEmpty())) {
                log_init.debug("没有密码，清除掉配置");
                netConfig.allowedAuthAlgorithms.clear();
                netConfig.allowedProtocols.clear();
                netConfig.allowedKeyManagement.clear();
                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                netConfig.allowedPairwiseCiphers.clear();
                netConfig.allowedGroupCiphers.clear();
                netConfig.wepKeys[0] = "";
                netConfig.wepTxKeyIndex = 0;
            } else {
                log_init.debug("有密码，设置配置--   热点名称：" + ssid + "    密码：" + password);
                netConfig.preSharedKey = password;
                netConfig.allowedAuthAlgorithms
                        .set(WifiConfiguration.AuthAlgorithm.OPEN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                netConfig.allowedKeyManagement
                        .set(WifiConfiguration.KeyMgmt.WPA_PSK);
                netConfig.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.CCMP);
                netConfig.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.TKIP);
                netConfig.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.CCMP);
                netConfig.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.TKIP);
            }
            return (Boolean) method.invoke(mWifiManager, netConfig, true);
        } catch (Exception e) {
            log_init.debug("反射打开热点异常", e);
        }
        return false;
    }

    public static void closeWifiAp(Context context) {
        log_init.debug("关闭portal。。。");
        SystemPropertiesProxy.getInstance().set(context, "persist.sys.nodog", "stop");
        log_init.debug("反射关闭热点");
        if (isWifiApEnabled(context)) {
            mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method setWifiApEnabled;
            try {
                setWifiApEnabled = mWifiManager.getClass().getMethod(
                        "setWifiApEnabled", WifiConfiguration.class, boolean.class);
                setWifiApEnabled.invoke(mWifiManager, null, false);
                SharedPreferencesUtils.putBooleanValue(context, KEY_WIFI_AP_STATE, false);
            } catch (Exception e) {
                log_init.debug("反射关闭热点异常", e);
            }
        }
    }

    public static boolean isWifiApEnabled(Context context) {
        log_init.debug("反射查询热点");
        try {
            if (mWifiManager == null) {
                mWifiManager = (WifiManager) context
                        .getSystemService(Context.WIFI_SERVICE);
            }
            Method method = mWifiManager.getClass()
                    .getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(mWifiManager);
        } catch (Exception e) {
            log_init.debug("反射查询热点异常", e);
        }

        return false;
    }

    public static WifiConfiguration getWifiApConfiguration(Context context) {
        try {
            if (mWifiManager == null) {
                mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            }
            Method method = mWifiManager.getClass().getMethod(
                    "getWifiApConfiguration");
            return (WifiConfiguration) method.invoke(mWifiManager);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static WifiApBean obtainPassword(Context context) {
        WifiConfiguration wifiConfiguration = getWifiApConfiguration(context);
        WifiApBean wifiApBean = new WifiApBean();
        if (wifiConfiguration != null) {
            wifiApBean.setSsid(wifiConfiguration.SSID);
            wifiApBean.setPassword(wifiConfiguration.preSharedKey);
        }
        return wifiApBean;
    }

    public static boolean saveWifiApConfiguration(Context context, WifiConfiguration config) {
        try {
            if (mWifiManager == null) {
                mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            }
            Method method = mWifiManager.getClass().getMethod(
                    "setWifiApConfiguration", WifiConfiguration.class);
            return (Boolean) method.invoke(mWifiManager, config);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void saveWifiApConfiguration(Context context, String ssid, String password) {
        WifiConfiguration wifiConfiguration = getWifiApConfiguration(context);
        if (wifiConfiguration != null) {
            wifiConfiguration.SSID = ssid;
            wifiConfiguration.preSharedKey = password;
            boolean isSuccess = saveWifiApConfiguration(context, wifiConfiguration);
            if (isSuccess) {
                log_init.debug("保存成功");
            } else {
                log_init.debug("保存失败");
            }
        }

    }


    //wifi 密码设置后的回调

    public interface WifiSettingStateCallback {
        void onWifiStateChanged(boolean open);
    }

    /*   public static void onWifiSettingStateCallback(WifiSettingStateCallback callback) {
           wifiSettingStateCallback = callback;
       }*/
    public static boolean obtainLocalSaveFlowState(Context context) {
        return SharedPreferencesUtils.getBooleanValue(context, KEY_WIFI_AP_STATE, false);
    }

    public static void saveSsidAndPassword(Context context, String ssid, String password) {
        SharedPreferencesUtil.putStringValue(context, KEY_WIFI_AP_SSID, ssid);
        SharedPreferencesUtil.putStringValue(context, KEY_WIFI_AP_PASSWORD, password);
    }

    public static void saveSsidAndPassword(Context context) {
        WifiConfiguration wifiConfiguration = getWifiApConfiguration(context);
        if (wifiConfiguration != null) {
            saveSsidAndPassword(context, wifiConfiguration.SSID, wifiConfiguration.preSharedKey);
        }
    }

    public static void saveLocalSaveFlowState(Context context, boolean isOpen) {
        SharedPreferencesUtil.putBooleanValue(context, KEY_WIFI_AP_STATE, isOpen);
    }

}
