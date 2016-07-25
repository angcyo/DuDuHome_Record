package com.dudu.aios.uvc;

public class Native {

    public static native String stringFromJNI();
    public static native void uvcSetFileName(byte[] filename, int size);
    public static native int uvcAP(String dev);
    public static native void uvcStop();
    public static native void uvcHideOSD(boolean hide);
    public static native void uvcPlugRelease();
    public static native void uvcStoreVideo();

    static {
        System.loadLibrary("uvc");
    }
}
