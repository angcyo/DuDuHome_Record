package com.record.util;

import android.content.Context;

import com.record.BuildConfig;
import com.record.view.DebugWindow;

/**
 * Created by robi on 2016-06-23 21:20.
 */
public class Debug {
    public static void show(Context context, String msg, int color) {
        if (BuildConfig.DEBUG) {
            DebugWindow.instance(context).addText(Thread.currentThread().getId() + " " + msg, color);
        }
    }

    public static void show(Context context, String msg) {
        if (BuildConfig.DEBUG) {
            DebugWindow.instance(context.getApplicationContext()).addText(Thread.currentThread().getId() + " " + msg);
        }
    }
}
