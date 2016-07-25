package com.dudu.obd.common;

/**
 * 请求类型的消息
 * @author    Bob
 * @date      2015年12月3日 上午11:45:03
 */
public class AskMsg extends BaseMsg {

    private static final long serialVersionUID = -60582389973723416L;

    public AskMsg() {
        super();
        setType(MsgType.ASK);

        params = new AskParams();
    }

    private AskParams params;

    public AskParams getParams() {
        return params;
    }

    public void setParams(AskParams params) {
        this.params = params;
    }
}
