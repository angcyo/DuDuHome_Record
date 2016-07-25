package com.dudu.workflow.tpms;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import rx.Observable;

public class TPMSFlow {
    private static Logger logger = LoggerFactory.getLogger("TPMS.checkFlow");

    public static Observable<TPMSInfo> TPMSParse(final byte mainFn) {
        return TpmsStream.getInstance().rawData()
                .filter(byteBuffer -> byteBuffer.get(0) == mainFn)
                .filter(byteBuffer -> {
                    int sz = byteBuffer.array().length;
                    logger.debug("byteBuffer size:{}", sz);
                    return sz == 10 | sz == 11;
                })
                .map(byteBuffer -> ByteBuffer.wrap(byteBuffer.array()))
                .map(byteBuffer -> {
                    logger.debug(String.copyValueOf(Hex.encodeHex(byteBuffer.array())));
                    byte fn = byteBuffer.get();
                    if (fn != mainFn) throw new RuntimeException("胎压数据传输");
                    TPMSInfo warnInfo = new TPMSInfo();
                    warnInfo.postion = byteBuffer.get();
                    if (warnInfo.postion == 0) {
                        warnInfo.postion = byteBuffer.get();
                    }
                    byteBuffer.get(); //pass one byte
                    warnInfo.sensorID = byteBuffer.getShort();
                    warnInfo.pressure = (float) ((0x03ff & byteBuffer.getShort()) * 0.025);
                    warnInfo.temperature = (0x0ff & byteBuffer.get()) - 50;

                    byte state = byteBuffer.get();
                    warnInfo.battery = (0x80 & state) != 0;
                    warnInfo.noData = (0x40 & state) != 0;
                    warnInfo.barometerHigh = (0x10 & state) != 0;
                    warnInfo.barometerLow = (0x08 & state) != 0;
                    warnInfo.temperatureHigh = (0x04 & state) != 0;
                    warnInfo.gasLeaks = 0x03 & state;
                    return warnInfo;
                })
                .doOnError(throwable -> logger.error("TPMSParse", throwable));
    }

    public static Observable<TPMSInfo> TPMSWarnInfoStream() {
        final byte mainFn = 0x63;
        return TPMSParse(mainFn);
    }

    public static Observable<TPMSInfo.POSITION> TPMSPairStream() {
        final byte mainFn = 0x66;
        return TPMSParse(mainFn)
                .filter(tpmsWarnInfo -> tpmsWarnInfo.gasLeaks == 3)
                .map(tpmsWarnInfo -> TPMSInfo.POSITION.valueOf(tpmsWarnInfo.postion))
                .filter(position -> position != TPMSInfo.POSITION.UNKNOW);
    }

    public static void TPMSPairStart(TPMSInfo.POSITION position) {
        byte[] cmd = {(byte) 0xAA, 0x41, (byte) 0xA1, 0x07, 0x66, 0x00};
        cmd[5] = (byte) position.value();
        TpmsStream.getInstance().write(cmd);
    }
}
