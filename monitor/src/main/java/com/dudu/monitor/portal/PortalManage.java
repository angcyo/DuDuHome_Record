package com.dudu.monitor.portal;

import android.content.Context;

import com.dudu.android.hideapi.SystemPropertiesProxy;
import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.DataJsonTranslation;
import com.dudu.commonlib.utils.File.FileUtilsOld;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;
import com.dudu.monitor.active.ActiveDeviceManage;
import com.dudu.monitor.portal.constants.PortalContants;
import com.dudu.workflow.common.RequestFactory;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by dengjun on 2016/3/18.
 * Description :
 */
public class PortalManage {
    private static PortalManage instance = null;


    private Subscription queryPortalInfoSubscription;
    /*portal弹出次数 */
    private int portalCountTmp = 0;

    private Logger log = LoggerFactory.getLogger("monitor.PortalManage");
    private Subscription getPortalVersionSubscription;
    private Subscription getPortalDownloadAddrSubscription;
    private Subscription uploadPortalPopupNumSubscription;
    private Subscription uploadPortalPopupNumTimerSubscription;

    public static PortalManage getInstance() {
        if (instance == null) {
            synchronized (PortalManage.class) {
                if (instance == null) {
                    instance = new PortalManage();
                }
            }
        }
        return instance;
    }

    public PortalManage() {


    }

    public void init() {
        writePortalConfig();
        queryPortalInfo();
        uploadPortalPopupNum();
    }

    public void release() {
        if (getPortalVersionSubscription != null && !getPortalVersionSubscription.isUnsubscribed()) {
            getPortalVersionSubscription.unsubscribe();
        }
        if (getPortalDownloadAddrSubscription != null && !getPortalDownloadAddrSubscription.isUnsubscribed()) {
            getPortalDownloadAddrSubscription.unsubscribe();
        }
        if (uploadPortalPopupNumSubscription != null && !uploadPortalPopupNumSubscription.isUnsubscribed()) {
            uploadPortalPopupNumSubscription.unsubscribe();
        }
        if (queryPortalInfoSubscription != null && !queryPortalInfoSubscription.isUnsubscribed()) {
            queryPortalInfoSubscription.unsubscribe();
        }
        if (uploadPortalPopupNumTimerSubscription != null && !uploadPortalPopupNumTimerSubscription.isUnsubscribed()) {
            uploadPortalPopupNumTimerSubscription.unsubscribe();
        }
    }


    private void writePortalConfig() {
        Observable.timer(0, TimeUnit.SECONDS, Schedulers.io())
                .subscribe((l) -> {
                    writePortalConfig(CommonLib.getInstance().getContext());
                }, throwable -> {
                    log.error("异常", throwable);
                });
    }


    /**
     * 讲htdocs压缩包解压到指定路径
     */
    public void writePortalConfig(Context context) {
        File portalDir = new File(FileUtilsOld.getExternalStorageDirectory(), PortalContants.NODOGSPLASH_NAME);
        if (!portalDir.exists()) {
            portalDir.mkdirs();
        }

        File portalZipDir = new File(portalDir, PortalContants.TEMP_ZIP_FOLDER_NAME);
        if (!portalZipDir.exists()) {
            portalZipDir.mkdirs();
        }

        File portalZipFile = new File(portalZipDir.getPath(), PortalContants.HTDOCS_ZIP_NAME);
        if (!portalZipFile.exists()) {
            try {
                portalZipFile.createNewFile();
                InputStream is = context.getAssets().open(PortalContants.HTDOCS_ZIP_NAME);
                log.info("portal配置  复制文件-----");
                if (FileUtilsOld.copyFileToSd(is, portalZipFile)) {
                    FileUtilsOld.upZipFile(portalZipFile, portalDir.getPath());
                    log.info("portal配置成功-----");
                }
            } catch (Exception e) {
                log.error("异常", e);
            }
        } else {
            log.info("portal  htdocs.zip 已经存在-----");
        }
    }


    private void queryPortalInfo() {
        log.info("interval.io.create queryPortalInfoAction");
        queryPortalInfoSubscription =
                Observable.interval(15, 60, TimeUnit.MINUTES, Schedulers.io())
                        .subscribe((l) -> {
                            log.error("interval.io queryPortalInfoAction");
                            if (ActiveDeviceManage.getInstance().isDeviceActived()) {
                                queryPortalInfoAction();
                            }
                        }, throwable -> {
                            log.error("interval.io 异常", throwable);
                        });
    }

    private void queryPortalInfoAction() {

        getPortalVersionSubscription = RequestFactory.getPortalRequest()
                .getPortalVersion()
                .subscribeOn(Schedulers.io())
                .subscribe(portalVersionResponse -> {
                    try {
                        log.debug("queryPortalInfoAction 收到响应：{}", DataJsonTranslation.objectToJson(portalVersionResponse));

                        if (portalVersionResponse != null && portalVersionResponse.resultCode == 0 && portalVersionResponse.result != null) {
                            String localVersion = SharedPreferencesUtil.getStringValue(CommonLib.getInstance().getContext(), PortalContants.KEY_PORTAL_VERSION, "0");
                            log.info("本地portalVersion：{}", localVersion);
                            if (Integer.valueOf(portalVersionResponse.result.portalVersion) > Integer.valueOf(localVersion)) {
                                getPortalDownloadAddr(portalVersionResponse.result.portalVersion);
                            }
                        }
                    } catch (Exception e) {
                        log.error("queryPortalInfoAction", e);
                    }
                }, throwable -> log.error("queryPortalInfoAction", throwable));
    }


    private void getPortalDownloadAddr(String latestVersion) {
        getPortalDownloadAddrSubscription = RequestFactory.getPortalRequest()
                .getPortalDownloadAddr(latestVersion)
                .subscribeOn(Schedulers.io())
                .subscribe(updatePortalResponse -> {
                    log.debug("getPortalDownloadAddr 收到响应：{}", new Gson().toJson(updatePortalResponse));
                    if (updatePortalResponse != null && updatePortalResponse.resultCode == 0 && updatePortalResponse.result != null) {
                        try {
                            new PortalUpdate()
                                    .refreshPortal(updatePortalResponse.result.getGroup(), updatePortalResponse.result.getUrl(), latestVersion);
                        } catch (Exception e) {
                            log.error("getPortalDownloadAddr", e);
                        }
                    }
                }, throwable -> log.error("getPortalDownloadAddr", throwable));
    }

    public void uploadPortalPopupNum() {
        log.info("interval.io.create uploadPortalPopupNumAction");
        uploadPortalPopupNumTimerSubscription = Observable
                .interval(60, 30 * 60, TimeUnit.SECONDS, Schedulers.io())
                .subscribe((num) -> {
                    log.debug("interval.io uploadPortalPopupNumAction");
                    if (ActiveDeviceManage.getInstance().isDeviceActived()) {
                        uploadPortalPopupNumAction();
                    }
                }, throwable -> {
                    log.error("interval.io 异常", throwable);
                });
    }

    public void uploadPortalPopupNumAction() {
        int periodPortalCount = getPeriodPortalCount();
        if (periodPortalCount > 0) {
            uploadPortalPopupNumSubscription = RequestFactory.getPortalRequest()
                    .uploadPortalPopupNum(periodPortalCount)
                    .subscribeOn(Schedulers.io())
                    .subscribe(requestResponse -> {
                        try {
                            log.debug("uploadPortalPopupNumAction 收到响应：{}", new Gson().toJson(requestResponse));
                        } catch (Exception e) {
                            log.error("uploadPortalPopupNumAction", e);
                        }
                    }, throwable -> log.error("uploadPortalPopupNumAction", throwable));
        }
    }

    private String getPortalTotalNum() {
        SystemPropertiesProxy.getInstance().set(CommonLib.getInstance().getContext(), "persist.sys.nodog", "views");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String portalCount = SystemPropertiesProxy.getInstance().get("persist.sys.views", "0");
        log.info("portal累计弹出次数：{}", portalCount);
        return portalCount;
    }

    private int getPeriodPortalCount() {
        String portalCount = getPortalTotalNum();
        int periodPortalCount = Integer.parseInt(portalCount.trim()) - portalCountTmp;
        portalCountTmp = Integer.parseInt(portalCount.trim());
        log.info("portal周期内弹出次数：{}", periodPortalCount);
        return periodPortalCount;
    }
}
