package com.dudu.drivevideo.frontcamera;

import android.text.TextUtils;

import com.dudu.commonlib.CommonLib;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by robi on 2016-05-31 16:30.
 */
public class TcpSocketService {


    private TcpSocketThread mTcpSocketThread;
    private int tcpPort = 54320;//test
    private String tcpIp = "182.254.227.45";

    private Logger log = LoggerFactory.getLogger("video.tcpsocketservice");
    private IStreamListener mIStreamListener;

    public TcpSocketService(IStreamListener listener) {
        mIStreamListener = listener;
        init();
    }

    public TcpSocketService() {
        init();
    }

    public void setIStreamListener(IStreamListener IStreamListener) {
        mIStreamListener = IStreamListener;
    }

    private void init() {
        mTcpSocketThread = new TcpSocketThread("tcp", tcpPort, tcpIp);//先用9876端口,获取用于监控的端口
        mTcpSocketThread.start();
        mTcpSocketThread.setDataListener(data -> {
            log.debug("收到数据:{}", data);
            TcpCommand command = new Gson().fromJson(data, TcpCommand.class);

            if (command.getStatus() == 1) {
                command.setStatus(2);//修改状态
                command.setDeviceID(CommonLib.getInstance().getObeId());//返回设备id
//                command.setCommand("REGISTER");
                String json = new Gson().toJson(command);
                log.info("发送数据:{}", json);
                mTcpSocketThread.addWriteData(json);
            } else if (command.getStatus() == 3) {
//                command.setIP("状态3");
//                String json = new Gson().toJson(command);
//                log.info("发送数据:{}", json);
//                mTcpSocketThread.addWriteData(json);

                mTcpSocketThread.exit();
                mTcpSocketThread = new TcpSocketThread("tcp_stream", command.getPort(), tcpIp);
                mTcpSocketThread.start();
                mTcpSocketThread.setDataListener(data2 -> {
                    log.debug("收到数据:{}", data2);
                    onTcpData(new Gson().fromJson(data2, TcpCommand.class));
                });
            }

//            if (command.getPort() != tcpPort) {
//                command.setStatus(2);//修改状态
//                command.setDeviceID(CommonLib.getInstance().getObeId());//返回设备id
////                command.setCommand("REGISTER");
//                String json = new Gson().toJson(command);
//                log.info("发送数据:{}", json);
//                mTcpSocketThread.addWriteData(json);
//
//                mTcpSocketThread.exit();
//                mTcpSocketThread = new TcpSocketThread("tcp_stream", command.getPort(), tcpIp);
//                mTcpSocketThread.start();
//                mTcpSocketThread.setDataListener(data2 -> {
//                    log.debug("收到数据:{}", data);
//                    onTcpData(new Gson().fromJson(data, TcpCommand.class));
//                });
//                return;
//            }
        });
    }

    private void onTcpData(TcpCommand command) {
        if (!TextUtils.equals(command.getDeviceID(), CommonLib.getInstance().getObeId())) {
            log.debug("设备不匹配, 请求id:{} 当前id:{}", command.getDeviceID(), CommonLib.getInstance().getObeId());
            return;
        }

        if (TextUtils.equals(command.getCommand(), "STREAM")) {
            if (mIStreamListener != null) {
                mIStreamListener.onStartStream();
            }
        } else if (TextUtils.equals(command.getCommand(), "NOSTREAM")) {
            if (mIStreamListener != null) {
                mIStreamListener.onStopStream();
            }
        }
    }

    public interface IStreamListener {
        void onStartStream();

        void onStopStream();
    }
}
