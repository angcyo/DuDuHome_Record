package rx.ext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class FixedLengthReader {

    private static final String TAG = "FixedLengthReader";
    private static Logger logger = LoggerFactory.getLogger("TPMS.FixedLengthReader");

    public static final byte[] STX = {(byte) 0xAA, (byte) 0xA1, 0x41};

    private Object lock;

    private InputStream in;

    private byte[] buf;

    private int frameSize;

    private int pos;

    private int end;

    private int mark = -1;

    private int markLimit = -1;

    private boolean isFrameStart;

    private boolean markedLastWasCR;

    public FixedLengthReader(InputStream in) {
        this(in, 512);
    }

    public FixedLengthReader(InputStream in, int size) {
        this.lock = this;

        if (size <= 0) {
            throw new IllegalArgumentException("size <= 0");
        }
        this.in = in;
        this.frameSize = 0;
        this.isFrameStart = false;
        buf = new byte[size];
    }

    public void close() throws IOException {
        synchronized (lock) {
            if (!isClosed()) {
                in.close();
                buf = null;
            }
        }
    }

    private int fillBuf() throws IOException {
        // assert(pos == end);
        logger.debug("fillBuf: pos=" + pos + " end=" + end);

        if (mark == -1 || (pos - mark >= markLimit)) {
            /* mark isn't set or has exceeded its limit. use the whole buffer */
            logger.debug("mark isn't set or has exceeded its limit. use the whole buffer");
            int result = in.read(buf, 0, buf.length);
            logger.debug("read: " + result);
            if (result > 0) {
                mark = -1;
                pos = 0;
                end = result;
            }
            return result;
        }

        if (mark == 0 && markLimit > buf.length) {
            /* the only way to make room when mark=0 is by growing the buffer */
            logger.debug("the only way to make room when mark=0 is by growing the buffer");
            int newLength = buf.length * 2;
            if (newLength > markLimit) {
                newLength = markLimit;
            }
            byte[] newbuf = new byte[newLength];
            System.arraycopy(buf, 0, newbuf, 0, buf.length);
            buf = newbuf;
        } else if (mark > 0) {
            /* make room by shifting the buffered data to left mark positions */
            logger.debug("make room by shifting the buffered data to left mark positions");
            System.arraycopy(buf, mark, buf, 0, buf.length - mark);
            pos -= mark;
            end -= mark;
            mark = 0;
        }

        /* Set the new position and mark position */
        logger.debug("Set the new position and mark position");
        int count = in.read(buf, pos, buf.length - pos);
        logger.debug("read: " + count);
        if (count != -1) {
            end += count;
        }
        return count;
    }

    private boolean isClosed() {
        return buf == null;
    }

    private void checkNotClosed() throws IOException {
        if (isClosed()) {
            throw new IOException("BufferedReader is closed");
        }
    }

    public ByteBuffer readFrame() throws IOException {
        synchronized (lock) {
            checkNotClosed();

            ByteBuffer result = null;

            logger.debug("readFrame begin: pos=" + pos + " end=" + end);
            // Do we have a whole line in the buffer?
            checkSTX();

            if (isFrameStart) {
                result = ByteBuffer.allocate(frameSize);
                if (fillAFrame(result)) {
                    return result;
                }
            }

            // Accumulate buffers in a ByteBuffer until we've read a whole line.
            while (true) {
                pos = end;
                logger.debug("try read again");
                if (fillBuf() == -1) {
                    // If there's no more input, return what we've read so far, if anything.
                    logger.debug("there's no more input");
                    break;
                }

                if (isFrameStart) {
                    logger.debug("fill last frame");
                    if (fillAFrame(result)) {
                        return result;
                    }
                } else {
                    // Do we have a whole line in the buffer now?
                    logger.debug("Do we have a whole line in the buffer now?: pos=" + pos + " end=" + end);
                    checkSTX();

                    if (isFrameStart) {
                        result = ByteBuffer.allocate(frameSize);
                        if (fillAFrame(result)) {
                            return result;
                        }
                    }
                }
            }
            return result;
        }
    }

    private boolean fillAFrame(ByteBuffer result) {
        int putsize = pos + frameSize - 1 < end ? frameSize : end - pos;
        result.put(buf, pos, putsize);
        pos += putsize;
        frameSize -= putsize;
        if (0 == frameSize) {
            isFrameStart = false;
            return true;
        }

        return false;
    }

    private void checkSTX() {
        for (int i = pos; i < end; i++) {
            byte ch = buf[i];
            if (ch == STX[0] && i + 3 < end) {
                //如果匹配到开头
                if (buf[i + 1] == STX[1] && buf[i + 2] == STX[2]) {
                    frameSize = buf[i + 3] - 4;
                    isFrameStart = true;
                    pos = i + 4;
                    break;
                }
            }
        }
    }
}
