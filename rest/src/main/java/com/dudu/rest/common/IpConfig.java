package com.dudu.rest.common;

/**
 * Created by Administrator on 2016/3/29.
 */
public class IpConfig {

    private static boolean isTest = false;

    public static boolean isTest() {
        return isTest;
    }

    public void setTest(boolean test) {
        isTest = test;
    }

    public static String getServerAddress() {
        return isTest ? IpConfigConstant.TEST_SERVER_ADDRESS : IpConfigConstant.SERVER_ADDRESS;
    }

    public static String getSocketAddress() {
        return isTest ? IpConfigConstant.TEST_SOCKET_ADDRESS : IpConfigConstant.SOCKET_ADDRESS;
    }

    public static String getSocketPort() {
        return isTest ? IpConfigConstant.TEST_SOCKETPORT : IpConfigConstant.SOCKET_PORT;
    }

}
