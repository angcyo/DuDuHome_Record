package com.dudu.persistence.rx;

import android.content.Context;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.File.FileUtil;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.rx.RealmObservableFactory;
import io.realm.rx.RxObservableFactory;
import rx.Observable;

/**
 * Created by dengjun on 2016/4/5.
 * Description :
 */
public class RealmManage {
    private static RealmManage instance = null;
    private static RealmConfiguration defaultConfig;
    private Realm realm;
    private RxObservableFactory rxObservableFactory;

    private RealmManage() {
        realm = Realm.getInstance(new RealmConfiguration.Builder(CommonLib.getInstance().getContext().getApplicationContext()).build());
        rxObservableFactory = new RealmObservableFactory();
    }

    public static RealmManage getInstance() {
        if (instance == null) {
            synchronized (RealmManage.class) {
                if (instance == null) {
                    instance = new RealmManage();
                }
            }
        }
        return instance;
    }

    public static <E extends RealmObject, T> Observable<E> createObservable(Class<E> value) {
        E realmObject = Realm.getInstance(new RealmConfiguration.Builder(CommonLib.getInstance().getContext().getApplicationContext()).build())
                .where(value)
                .findFirst();
        if (realmObject == null) {
            return null;
        } else {
            return realmObject.asObservable();
        }
    }

    public static void cleanRealm() {
        try {
            Realm.deleteRealm(defaultConfig);
        } catch (Exception e) {
//            Realm.removeDefaultConfiguration();
            File filesDir = CommonLib.getInstance().getContext().getFilesDir();
            FileUtil.deleteFile(filesDir.getAbsolutePath() + File.separatorChar + RealmConfiguration.DEFAULT_REALM_NAME + ".lock");
            FileUtil.deleteFile(filesDir.getAbsolutePath() + File.separatorChar + RealmConfiguration.DEFAULT_REALM_NAME);
        } finally {
        }
    }

    public static void init(Context context) {
        defaultConfig = getDefaultConfig(context);
        Realm.setDefaultConfiguration(defaultConfig);
    }

    public static RealmConfiguration getDefaultConfig(Context context) {
        return new RealmConfiguration.Builder(context).build();
    }

    public static Realm getRealm() {
        return Realm.getDefaultInstance();
    }

    public void removeAllChangeListeners() {
        realm.removeAllChangeListeners();
    }

    public void removeChangeListener(RealmChangeListener realmChangeListener) {
        realm.removeChangeListener(realmChangeListener);
    }

    public <E extends RealmObject> Observable<E> from(E var2) {
        return rxObservableFactory.from(realm, var2);
    }

    public <E extends RealmObject, T> Observable<E> from(Class<E> value) {
        return rxObservableFactory.from(realm, realm.where(value).findFirst());
    }
}
