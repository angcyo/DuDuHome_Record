package com.dudu.monitor.flow;

import android.content.Context;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.DataJsonTranslation;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;
import com.dudu.monitor.active.ActiveDeviceManage;
import com.dudu.monitor.flow.constants.FlowConstants;
import com.dudu.rest.model.flow.FlowSyncConfigRes;
import com.dudu.workflow.common.RequestFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;


public class FlowManage {
    private static FlowManage instance = null;
    private Context mContext;
    private Logger log;

    private FlowService flowService;

    private Subscription flowSyncConfigSubscription;
    private Subscription workerSubscription;


    public static FlowManage getInstance() {
        if (instance == null) {
            synchronized (FlowManage.class) {
                if (instance == null) {
                    instance = new FlowManage();
                }
            }
        }
        return instance;
    }

    public FlowManage() {
        mContext = CommonLib.getInstance().getContext();

        log = LoggerFactory.getLogger("monitor.flowManage");

        flowService = new FlowService();
    }


    public void init() {
        doFlowSyncConfig();

        flowService.init();
    }

    /* 释放资源*/
    public void release() {
        if (flowSyncConfigSubscription != null && !flowSyncConfigSubscription.isUnsubscribed()) {
            flowSyncConfigSubscription.unsubscribe();
        }



        if (workerSubscription != null && !workerSubscription.isUnsubscribed()) {
            workerSubscription.unsubscribe();
        }
    }

    private void doFlowSyncConfig() {
        log.info("interval.io.create 流量同步配置");
        workerSubscription = Observable
                .interval(15, 30*60, TimeUnit.SECONDS, Schedulers.io())
                .subscribe(l->{
                    try {
                        log.debug("interval.io 流量同步配置");
                        doFlowSyncConfigAction();
                    } catch (Exception e) {
                        log.error("interval.io 异常", e);
                    }
                }, throwable -> {
                    log.error("interval.io 异常", throwable);
                });
    }


    private void doFlowSyncConfigAction() {
        if (!ActiveDeviceManage.getInstance().isDeviceActived()){
            delayDoFlowSyncConfigAction(30);
            return;
        }


        flowSyncConfigSubscription = RequestFactory
                .getFlowRequest()
                .flowSyncConfig()
                .subscribeOn(Schedulers.io())
                .subscribe(flowSyncConfigRes -> {
                    log.debug("流量同步配置收到响应：{}", DataJsonTranslation.objectToJson(flowSyncConfigRes));
                    if (flowSyncConfigRes != null && flowSyncConfigRes.resultCode == 0 && flowSyncConfigRes.result != null) {
                        log.debug("流量同步配置信息：{}", DataJsonTranslation.objectToJson(flowSyncConfigRes.result));
                        saveConfig(flowSyncConfigRes.result, CommonLib.getInstance().getContext());
                    }
                }, throwable -> {
                    log.error("异常", throwable);
                    delayDoFlowSyncConfigAction(30);
                });
    }

    private void delayDoFlowSyncConfigAction(int seconds){
        Observable
                .timer(seconds, TimeUnit.SECONDS, Schedulers.io())
                .subscribe(l->{
                    doFlowSyncConfigAction();
                },throwable -> {
                    log.error("interval.io 异常", throwable);
                });
    }


    private void saveConfig(FlowSyncConfigRes.FlowSynConfiguration flowSynConfigurationRes, Context context) {
        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_MONTH_MAX_VALUE,
                flowSynConfigurationRes.getMonthMaxValue());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_FREE_ADD_VALUE,
                flowSynConfigurationRes.getFreeAddValue());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_DAILY_MAX_VALUE,
                flowSynConfigurationRes.getDailyMaxValue());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_UP_LIMIT_MAX_VALUE,
                flowSynConfigurationRes.getUpLimitMaxValue());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_PORTAL_ADDRESS,
                flowSynConfigurationRes.getPortalAddress());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_PORTAL_VERSION,
                flowSynConfigurationRes.getPortalVersion());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_DOWN_LIMIT_MAX_VALUE,
                flowSynConfigurationRes.getDownLimitMaxValue());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_LIFE_TYPE,
                flowSynConfigurationRes.getLifeType());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_UPLOAD_LIMIT,
                flowSynConfigurationRes.getUploadLimit());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_FREE_ADD_TIMES,
                flowSynConfigurationRes.getFreeAddTimes());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_MIDDLE_ARLAM_VALUE,
                flowSynConfigurationRes.getMiddleArlamValue());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_HIGH_ARLAM_VALUE,
                flowSynConfigurationRes.getHighArlamValue());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_LOW_ARLAM_VALUE,
                flowSynConfigurationRes.getLowArlamValue());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_DOWNLOAD_LIMIT,
                flowSynConfigurationRes.getDownloadLimit());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_FREE_ARRIVE_VALUE,
                flowSynConfigurationRes.getFreeArriveValue());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_CLOSER_ARLAM_VALUE,
                flowSynConfigurationRes.getCloseArlamValue());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_FLOW_FREQUENCY,
                flowSynConfigurationRes.getFlowFrequency());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_GPS_FREQUENCU,
                flowSynConfigurationRes.getGpsFrequency());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_PORTAL_COUNT_FREQUENCY,
                flowSynConfigurationRes.getPortalCountFrequency());

//            log.info("FlowUploadResponse剩余流量：{}", flowSynConfigurationRes.getRemainingFlow());
//            SharedPreferencesUtil.putStringValue(mContext,FlowConstants.KEY_REMAINING_FLOW, flowSynConfigurationRes.getRemainingFlow());

        SharedPreferencesUtil.putStringValue(context, FlowConstants.KEY_UPLOAD_FLOW_VALUE, flowSynConfigurationRes.getUploadFlowValue());
    }

}
