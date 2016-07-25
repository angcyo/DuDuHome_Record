package com.dudu.resource.resource;

/**
 * Created by dengjun on 2016/4/1.
 * Description : 自动关闭的抽象资源
 */
public abstract class AutoCloseAbstractResource extends AbstractResource implements  AutoCloseable{
    /* 设计待定*/
    @Override
    public void close() throws Exception {

    }
}
