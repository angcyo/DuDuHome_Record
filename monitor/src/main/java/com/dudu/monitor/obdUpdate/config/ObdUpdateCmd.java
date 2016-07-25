package com.dudu.monitor.obdUpdate.config;

import com.dudu.commonlib.utils.shell.ShellExe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dengjun on 2016/3/8.
 * Description :
 */
public class ObdUpdateCmd {
    /* 设置串口波特率*/
    public static final String SET_SERIAL_PORT = "setserialport";
    /*串口设备 */
    public static final String SERIAL_DEV = "/dev/ttyHS5";
    /*复位obd芯片 */
    public static final String RESET_OBD_CHIP_0 = "echo 0 > /sys/bus/platform/devices/obd_gpio.68/obd_reset_enable";
    public static final String RESET_OBD_CHIP_1 = "echo 1 > /sys/bus/platform/devices/obd_gpio.68/obd_reset_enable";
    /* download模式命令*/
    public static final String DOWNLOAD_MODE = "ATDOWNLOAD\r\n";
    /* 使能download模式命令*/
    public static final String ENABLE_DOWNLOAD_MODE_CMD = "echo -e "+ DOWNLOAD_MODE+" > "+SERIAL_DEV;
   /* 命令将obd固件发送到obd芯片*/
    public static final String SEND_OBD_ROM_CMD = "aymodem s ";


    public static Logger log = LoggerFactory.getLogger("car.obdUpdate");

    public static void setSerialPort(){
        log.info("执行命令：{}", SET_SERIAL_PORT);
        ShellExe.execShellCmd(SET_SERIAL_PORT);
    }

    public static void resetObdChip(){
        setObdResetEnable0();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setObdResetEnable1();
    }

    public static void setObdResetEnable0(){
        log.info("执行命令：{}", RESET_OBD_CHIP_0);
        ShellExe.execShellCmd(RESET_OBD_CHIP_0);
    }

    public static void setObdResetEnable1(){
        log.info("执行命令：{}", RESET_OBD_CHIP_1);
        ShellExe.execShellCmd(RESET_OBD_CHIP_1);
    }

    public static void enableDownloadMode(){
        log.info("执行命令：{}", ENABLE_DOWNLOAD_MODE_CMD);
        ShellExe.execShellCmd(ENABLE_DOWNLOAD_MODE_CMD);
    }

    public static void sendObdRom(String obdRomPath){
        log.info("执行命令：{}", SEND_OBD_ROM_CMD + obdRomPath);
        ShellExe.execShellCmd(SEND_OBD_ROM_CMD + obdRomPath);
    }
}
