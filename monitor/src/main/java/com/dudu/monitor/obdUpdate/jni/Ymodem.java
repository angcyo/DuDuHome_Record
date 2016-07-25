package com.dudu.monitor.obdUpdate.jni;

/**
 * Created by dengjun on 2016/3/11.
 * Description :
 */
public class Ymodem {
    public native  int sendFile(String filePath);
    
     
    static {
        System.loadLibrary("ymodem");
    }
}
