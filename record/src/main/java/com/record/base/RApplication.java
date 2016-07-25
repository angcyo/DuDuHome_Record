package com.record.base;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.dudu.commonlib.CommonLib;
import com.dudu.persistence.factory.RRealm;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.orhanobut.hawk.LogLevel;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by angcyo on 16-03-04-004.
 */
public class RApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Hawk.init(this)
                .setEncryptionMethod(HawkBuilder.EncryptionMethod.MEDIUM)
                .setLogLevel(LogLevel.NONE)
                .setStorage(HawkBuilder.newSqliteStorage(this))
                .setPassword("angcyo")
                .build();

        RRealm.init(this, "record.realm", false/*是否清理数据库*/);

        CommonLib.getInstance().init(this);

        LeakCanary.install(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}
