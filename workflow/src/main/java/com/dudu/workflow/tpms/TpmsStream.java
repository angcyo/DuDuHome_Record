package com.dudu.workflow.tpms;

import com.dudu.android.libserial.SerialManager;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import android_serialport_api.SerialPort;
import rx.Observable;
import rx.Subscriber;
import rx.ext.FixedLengthReader;
import rx.observables.ConnectableObservable;

public class TpmsStream {
    private static TpmsStream ourInstance = new TpmsStream();
    private static Logger logger = LoggerFactory.getLogger("monitor.TPMSStream");
    private OutputStream outputStream = null;
    private Observable<ByteBuffer> rawData = null;

    public static TpmsStream getInstance() {
        return ourInstance;
    }

    private TpmsStream() {
    }

    public Observable<ByteBuffer> rawData() {
        return rawData;
    }

    public void init() {
        if (rawData == null) {
            SerialPort serialPort = SerialManager.getInstance().getSerialPortTPMS();
            InputStream inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            rawData = from(new FixedLengthReader(inputStream)).autoConnect();
        }
    }

    public void write(byte[] cmd) {
        byte[] tmp = new byte[cmd.length + 1];
        System.arraycopy(cmd, 0, tmp, 0, cmd.length);

        int sum = 0;
        for (int i = 0; i < cmd.length; i++) {
            sum += cmd[i];
        }
        tmp[cmd.length] = (byte) (0xff & sum);
        logger.debug("exec:{}", String.copyValueOf(Hex.encodeHex(tmp)));
        if (outputStream != null) {
            try {
                outputStream.write(tmp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ConnectableObservable<ByteBuffer> from(final FixedLengthReader reader) {
        return Observable.create((Subscriber<? super ByteBuffer> subscriber) -> {
            try {
                ByteBuffer line;

                if (subscriber.isUnsubscribed()) {
                    return;
                }

                while (!subscriber.isUnsubscribed() && (line = reader.readFrame()) != null) {
                    logger.debug("TPMS:{}", String.copyValueOf(Hex.encodeHex(line.array())));
                    subscriber.onNext(line);
                }
            } catch (IOException e) {
                subscriber.onError(e);
            }

            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }).replay(1, 1, TimeUnit.SECONDS);
    }


    public void tpmsStreamClose() {
        outputStream = null;
        rawData = null;
        SerialManager.getInstance().closeSerialPortTPMS();
        logger.debug("关闭TPMS流");
    }
}
