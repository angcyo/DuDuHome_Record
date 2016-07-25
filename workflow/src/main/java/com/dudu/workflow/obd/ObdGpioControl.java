package com.dudu.workflow.obd;

import com.dudu.commonlib.utils.shell.ShellExe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dengjun on 2016/5/17.
 * Description :
 */
public class ObdGpioControl {
    private static final String WAKE_OBD = "echo 1 > /sys/devices/soc.0/obd_gpio.68/obd_wakeup";
    private static final String SLEEP_OBD = "echo 0 > /sys/devices/soc.0/obd_gpio.68/obd_wakeup";
    private static final String POWERON_OBD = "echo 1 > /sys/devices/soc.0/obd_gpio.68/obd_power_enable";
    private static final String POWEROFF_OBD = "echo 0 > /sys/devices/soc.0/obd_gpio.68/obd_power_enable";

    private static Logger log = LoggerFactory.getLogger("car.obd");

    public static void wakeObd() {
        log.info("唤醒obd");
        ShellExe.execShellCmd(WAKE_OBD);
    }

    public static void sleepObd() {
        log.info("休眠obd");
        ShellExe.execShellCmd(SLEEP_OBD);
    }

    public static void powerOnObd() {
        log.info("obd上电");
        ShellExe.execShellCmd(POWERON_OBD);
    }

    public static void powerOffObd() {
        log.info("obd下电");
        ShellExe.execShellCmd(POWEROFF_OBD);
    }
}
