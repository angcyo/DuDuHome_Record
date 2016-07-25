package com.dudu.resource.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dengjun on 2016/3/31.
 * Description :  初始化过程异步的资源继承此类
 */
public abstract class AsyncAbstactResource extends AbstractResource {
    private List<ResourceStateCallBack> resourceStateCallBackList;

    public void registerStateListener(ResourceStateCallBack resourceStateCallBack) {
        if (resourceStateCallBackList == null) {
            resourceStateCallBackList = Collections.synchronizedList(new ArrayList());
        }
        if (resourceStateCallBack != null) {
            resourceStateCallBackList.add(resourceStateCallBack);
        }
    }

    public void unRegisterStateListener(ResourceStateCallBack resourceStateCallBack) {
        if (resourceStateCallBackList != null) {
            if (resourceStateCallBack != null && resourceStateCallBackList.contains(resourceStateCallBack)) {
                resourceStateCallBackList.remove(resourceStateCallBack);
            }
        }
    }
}
