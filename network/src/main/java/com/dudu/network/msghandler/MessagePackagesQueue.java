package com.dudu.network.msghandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by dengjun on 2016/3/5.
 * Description :
 */
public class MessagePackagesQueue <T>{
    //阻塞队列，用于存放要发送的消息
    private BlockingQueue<T> messagePackagesQueue;

    private Logger log;

    public MessagePackagesQueue(int capacity) {
        log = LoggerFactory.getLogger("network");
        messagePackagesQueue = new ArrayBlockingQueue<T>(capacity, true);
    }

    public  int size(){
        return  messagePackagesQueue.size();
    }

    public T remove(){
        return messagePackagesQueue.remove();
    }

    public void put(T messagePackage){
        try {
            messagePackagesQueue.put(messagePackage);
        } catch (InterruptedException e) {
            log.error("异常:",e);
        }
       notifyFinishWait();
    }

    /* 阻塞方式获取消息包*/
    public T getQueueHeadMessage(){
        T messagePackage = null;
        while ((messagePackage = messagePackagesQueue.peek()) == null) {//只取，不删，发送成功才删除
            try {//为null的情况说明队列里面没有要发送的消息，等待有发送的消息
                synchronized (messagePackagesQueue) {
                    messagePackagesQueue.wait();
                }
            } catch (InterruptedException e) {
                log.error("获取下一条消息异常:",e);
            }
        }
        return messagePackage;
    }

    public T peek(){
        return  messagePackagesQueue.peek();
    }

    public void notifyFinishWait(){
        synchronized (messagePackagesQueue) {
            messagePackagesQueue.notifyAll();
        }
    }
}
