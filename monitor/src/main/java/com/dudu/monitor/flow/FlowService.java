package com.dudu.monitor.flow;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.DataJsonTranslation;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;
import com.dudu.monitor.active.ActiveDeviceManage;
import com.dudu.monitor.flow.claculate.FlowCalculate;
import com.dudu.monitor.flow.constants.FlowConstants;
import com.dudu.monitor.wifi.WifiApAdmin;
import com.dudu.monitor.wifi.contants.WifiContants;
import com.dudu.rest.model.flow.FlowUpload;
import com.dudu.rest.model.flow.FlowUploadRequestRes;
import com.dudu.workflow.common.RequestFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;


public class FlowService {
    private Logger log;

    private FlowCalculate flowCalculate;
    private  int flowUploadPeriod = 30;

    private float uploadFlowValue = 300;
    private float totalUsedFlowTmp = 0;

    private Subscription workerSubscription;
    private Subscription flowUploadSubscription;


    public FlowService() {
        flowCalculate = new FlowCalculate();
        log = LoggerFactory.getLogger("monitor.flowManage");
    }



    public void init(){
        uploadFlowValue = Float.valueOf(SharedPreferencesUtil.getStringValue(CommonLib.getInstance().getContext(), FlowConstants.KEY_UPLOAD_FLOW_VALUE, "300"));
        flowUploadPeriod = Integer.valueOf(SharedPreferencesUtil.getStringValue(CommonLib.getInstance().getContext(), FlowConstants.KEY_FLOW_FREQUENCY, "30"));

        log.info("interval.io.create doFlowCalculate");
        workerSubscription = Observable
                .interval(15, flowUploadPeriod, TimeUnit.SECONDS, Schedulers.io())
                .subscribe(i->{
                    try {
                        log.debug("interval.io doFlowCalculate");
                        doFlowCalculate();
                    } catch (Exception e) {
                        log.error("interval.io 异常", e);
                    }
                }, throwable -> {
                    log.error("interval.io 异常", throwable);
                });

        Observable
                .timer(30,TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(l->{
                    log.info("开机上报0KB流量，获取剩余流量值");
                    doUpdateAction(0);
                }, throwable -> {
                    log.error("异常", throwable);
                });
    }

    public void release(){
        if (flowUploadSubscription != null && !flowUploadSubscription.isUnsubscribed()){
            flowUploadSubscription.unsubscribe();
        }

        if (workerSubscription != null && !workerSubscription.isUnsubscribed()){
            workerSubscription.unsubscribe();
        }
    }



    private void doFlowCalculate(){
        if (ActiveDeviceManage.getInstance().isDeviceActived()){
            FlowCalculate.UsedFlowInfo  usedFlowInfo = flowCalculate.calculate();

            refreshFlowData(usedFlowInfo);
            decideUploadFlow(usedFlowInfo);
        }
    }



    private void refreshFlowData(FlowCalculate.UsedFlowInfo usedFlowInfo) {
        float primaryRemainingFlow = Float.parseFloat(SharedPreferencesUtil.getStringValue(CommonLib.getInstance().getContext(), FlowConstants.KEY_REMAINING_FLOW, FlowConstants.DEFAULT_FLOW_VALUE));//无值的时候先给1024M
        log.debug("refreshFlowData剩余总流量：{}，mDeltaRx + mDeltaTx = {}", primaryRemainingFlow, usedFlowInfo.getmUsedTotalFlowBetweenCalculate());
//        float timelyRemainingFlow = primaryRemainingFlow - mMobileTotalRx - mMobileTotalTx;

        float timelyRemainingFlow = primaryRemainingFlow - usedFlowInfo.getmUsedTotalFlowBetweenCalculate();//更新剩余流量应该是减去时间段内消耗的流量
//        log.debug("timelyRemainingFlow剩余总流量：{}", timelyRemainingFlow);
        SharedPreferencesUtil.putStringValue(CommonLib.getInstance().getContext(), FlowConstants.KEY_REMAINING_FLOW, String.valueOf(timelyRemainingFlow));
    }





    private void decideUploadFlow(FlowCalculate.UsedFlowInfo usedFlowInfo){
        updatetotalUsedFlowTmp(usedFlowInfo.getmUsedTotalFlowBetweenCalculate());
//        log.debug("totalUsedFlowTmp = {}, uploadFlowValue = {}", totalUsedFlowTmp, uploadFlowValue);
        if (totalUsedFlowTmp >= uploadFlowValue){
            log.info("本次上传流量消耗值：{} KB", totalUsedFlowTmp);
            doUpdateAction(totalUsedFlowTmp);
            totalUsedFlowTmp = 0;
        }
    }

    private synchronized void updatetotalUsedFlowTmp(float usedFlow){
        totalUsedFlowTmp += usedFlow;
    }

    private void doUpdateAction(float userFlowPeriod){
        flowUploadSubscription = RequestFactory
                .getFlowRequest()
                .flowUpload(new FlowUpload(userFlowPeriod, CommonLib.getInstance().getObeId()))
                .subscribeOn(Schedulers.io())
                .subscribe(flowUploadRequestRes -> {
                    try {
                        log.debug("post 收到响应：{}", DataJsonTranslation.objectToJson(flowUploadRequestRes));
                        if (flowUploadRequestRes != null && flowUploadRequestRes.resultCode == 0 && flowUploadRequestRes.result != null){
                            proFlowUploadRes(flowUploadRequestRes.result);
                        }
                    } catch (Exception e) {
                        log.error("异常", e);
                    }
                }, throwable -> {
                    log.error("异常", throwable);
                    updatetotalUsedFlowTmp(userFlowPeriod);//异常情况
                });
    }

    private void proFlowUploadRes(FlowUploadRequestRes.FlowUploadRes flowUploadRes){
        float remianFlow = flowUploadRes.getRemainingFlow();
        log.info("FlowUploadResponse剩余流量：{}",remianFlow);
        SharedPreferencesUtil.putStringValue(CommonLib.getInstance().getContext(), FlowConstants.KEY_REMAINING_FLOW, String.valueOf(remianFlow));

        log.info("流量开关：{}", flowUploadRes.getTrafficControl());
        proSwitchFlow(flowUploadRes.getTrafficControl());

        log.info("当日流量超限异常状态：{}", flowUploadRes.getExceptionState());
        log.info("每月流量告警状态：{}", flowUploadRes.getTrafficState());
    }


    private void proSwitchFlow(int switchState){
        switch (switchState) {
            case WifiContants.RESULT_TRAFFIC_CONTROL_OPEN:
                WifiApAdmin.initWifiApState(CommonLib.getInstance().getContext());
                break;
            case WifiContants.RESULT_TRAFFIC_CONTROL_CLOSE:
                WifiApAdmin.closeWifiAp(CommonLib.getInstance().getContext());
                break;
        }
    }

}
