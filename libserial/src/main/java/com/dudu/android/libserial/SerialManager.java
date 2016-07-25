package com.dudu.android.libserial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

public class SerialManager {
    private static Logger log = LoggerFactory.getLogger("car.obd.serial");

    private static final String SP_OBD = "/dev/ttyHS5";
    private static final int BAUDRATE_OBD = 115200;

    private static final String SP_TPMS = "/dev/ttyHSL0";
    private static final int BAUDRATE_TPMS = 9600;

    private static SerialManager ourInstance = new SerialManager();

    public static SerialManager getInstance() {
        return ourInstance;
    }

    private SerialManager() {
    }

    private SerialPort mSerialPortOBD = null;
    private SerialPort mSerialPortTPMS = null;

    private SerialPort getSerialPort(String path, int baudrate) throws SecurityException, IOException, InvalidParameterException {
        /* Read serial port parameters */
        log.info("config:path=" + path + " bandrate=" + baudrate);
        /* Check parameters */
        if ((path.length() == 0) || (baudrate == -1)) {
            throw new InvalidParameterException();
        }

        /* Open the serial port */
        return new SerialPort(new File(path), baudrate, 0);
    }

    public SerialPort getSerialPortOBD() {
        if (mSerialPortOBD == null) {
            try {
                mSerialPortOBD = getSerialPort(SP_OBD, BAUDRATE_OBD);
            } catch (IOException e) {
                log.error("异常", e);
            }
        }
        return mSerialPortOBD;
    }

    public SerialPort getSerialPortTPMS() {
        if (mSerialPortTPMS == null) {
            try {
                mSerialPortTPMS = getSerialPort(SP_TPMS, BAUDRATE_TPMS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mSerialPortTPMS;
    }

    public void closeSerialPortOBD() {
        if (mSerialPortOBD != null) {
            mSerialPortOBD.close();
            mSerialPortOBD = null;
        }
    }

    public void closeSerialPortTPMS() {
        if (mSerialPortTPMS != null) {
            mSerialPortTPMS.close();
            mSerialPortTPMS = null;
        }
    }
}
