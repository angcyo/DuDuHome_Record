package com.dudu.drivevideo.frontcamera;

import android.text.TextUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;

/**
 * Created by Administrator on 2016/5/11.
 */
public class TcpSocketThread extends Thread {

    private static final int DATA_LEN = 1024;
    Exception mException;
    private int port;
    private String ip;
    private Socket socket;
    private int TIME_OUT = 10 * 1000;
    private boolean isExit = false;
    private IOnDataListener dataListener;
    private Vector<byte[]> mBytes;
    private Logger log = LoggerFactory.getLogger("video.tcp");


    public TcpSocketThread(String threadName, int port, String ip) {
        super(threadName);
        this.port = port;
        this.ip = ip;
        mBytes = new Vector<>();
    }

    public void setDataListener(IOnDataListener dataListener) {
        this.dataListener = dataListener;
    }

    private void createSocket() throws SocketException {
        socket = new Socket();
        socket.setSoTimeout(TIME_OUT);
        socket.setKeepAlive(true);
    }

    @Override
    public void run() {
        while (!isExit) {
            if (connect()) {
//                while (socket.isConnected()) {

//                try {
//                    writeData();
//                } catch (Exception e) {
//                    mException = e;
//                }

                try {
                    readData();
                } catch (Exception e) {
                    mException = e;
                }
            }
            close();
        }
        log.debug("Socket 退出");
    }

    public void addWriteData(byte[] bytes) {
        mBytes.add(bytes);
    }

    public void addWriteData(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            mBytes.add(msg.getBytes());
        }

        try {
            writeData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void writeData() throws IOException {
        BufferedOutputStream outputStream = new BufferedOutputStream(socket.getOutputStream());
        while (!mBytes.isEmpty()) {
            byte[] bytes = mBytes.remove(0);
            outputStream.write(bytes);
        }
        outputStream.flush();
//        outputStream.close();
    }

    public synchronized void exit() {
        isExit = true;
    }

    private boolean connect() {
        try {
            if (socket == null) {
                createSocket();
            }
            socket.connect(new InetSocketAddress(ip, port));
            log.info("连接成功Socket--> {}:{}", ip, port);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void close() {
        if (socket != null) {
            try {
//                log.info("关闭Socket");
                socket.shutdownInput();
                socket.close();
            } catch (IOException e) {
            }
            socket = null;
//            System.gc();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    private void readData() throws IOException {
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        byte[] data = new byte[DATA_LEN];
        int readLen;
//        e("等待读取数据...");
        while (!isExit) {
            readLen = dataInputStream.read(data);
            String strData = new String(data, 0, readLen);
            onData(strData);
        }
//        e("读取数据...结束");
    }

    /**
     * 读取收到的数据
     */
    private void onData(String strData) {
//        e("数据:" + strData);
        if (dataListener != null) {
            dataListener.onData(strData);
        }
    }
}
