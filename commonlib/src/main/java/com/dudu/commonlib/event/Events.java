package com.dudu.commonlib.event;

/**
 * Created by Administrator on 2016/4/24.
 */
public class Events {


    public static final int TEST_SPEED_START = 1;

    public static final int TEST_SPEED_STOP = 2;

    public static final int TEST_SPEED_ZERO = 3;

    public static final int OPEN_VEHICLE_INSPECTION = 1;

    public static final int OPEN_SAFETY_CENTER = 2;

    public static int REBOOT = 1;

    public static class DeviceEvent {
        private int event;

        public DeviceEvent(int event) {
            this.event = event;
        }

        public int getEvent() {
            return event;
        }

        public void setEvent(int event) {
            this.event = event;
        }
    }

    public static class TestSpeedEvent {

        private int isTestComplete;

        private String mSpeedTotalTime;

        private String speed;

        public TestSpeedEvent(int event) {
            this.isTestComplete = event;
        }

        public String getSpeed() {
            return speed;
        }

        public void setSpeed(String speed) {
            this.speed = speed;
        }

        public int getEvent() {
            return isTestComplete;
        }

        public void setEvent(int isTestComplete) {
            this.isTestComplete = isTestComplete;
        }


        public String getSpeedTotalTime() {
            return mSpeedTotalTime;
        }

        public void setSpeedTotalTime(String mSpeedTotalTime) {
            this.mSpeedTotalTime = mSpeedTotalTime;
        }
    }

    public static class VoltageTypeChangeEvent {

        private boolean isAccVoltage;

        public VoltageTypeChangeEvent(boolean isAccVoltage) {
            this.isAccVoltage = isAccVoltage;
        }

        public boolean isAccVoltage() {
            return isAccVoltage;
        }
    }

    public static class RobberyEvent {
        private int revolutions, numberOfOperations, completeTime;

        public RobberyEvent(int revolutions, int numberOfOperations, int completeTime) {
            this.revolutions = revolutions;
            this.numberOfOperations = numberOfOperations;
            this.completeTime = completeTime;
        }

        public int getRevolutions() {
            return revolutions;
        }

        public void setRevolutions(int revolutions) {
            this.revolutions = revolutions;
        }

        public int getNumberOfOperations() {
            return numberOfOperations;
        }

        public void setNumberOfOperations(int numberOfOperations) {
            this.numberOfOperations = numberOfOperations;
        }

        public int getCompleteTime() {
            return completeTime;
        }

        public void setCompleteTime(int completeTime) {
            this.completeTime = completeTime;
        }
    }

    public static class GuardSwitchState {
        private boolean isOpen;

        public GuardSwitchState(boolean isOpen) {
            this.isOpen = isOpen;
        }

        public boolean isOpen() {
            return isOpen;
        }
    }

    public static class OpenSafeCenterEvent {

        private int openType;

        public OpenSafeCenterEvent(int openType) {
            this.openType = openType;
        }

        public int getOpenType() {
            return openType;
        }
    }
}
