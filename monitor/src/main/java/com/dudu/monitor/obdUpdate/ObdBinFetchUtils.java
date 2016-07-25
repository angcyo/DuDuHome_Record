package com.dudu.monitor.obdUpdate;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.DataJsonTranslation;
import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;
import com.dudu.commonlib.utils.fastdfs.FastDfsTools;
import com.dudu.fdfs.fastdfs.FileProcessUtil;
import com.dudu.monitor.R;
import com.dudu.monitor.obdUpdate.config.ObdUpdateConstants;
import com.dudu.rest.model.CheckUpdateResponse;
import com.dudu.workflow.common.RequestFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.schedulers.Schedulers;

/**
 * Created by dengjun on 2016/5/12.
 * Description :
 */
public class ObdBinFetchUtils {
    public static Logger log = LoggerFactory.getLogger("car.obdUpdate");

    private boolean isQueryIng = false;

    /**
     * 查询服务器上的obd版本信息，有新版本就下载
     * @param curObdVersion
     */
    public  void queryObdBinVersion(String curObdVersion){
        if (isQueryIng){
            log.info("正在下载，不重复下载");
            return;
        }
        if (getDownloadState() == true){
            log.info("有已经下载好的obdBin，不重复下载");
            return;
        }
        isQueryIng = true;
        RequestFactory
                .getAppRequest()
                .checkObdUpdate()
                .subscribeOn(Schedulers.io())
                .subscribe(appUpdateInfo -> {
                    if (appUpdateInfo != null) {
                        log.info("查询到obdBin版本信息：{}, 当前obdBin版本：{}", DataJsonTranslation.objectToJson(appUpdateInfo), curObdVersion);
                        log.debug("appUpdateInfo: " + appUpdateInfo.fastdfsId + appUpdateInfo.fastdfsGroup + appUpdateInfo.version);
                        judgeToDownload(curObdVersion, appUpdateInfo);
                    }else {
                        log.info("服务未配置obdbin升级---");
                    }
                    isQueryIng = false;
                }, throwable -> {
                    log.error("异常", throwable);
                    isQueryIng = false;
                });
    }


    private static void judgeToDownload(String curObdVersion, CheckUpdateResponse.AppUpdateInfo appUpdateInfo){
        if (!"".equals(curObdVersion)){
            log.info("obdBinVersion:cur-{}, new-{}", curObdVersion, appUpdateInfo.softwareVersion);
            if(compareTwoVersion(appUpdateInfo.softwareVersion, curObdVersion)){
                doDownloadAction(curObdVersion, appUpdateInfo);
            }else {
                log.info("obdBin无需更新");
            }
        }else {
            log.info("当前版本为空，强制下载-------");
            doDownloadAction(curObdVersion, appUpdateInfo);
        }
    }

    private static void doDownloadAction(String curObdVersion, CheckUpdateResponse.AppUpdateInfo appUpdateInfo){
        saveObdVersion(appUpdateInfo.softwareVersion);
        log.debug("尝试下载-----");
        if(downLoadObdBin(appUpdateInfo.fastdfsGroup, appUpdateInfo.fastdfsId) == 0){
            saveDownloadState(true);
        }else {
            log.info("下载失败---");
            saveDownloadState(false);
        }
    }

    private static int downLoadObdBin(String fastdfsGroup, String fastdfsId){
        int downloadResult = -1;
        try {
            String fastPath = FastDfsTools.copyFastDfsConfig(CommonLib.getInstance().getContext());
            FileUtil.createSdcardDir(ObdUpdateConstants.obdBinRelativeSaveDir);
            if (fastPath != null){
                log.info("开始下载obdBin文件------");
                downloadResult =
                        FileProcessUtil.getInstance(fastPath)
                        .downloadFile(fastdfsGroup, fastdfsId, ObdUpdateConstants.obdOnlineBinSaveDir, ObdUpdateConstants.obdBinOnlineName);
                log.debug("obdBin文件下载结果：{}，(0：表示下载成功)", downloadResult);
            }
        } catch (Exception e) {
            log.error("异常：", e);
        }
        return downloadResult;
    }


    public static void saveDownloadState(boolean downloadState){
        SharedPreferencesUtil.putBooleanValue(CommonLib.getInstance().getContext(), ObdUpdateConstants.downloadObdBinApkFlag, downloadState);
    }

    public static boolean getDownloadState(){
        return SharedPreferencesUtil.getBooleanValue(CommonLib.getInstance().getContext(), ObdUpdateConstants.downloadObdBinApkFlag, false);
    }


    private static void saveObdVersion(String obdVersion){
        SharedPreferencesUtil.putStringValue(CommonLib.getInstance().getContext(), ObdUpdateConstants.obdBinOnlineVersionCode, obdVersion);
    }

    public static String getSavedObdVersion(){
        return SharedPreferencesUtil.getStringValue(CommonLib.getInstance().getContext(), ObdUpdateConstants.obdBinOnlineVersionCode, ObdUpdateConstants.obdBinOnlineDefaultVersion);
    }

    public static void saveCopyFlag(boolean copyFlag){
        SharedPreferencesUtil.putBooleanValue(CommonLib.getInstance().getContext(), ObdUpdateConstants.nativeCopyObdBinApkFlag, copyFlag);
    }

    public static boolean getCopyFlag(){
        return SharedPreferencesUtil.getBooleanValue(CommonLib.getInstance().getContext(), ObdUpdateConstants.nativeCopyObdBinApkFlag, false);
    }

    public static void copyObdBin(){
        boolean copyFlag = FileUtil
                .copyFileAndCreateDir(CommonLib.getInstance().getContext().getResources().openRawResource(R.raw.obddownload),
                ObdUpdateConstants.obdBinSaveDir, ObdUpdateConstants.obdBinNativeName);
        if (copyFlag){
            log.info("复制ObdBin成功");
            saveCopyFlag(true);
        }else {
            log.info("复制ObdBin失败");
            saveCopyFlag(false);
        }
    }

    public static boolean compareTwoVersion(String srcVersion, String dstVesion){
        long srcLongVersion = obdVersionStringToLong(srcVersion);
        long dstLongVersion = obdVersionStringToLong(dstVesion);
        return (srcLongVersion > dstLongVersion) ? true: false;
    }

    public static long obdVersionStringToLong(String obdVerison){
        if ("".equals(obdVerison))
            return 0;
        if (obdVerison.endsWith("S"))  obdVerison = obdVerison.substring(0, obdVerison.length()-1);
        String[] obdVersionArray = obdVerison.split("\\.");
        return Long.valueOf(obdVersionArray[0]+obdVersionArray[1]+obdVersionArray[2]);
    }
}
