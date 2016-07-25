package com.dudu.obd.common;

import java.io.Serializable;

/**
 * ReplyBody
 * 回复消息体基类
 * @author    Bob
 * @date      2015年12月3日 上午11:59:19
 */
public class ReplyBody implements Serializable {

    private static final long serialVersionUID = 1L;

    //返回码
    protected ReturnCode      returnCode;

    public ReturnCode getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(ReturnCode returnCode) {
        this.returnCode = returnCode;
    }

}
