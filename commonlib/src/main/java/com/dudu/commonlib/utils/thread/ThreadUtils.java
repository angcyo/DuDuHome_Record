package com.dudu.commonlib.utils.thread;

/**
 * Created by dengjun on 2016/5/12.
 * Description :
 */
public class ThreadUtils {
    public static void threadSleep(int millisecond) {
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
