package com.dudu.obd.common;

/**
 * 心跳检测的消息类型
 * @author    Administrator
 * @date      2015年12月3日 上午11:54:09
 */
public class PingMsg extends BaseMsg {

    private static final long serialVersionUID = 8111484689248278247L;

    public PingMsg() {
        super();
        setType(MsgType.PING);
    }
}
