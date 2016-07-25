package com.dudu.network.message;

/**
 * Created by dengjun on 2016/3/4.
 * Description :
 */
public abstract class MessagePackage<T> {
    //设置消息ID
    public abstract void setMessageId(String messageId);

    //获取消息ID
    public abstract  String getMessageId();


    /*
    * 标识此包数据发送后是否需要等待响应，目前处理方式发送后不等待响应
    * 预留接口，后面可能会需要发送数据等待响应后再发下一条
    */
    public abstract boolean isNeedWaitResponse();

    //标识此包数据是否需要加密
    public abstract boolean isNeedEncrypt();

    // json格式字符串解码生成具体的业务数据方法，子类实现
    public abstract void createFromJsonString(String messageJsonString);

    //具体业务数据编码成json格式的消息字符串
    public abstract String toJsonString();

    //标记消息包发送失败的时候，是否需要缓存数据，待网络恢复再次发送
    public abstract boolean isNeedCache();

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        } else {
            if (object instanceof MessagePackage){
                MessagePackage messagePackage = (MessagePackage)object;
                if (messagePackage.getMessageId().equals(this.getMessageId())){
                    return true;
                }
            }
            return false;
        }
    }

    public abstract  T getMessageEntity();

    public abstract void setMessageEntity(T messageEntity);

    public abstract String getBusinessCode();

    public abstract void setBusinessCode(String businessCode);
}
