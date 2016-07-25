package com.dudu.monitor.obdUpdate.config;

/**
 * Created by dengjun on 2016/5/12.
 * Description :
 */
public class ObdUpdateConstants {
    /* obdBin放置到raw目录下，开发人员需要配置此版本号*/
    public static final String OBD_CONFIG_VERSION = "100.4.17";

    /* 未下载之前obdBin之前，查询时设置默认值*/
    public static final String obdBinOnlineDefaultVersion = "00.0.00";

    /* obdBin文件存储路径*/
    public static final  String obdBinSaveDir = "/sdcard/dudu/obdBin";
    public static final  String obdBinRelativeSaveDir = "/dudu/obdBin";

    /* 本地升级*/
    public static final String obdBinNativeName = "obddownload.bin";
    public static final String obdbinNativeAbPath = obdBinSaveDir + "/"+ obdBinNativeName;
    public static final String obdBinNativeVersionCode = "obdBinNativeVersionCode";
    public static final String nativeCopyObdBinApkFlag = "nativeCopyObdBinApkFlag";

    /* 本地强制升级*/
    public static final String hardUpdateObdBinVersion = "hardUpdateObdBinVersion";

    /* 在线升级*/
    /*网络下载obdbin文件名 */
    public static final String obdBinOnlineName = "obd.bin";
    public static final  String obdOnlineBinSaveDir = "/sdcard/dudu/obdBin/";
    public static final String obdbinOnlineAbPath = obdBinSaveDir +"/"+ obdBinOnlineName;
    public static final String obdBinOnlineVersionCode = "obdBinOnlineVersionCode";
    public static final String downloadObdBinApkFlag = "downloadObdBinApkFlag";


}
