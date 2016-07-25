package com.dudu.workflow.tpms;

import java.io.InputStream;
import java.io.Reader;

import rx.ext.FixedLengthReader;

public class TPMSReader extends FixedLengthReader {
    private static final byte FRAME_START = (byte) 0xAA;

    public TPMSReader(InputStream in) {
        super(in);
    }

}
