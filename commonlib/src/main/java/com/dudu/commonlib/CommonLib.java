package com.dudu.commonlib;

import android.content.Context;

import com.dudu.commonlib.repo.CommonResouce;
import com.dudu.commonlib.repo.VersionManage;
import com.dudu.commonlib.utils.DeviceIDUtil;

/**
 * Created by dengjun on 2016/1/21.
 * Description :公共库聚合根,需要在application中初始化
 */
public class CommonLib {
    private static CommonLib instance = null;

    private CommonResouce commonResouce;

    private VersionManage versionManage;

    public static CommonLib getInstance() {
        if (instance == null) {
            synchronized (CommonLib.class) {
                if (instance == null) {
                    instance = new CommonLib();
                }
            }
        }
        return instance;
    }

    private CommonLib() {
        commonResouce = new CommonResouce();
        versionManage = new VersionManage();
    }

    public void init(Context context) {
        commonResouce.init(context);
        versionManage.init(context);
    }

    public void init() {

    }

    public Context getContext() {
        return commonResouce.getContext();
    }

    public String getObeId() {
        String obeId = commonResouce.getObeId();
        if (obeId == null) {
            obeId = DeviceIDUtil.getIMEI(getContext());
        }
        return obeId;
    }

    public VersionManage getVersionManage() {
        return versionManage;
    }


}
