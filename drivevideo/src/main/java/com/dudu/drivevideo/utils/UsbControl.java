package com.dudu.drivevideo.utils;

import com.dudu.commonlib.utils.shell.ShellExe;

import org.slf4j.LoggerFactory;

/**
 * Created by dengjun on 2016/2/20.
 * Description :
 */
public class UsbControl {
    public static String TO_HOST_CMD = "echo 1 > /sys/bus/platform/devices/obd_gpio.68/usb_id_enable";
    public static String TO_CLIENT_CMD = "echo 0 > /sys/bus/platform/devices/obd_gpio.68/usb_id_enable";

    public static boolean usbHostState = false;

    public static boolean isUsbHostState() {
        return usbHostState;
    }

    public static void setToHost(){
     /*   LoggerFactory.getLogger("video.reardrivevideo").debug("USB设置成host-------------");
        String setReturnValue = ShellExe.execShellCmd(TO_HOST_CMD);
        LoggerFactory.getLogger("video.reardrivevideo").debug("USB设置成host-----结果：{}", setReturnValue);
        usbHostState = true;
        return  setReturnValue;*/

        new Thread(new Runnable() {
            @Override
            public void run() {
                LoggerFactory.getLogger("video.reardrivevideo").debug("USB设置成host-------------");
                String setReturnValue = ShellExe.execShellCmd(TO_HOST_CMD);
                LoggerFactory.getLogger("video.reardrivevideo").debug("USB设置成host-----结果：{}", setReturnValue);
            }
        }).start();
    }

    public static void setToClient(){
/*        LoggerFactory.getLogger("video.reardrivevideo").debug("UBS设置成client-------------");
        String setReturnValue = ShellExe.execShellCmd(TO_CLIENT_CMD);
        LoggerFactory.getLogger("video.reardrivevideo").debug("UBS设置成client-----结果：{}", setReturnValue);
        usbHostState = false;
        return  setReturnValue;*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                LoggerFactory.getLogger("video.reardrivevideo").debug("USB设置成client-------------");
                String setReturnValue = ShellExe.execShellCmd(TO_CLIENT_CMD);
                LoggerFactory.getLogger("video.reardrivevideo").debug("USB设置成client-----结果：{}", setReturnValue);
                usbHostState = false;
            }
        }).start();
    }
}
