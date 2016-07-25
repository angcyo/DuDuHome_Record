package com.dudu.workflow.upgrade;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.DataJsonTranslation;
import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;
import com.dudu.commonlib.utils.VersionTools;
import com.dudu.commonlib.utils.fastdfs.FastDfsTools;
import com.dudu.fdfs.fastdfs.FileProcessUtil;
import com.dudu.rest.model.CheckUpdateResponse;
import com.dudu.workflow.common.RequestFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import rx.schedulers.Schedulers;

/**
 * Created by dengjun on 2016/4/21.
 * Description :
 */
public class LauncherUpgrade {
    public static final  String launcherSaveDir = "/sdcard/dudu/apk/";
    public static final String laucnerVersionCode = "laucnerVersionCode";
    public static final String laucnerApkName = "launcher.apk";
    public static final String laucnerApkAbPath =launcherSaveDir + laucnerApkName;
    public static final String downloadLaucnerApkFlag = "downloadLaucnerApkFlag";


    private static Logger log = LoggerFactory.getLogger("workFlow.LauncherUpgrade");

    public static void installLauncherApk(){
        if (getLaucherCurVersionCode() < getVersionCodeFromSf() && getDownloadFlag() == true){
            log.info("安装下载的launcher.apk {}", laucnerApkAbPath);
            VersionTools.installApp(CommonLib.getInstance().getContext(), new File(laucnerApkAbPath));
        }else {
            log.info("没有可供升级的launcher.apk");
        }
    }

    public static void queryVersionInfo(){
        RequestFactory
                .getAppRequest()
                .checkLauncherUpdate()
                .subscribeOn(Schedulers.io())
                .subscribe(appUpdateInfo -> {
                    log.info("launcher版本号：{}", VersionTools.getAppVersionCode(CommonLib.getInstance().getContext()));
                    log.info("查询的launcher版本信息：{}", DataJsonTranslation.objectToJson(appUpdateInfo));
                    if (appUpdateInfo != null){
                        judgeToDwonload(appUpdateInfo);
                    }
                },throwable -> {
                    log.error("queryVersionInfo 异常", throwable);
                });
    }

    private static  void judgeToDwonload(CheckUpdateResponse.AppUpdateInfo appUpdateInfo){
        if(getLaucherCurVersionCode() < Integer.valueOf(appUpdateInfo.version)){
            log.info("开始下载launcher.apk {}", appUpdateInfo.fastdfsId);
            downLoadLauncherApk(appUpdateInfo);
        }else {
            log.info("launcher已经是最新版本，无需升级");
        }
    }

    private static void downLoadLauncherApk(CheckUpdateResponse.AppUpdateInfo appUpdateInfo){
        if (downLoadAction(appUpdateInfo) == 0){
            log.info("launcher下载成功");
            saveVersionCodeToSf(appUpdateInfo.version);
            saveDownloadFlag(true);

//            installLauncherApk();//后面要放到熄火后安装
        }else {
            log.info("launcher下载失败");
            saveDownloadFlag(false);
        }
    }


    private static int  downLoadAction(CheckUpdateResponse.AppUpdateInfo appUpdateInfo){
        int downloadResult = -1;
        try {
            String fastPath = FastDfsTools.copyFastDfsConfig(CommonLib.getInstance().getContext());
            if (fastPath != null){
                FileUtil.createSdcardDir("/dudu/apk");
                downloadResult = FileProcessUtil
                        .getInstance(fastPath)
                        .downloadFile(appUpdateInfo.fastdfsGroup, appUpdateInfo.fastdfsId, launcherSaveDir, laucnerApkName);
                log.debug("下载结果：{}", downloadResult);
            }
        } catch (IOException e) {
            log.error("异常", e);
        } catch (Exception e) {
            log.error("异常", e);
        }
        return downloadResult;
    }

    private static int getVersionCodeFromSf(){
        return Integer.valueOf(SharedPreferencesUtil.getStringValue(CommonLib.getInstance().getContext(), laucnerVersionCode, "0"));
    }

    private static void saveVersionCodeToSf(String versionCode){
        SharedPreferencesUtil.putStringValue(CommonLib.getInstance().getContext(), laucnerVersionCode, versionCode);
    }

    private static boolean getDownloadFlag(){
        return SharedPreferencesUtil.getBooleanValue(CommonLib.getInstance().getContext(), downloadLaucnerApkFlag, false);
    }

    private static void saveDownloadFlag(boolean downloadFlag){
        SharedPreferencesUtil.putBooleanValue(CommonLib.getInstance().getContext(), downloadLaucnerApkFlag, downloadFlag);
    }

    private static int getLaucherCurVersionCode(){
        return VersionTools.getAppVersionCode(CommonLib.getInstance().getContext());
    }
}
