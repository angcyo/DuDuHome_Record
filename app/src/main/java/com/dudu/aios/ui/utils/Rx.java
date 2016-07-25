package com.dudu.aios.ui.utils;

import android.util.Log;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by robi on 2016-04-21 15:41.
 */
public class Rx {

    public static <T, R> void base(T t, Func1<? super T, ? extends R> func, final Action1<? super R> onNext) {
        Observable.just(t).map(func).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(onNext,throwable -> Log.e("Rx", "base: ", throwable));
    }

    public static <T, R> void base(T t, Func1<? super T, ? extends R> func) {
        Observable.just(t).map(func).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe();
    }
}
