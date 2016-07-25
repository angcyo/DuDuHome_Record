package com.dudu.commonlib;

/**
 * Created by robi on 2016-06-04 11:43.
 */
public class StateManager {

    StateApp mStateApp;

    public StateManager() {
        mStateApp = new StateApp();
    }

    public static StateManager instance() {
        return Holder.state;
    }

    public boolean isFire() {
        return mStateApp.isFire();
    }

    public void setFire(boolean fire) {
        mStateApp.setFire(fire);
    }

    public boolean isCamera() {
        return mStateApp.isCamera();
    }

    public void setCamera(boolean camera) {
        mStateApp.setCamera(camera);
    }

    public boolean isPreview() {
        return mStateApp.isPreview();
    }

    public void setPreview(boolean preview) {
        mStateApp.setPreview(preview);
    }

    public boolean isScreenOn() {
        return mStateApp.isScreenOn();
    }

    public void setScreenOn(boolean screenOn) {
        mStateApp.setScreenOn(screenOn);
    }

    public boolean isTfSpace() {
        return mStateApp.isTfSpace();
    }

    public void setTfSpace(boolean tfSpace) {
        mStateApp.setTfSpace(tfSpace);
    }

    public boolean isVideoStream() {
        return mStateApp.isVideoStream();
    }

    public void setVideoStream(boolean videoStream) {
        mStateApp.setVideoStream(videoStream);
    }

    static class Holder {
        static final StateManager state = new StateManager();
    }

    static class StateApp {
        boolean isFire;//是否点火
        boolean isCamera;//是否打开了摄像头
        boolean isPreview;//是否开始了预览
        boolean isScreenOn;//是否屏幕点亮了
        boolean isTfSpace;//是否T卡空间充足
        boolean isVideoStream;//是否正在实时监控

        public boolean isFire() {
            return isFire;
        }

        public void setFire(boolean fire) {
            isFire = fire;
        }

        public boolean isCamera() {
            return isCamera;
        }

        public void setCamera(boolean camera) {
            isCamera = camera;
        }

        public boolean isPreview() {
            return isPreview;
        }

        public void setPreview(boolean preview) {
            isPreview = preview;
        }

        public boolean isScreenOn() {
            return isScreenOn;
        }

        public void setScreenOn(boolean screenOn) {
            isScreenOn = screenOn;
        }

        public boolean isTfSpace() {
            return isTfSpace;
        }

        public void setTfSpace(boolean tfSpace) {
            isTfSpace = tfSpace;
        }

        public boolean isVideoStream() {
            return isVideoStream;
        }

        public void setVideoStream(boolean videoStream) {
            isVideoStream = videoStream;
        }
    }
}
