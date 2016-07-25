package com.dudu.map;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.dudu.aios.ui.map.AddressSearchActivity;
import com.dudu.android.hideapi.SystemPropertiesProxy;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.android.launcher.utils.Utils;
import com.dudu.commonlib.CommonLib;
import com.dudu.navi.entity.Navigation;

/**
 * Created by lxh on 2016/3/1.
 */
public class GaodeMapAppUtil {

    private static final String AMAP = "amap";

    private static final String AUTO = "auto";

    public static void startNavi(Navigation navigation) {

        switch (getAppType()) {
            case AMAP:
                startNavi_amap(CommonLib.getInstance().getContext(), navigation);
                break;
            case AUTO:
                startNavi_auto(CommonLib.getInstance().getContext(), navigation);
                break;
        }

    }

    public static void exitNavi() {
        switch (getAppType()) {
            case AMAP:
                exitNavi_amap(CommonLib.getInstance().getContext());
                break;
            case AUTO:
                exitNavi_auto(CommonLib.getInstance().getContext());
                break;
        }
    }

    public static void closeNaviVoice() {
        switch (getAppType()) {
            case AMAP:
                closeNaviVoice_amap(CommonLib.getInstance().getContext());
                break;
            case AUTO:
                closeNaviVoice_auto(CommonLib.getInstance().getContext());
                break;
        }
    }


    public static void startNaviNightMode() {
        switch (getAppType()) {
            case AMAP:
                startNaviNightMode_amap(CommonLib.getInstance().getContext());
                break;
            case AUTO:
                startNaviNightMode_auto(CommonLib.getInstance().getContext());
                break;
        }
    }

    public static void startNaviDayMode() {
        switch (getAppType()) {
            case AMAP:
                startNaviDayMode_amap(CommonLib.getInstance().getContext());
                break;
            case AUTO:
                startNaviDayMode_auto(CommonLib.getInstance().getContext());
                break;
        }
    }


    public static void exitGapdeApp() {
        switch (getAppType()) {
            case AMAP:
                exitGapdeApp_amap(CommonLib.getInstance().getContext());
                break;
            case AUTO:
                exitGapdeApp_auto(CommonLib.getInstance().getContext());
                break;
        }
        ActivitiesManager.getInstance().closeTargetActivity(AddressSearchActivity.class);
    }

    public static void openNaviBroadcast() {
        switch (getAppType()) {
            case AMAP:
                openNaviBroadcast_amap(CommonLib.getInstance().getContext());
                break;
            case AUTO:
                openNaviBroadcast_auto(CommonLib.getInstance().getContext());
                break;
        }
    }

    public static void closeNaviBroadcast() {
        switch (getAppType()) {
            case AMAP:
                closeNaviBroadcast_amap(CommonLib.getInstance().getContext());
                break;
            case AUTO:
                closeNaviBroadcast_auto(CommonLib.getInstance().getContext());
                break;
        }
    }

    public static void openGaode() {

        String packageName = "com.autonavi.minimap";
        if (getAppType().equals(AUTO)) {
            packageName = "com.autonavi.amapauto";
        }
        Utils.startThirdPartyApp(CommonLib.getInstance().getContext(), packageName);
    }

    public static String getAppType() {
        return SystemPropertiesProxy.getInstance().get("persist.sys.amap.ver", "amap");
    }

    public static void startNavi_auto(Context context, Navigation navigation) {

//        Log.d("navi", "startNavi : " + navigation.getDestination().latitude + " , " + navigation.getDestination().longitude + " " + navigation.getDriveMode().getnCode());

        String url = "androidauto://navi?sourceApplication=duduLauncher&lat="
                + navigation.getDestination().latitude + "&lon=" + navigation.getDestination().longitude + "&dev=0&style=" + navigation.getDriveMode().getnCode();

//        String url2 = "androidauto://route?sourceApplication=duduLauncher&slat=" + Monitor.getInstance().getCurrentLocation().getLatitude()
//                + "&slon=" + Monitor.getInstance().getCurrentLocation().getLongitude() + "&sname=abc"
//                + "&dlat=" + navigation.getDestination().latitude + "&dlon=" + navigation.getDestination().longitude + "&dname=def&dev=0&m=0";

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.autonavi.amapauto");
        context.startActivity(intent);

    }


    public static void exitNavi_auto(Context context) {

        Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("androidauto://naviExit?sourceApplication=launcher"));
        intent.setPackage("com.autonavi.amapauto");
        context.sendBroadcast(intent);


    }

    public static void exitGapdeApp_auto(Context context) {
        Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("androidauto://appExit?sourceApplication=launcher"));
        intent.setPackage("com.autonavi.amapauto");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    public static void startNaviDayMode_auto(Context context) {
        Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("androidauto://mapOpera?sourceApplication=launcher&switchNightMode=0"));
        intent.setPackage("com.autonavi.amapauto");
        context.sendBroadcast(intent);

    }

    public static void startNaviNightMode_auto(Context context) {

        Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("androidauto://mapOpera?sourceApplication=launcher&switchNightMode=1"));
        intent.setPackage("com.autonavi.amapauto");
        context.sendBroadcast(intent);

    }


    public static void startNavi3D_auto(Context context) {

        Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("androidauto://mapOpera?sourceApplication=launcher&switchView=2"));
        intent.setPackage("com.autonavi.amapauto");
        context.sendBroadcast(intent);

    }


    public static void openNaviBroadcast_auto(Context context) {

        Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("androidauto://mapOpera?sourceApplication=launcher&traffic=0"));
        intent.setPackage("com.autonavi.amapauto");
        context.sendBroadcast(intent);

    }

    public static void closeNaviBroadcast_auto(Context context) {
        Intent intent = new Intent("android.intent.action.VIEW", android.net.Uri.parse("androidauto://mapOpera?sourceApplication=launcher&traffic=1"));
        intent.setPackage("com.autonavi.amapauto");
        context.sendBroadcast(intent);

    }

    public static void closeNaviVoice_auto(Context context) {

        Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("androidauto://mapOpera?sourceApplication=launcher&openNaviVoice=0"));
        intent.setPackage("com.autonavi.amapauto");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    public static void startNavi_amap(Context context, Navigation navigation) {

        String url = "androidamap://navi?sourceApplication=duduLauncher&lat="
                + navigation.getDestination().latitude + "&lon=" + navigation.getDestination().longitude + "&dev=0&style=" + navigation.getDriveMode().getnCode();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.autonavi.minimap");
        context.startActivity(intent);

    }


    public static void exitNavi_amap(Context context) {

        Intent mIntent = new Intent("com.autonavi.minimap");
        mIntent.putExtra("NAVI", "NAVI_EXIT");
        context.sendBroadcast(mIntent);
    }

    public static void exitGapdeApp_amap(Context context) {
        Intent mIntent = new Intent("com.autonavi.minimap");
        mIntent.putExtra("NAVI", "APP_EXIT");
        context.sendBroadcast(mIntent);
    }


    public static void startNaviDayMode_amap(Context context) {
        Intent mIntent = new Intent("com.autonavi.minimap");
        mIntent.putExtra("NAVI", "OPEN_DAY_MODEL");
        context.sendBroadcast(mIntent);

    }

    public static void startNaviNightMode_amap(Context context) {

        Intent mIntent = new Intent("com.autonavi.minimap");
        mIntent.putExtra("NAVI", "OPEN_NIGHT_MODEL");
        context.sendBroadcast(mIntent);

    }

    public static void startNavi3D_amap(Context context) {
        Intent mIntent = new Intent("com.autonavi.minimap");
        mIntent.putExtra("NAVI", "MAP_3D_MODEL");
        context.sendBroadcast(mIntent);

    }

    public static void openNaviBroadcast_amap(Context context) {
        Intent mIntent = new Intent("com.autonavi.minimap");
        mIntent.putExtra("NAVI", "OPEN_TRAFFIC_BROADCAST");
        context.sendBroadcast(mIntent);

    }

    public static void closeNaviBroadcast_amap(Context context) {
        Intent mIntent = new Intent("com.autonavi.minimap");
        mIntent.putExtra("NAVI", "CLOSE_TRAFFIC_BROADCAST");
        context.sendBroadcast(mIntent);

    }

    public static void closeNaviVoice_amap(Context context) {
        Intent mIntent = new Intent("com.autonavi.minimap");
        mIntent.putExtra("NAVI", "CLOSE_VOICE");
        context.sendBroadcast(mIntent);

    }


}
