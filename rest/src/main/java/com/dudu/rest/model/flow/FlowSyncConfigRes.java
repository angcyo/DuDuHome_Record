package com.dudu.rest.model.flow;

import com.dudu.rest.model.common.RequestResponse;

/**
 * Created by dengjun on 2016/4/22.
 * Description :
 */
public class FlowSyncConfigRes extends RequestResponse{
    public FlowSynConfiguration result;

    public class FlowSynConfiguration {

        public String uploadLimit;


        public String trafficControl;


        public String downloadLimit;


        public String lifeType;


        public String upLimitMaxValue;


        public String downLimitMaxValue;


        public String dailyMaxValue;


        public String monthMaxValue;


        public String highArlamValue;


        public String middleArlamValue;

        public String lowArlamValue;


        public String freeArriveValue;


        public String freeAddValue;


        public String freeAddTimes;


        public String portalVersion;


        public String portalAddress;


        public String closeArlamValue;

        public String flowFrequency;


        public String gpsFrequency;


        public String portalCountFrequency;


        public String uploadFlowValue;


        public String getUploadLimit() {
            return uploadLimit;
        }

        public void setUploadLimit(String uploadLimit) {
            this.uploadLimit = uploadLimit;
        }

        public String getTrafficControl() {
            return trafficControl;
        }

        public void setTrafficControl(String trafficControl) {
            this.trafficControl = trafficControl;
        }

        public String getDownloadLimit() {
            return downloadLimit;
        }

        public void setDownloadLimit(String downloadLimit) {
            this.downloadLimit = downloadLimit;
        }

        public String getLifeType() {
            return lifeType;
        }

        public void setLifeType(String lifeType) {
            this.lifeType = lifeType;
        }

        public String getUpLimitMaxValue() {
            return upLimitMaxValue;
        }

        public void setUpLimitMaxValue(String upLimitMaxValue) {
            this.upLimitMaxValue = upLimitMaxValue;
        }

        public String getDownLimitMaxValue() {
            return downLimitMaxValue;
        }

        public void setDownLimitMaxValue(String downLimitMaxValue) {
            this.downLimitMaxValue = downLimitMaxValue;
        }

        public String getDailyMaxValue() {
            return dailyMaxValue;
        }

        public void setDailyMaxValue(String dailyMaxValue) {
            this.dailyMaxValue = dailyMaxValue;
        }

        public String getMonthMaxValue() {
            return monthMaxValue;
        }

        public void setMonthMaxValue(String monthMaxValue) {
            this.monthMaxValue = monthMaxValue;
        }

        public String getHighArlamValue() {
            return highArlamValue;
        }

        public void setHighArlamValue(String highArlamValue) {
            this.highArlamValue = highArlamValue;
        }

        public String getMiddleArlamValue() {
            return middleArlamValue;
        }

        public void setMiddleArlamValue(String middleArlamValue) {
            this.middleArlamValue = middleArlamValue;
        }

        public String getLowArlamValue() {
            return lowArlamValue;
        }

        public void setLowArlamValue(String lowArlamValue) {
            this.lowArlamValue = lowArlamValue;
        }

        public String getFreeArriveValue() {
            return freeArriveValue;
        }

        public void setFreeArriveValue(String freeArriveValue) {
            this.freeArriveValue = freeArriveValue;
        }

        public String getFreeAddValue() {
            return freeAddValue;
        }

        public void setFreeAddValue(String freeAddValue) {
            this.freeAddValue = freeAddValue;
        }

        public String getFreeAddTimes() {
            return freeAddTimes;
        }

        public void setFreeAddTimes(String freeAddTimes) {
            this.freeAddTimes = freeAddTimes;
        }

        public String getPortalVersion() {
            return portalVersion;
        }

        public void setPortalVersion(String portalVersion) {
            this.portalVersion = portalVersion;
        }

        public String getPortalAddress() {
            return portalAddress;
        }

        public void setPortalAddress(String portalAddress) {
            this.portalAddress = portalAddress;
        }

        public String getCloseArlamValue() {
            return closeArlamValue;
        }

        public void setCloseArlamValue(String closeArlamValue) {
            this.closeArlamValue = closeArlamValue;
        }

        public String getFlowFrequency() {
            return flowFrequency;
        }

        public void setFlowFrequency(String flowFrequency) {
            this.flowFrequency = flowFrequency;
        }

        public String getGpsFrequency() {
            return gpsFrequency;
        }

        public void setGpsFrequency(String gpsFrequency) {
            this.gpsFrequency = gpsFrequency;
        }

        public String getPortalCountFrequency() {
            return portalCountFrequency;
        }

        public void setPortalCountFrequency(String portalCountFrequency) {
            this.portalCountFrequency = portalCountFrequency;
        }

        public String getUploadFlowValue() {
            return uploadFlowValue;
        }

        public void setUploadFlowValue(String uploadFlowValue) {
            this.uploadFlowValue = uploadFlowValue;
        }
    }

}
