package com.dudu.monitor.flow.claculate;

import android.net.TrafficStats;

/**
 * Created by dengjun on 2015/12/9.
 * Description : 流量使用计算类
 */
public class FlowCalculate {
    //设备启动以来接收消耗的流量 kb
    private float mUsedRxFlowSinceBoot;
    //设备启动以来发送消耗的流量 kb
    private float mUsedTxFlowSinceBoot;

    public FlowCalculate() {
        mUsedRxFlowSinceBoot = TrafficStats.getMobileRxBytes() / 1024;
        mUsedTxFlowSinceBoot = TrafficStats.getMobileTxBytes() / 1024;
    }

    public UsedFlowInfo calculate(){
        UsedFlowInfo usedFlowInfo = new UsedFlowInfo();

        float usedRxFlowSinceBootTmp = TrafficStats.getMobileRxBytes() / 1024;
        float usedTxFlowSinceBootTmp = TrafficStats.getMobileTxBytes() / 1024;
        if (usedRxFlowSinceBootTmp == -1 && usedTxFlowSinceBootTmp == -1)
            return usedFlowInfo;

        float usedRxFlow = usedRxFlowSinceBootTmp - mUsedRxFlowSinceBoot;
        float usedTxFlow = usedTxFlowSinceBootTmp - mUsedTxFlowSinceBoot;
        mUsedRxFlowSinceBoot = usedRxFlowSinceBootTmp;
        mUsedTxFlowSinceBoot = usedTxFlowSinceBootTmp;

        usedRxFlow = (Math.round(usedRxFlow * 100.0)) / 100;
        usedTxFlow = (Math.round(usedTxFlow * 100.0)) / 100;

        usedFlowInfo.setmUsedRxFlowBetweenCalculate(usedRxFlow);
        usedFlowInfo.setmUsedTxFlowBetweenCalculate(usedTxFlow);
        usedFlowInfo.setmUsedTotalFlowBetweenCalculate(usedRxFlow + usedTxFlow);

        return usedFlowInfo;
    }

    //使用流量信息类
    public class UsedFlowInfo{
        //两次计算之间消耗的接收流量 kb
        private float mUsedRxFlowBetweenCalculate = 0;
        //两次计算之间消耗的发送流量 kb
        private float mUsedTxFlowBetweenCalculate = 0;
        //两次计算之间消耗的总流量 kb
        private float mUsedTotalFlowBetweenCalculate = 0;

        public float getmUsedRxFlowBetweenCalculate() {
            return mUsedRxFlowBetweenCalculate;
        }

        public void setmUsedRxFlowBetweenCalculate(float mUsedRxFlowBetweenCalculate) {
            this.mUsedRxFlowBetweenCalculate = mUsedRxFlowBetweenCalculate;
        }

        public float getmUsedTxFlowBetweenCalculate() {
            return mUsedTxFlowBetweenCalculate;
        }

        public void setmUsedTxFlowBetweenCalculate(float mUsedTxFlowBetweenCalculate) {
            this.mUsedTxFlowBetweenCalculate = mUsedTxFlowBetweenCalculate;
        }

        public float getmUsedTotalFlowBetweenCalculate() {
            return mUsedTotalFlowBetweenCalculate;
        }

        public void setmUsedTotalFlowBetweenCalculate(float mUsedTotalFlowBetweenCalculate) {
            this.mUsedTotalFlowBetweenCalculate = mUsedTotalFlowBetweenCalculate;
        }
    }
}
