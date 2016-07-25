package com.dudu.android.launcher.exception;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.dudu.android.launcher.BuildConfig;
import com.dudu.android.launcher.utils.FileUtils;
import com.dudu.android.launcher.utils.LogUtils;
import com.dudu.android.launcher.utils.TimeUtils;
import com.dudu.init.InitManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;

public class CrashHandler implements UncaughtExceptionHandler {

    public static final String INTENT_ACTION_RESTART_ACTIVITY = "com.dudu.crash";

    public static final String TAG = "CrashHandler";

    private static CrashHandler mInstance;

    private Context mContext;

    private Logger logger = LoggerFactory.getLogger("crash");

    private UncaughtExceptionHandler mDefaultHandler;

    private CrashHandler() {

    }

    public static CrashHandler getInstance() {
        if (mInstance == null) {
            mInstance = new CrashHandler();
        }

        return mInstance;
    }

    public static void restartApplicationWithIntent(Activity activity, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.finish();
        activity.startActivity(intent);
        killCurrentProcess();
    }

    private static void killCurrentProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    public static Class<? extends Activity> getLauncherActivity(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            try {
                return (Class<? extends Activity>) Class.forName(intent.getComponent().getClassName());
            } catch (ClassNotFoundException e) {
            }
        }
        return null;
    }

    private static Class<? extends Activity> getRestartActivityClassWithIntentFilter(Context context) {
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(
                new Intent().setAction(INTENT_ACTION_RESTART_ACTIVITY),
                PackageManager.GET_RESOLVED_FILTER);

        for (ResolveInfo info : resolveInfos) {
            if (info.activityInfo.packageName.equalsIgnoreCase(context.getPackageName())) {
                try {
                    return (Class<? extends Activity>) Class.forName(info.activityInfo.name);
                } catch (ClassNotFoundException e) {
                    //Should not happen, print it to the log!
                    Log.e("TAG", "Failed when resolving the restart activity class via intent filter, stack trace follows!", e);
                }
            }
        }

        return null;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        logger.error("uncaughtException", ex);

//        CameraControl.exit();

        handleException(ex);
        if (BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
            try {
                Class<? extends Activity> restartClass = getRestartActivityClassWithIntentFilter(mContext);
                if (restartClass != null) {
                    Intent intent = new Intent(mContext, restartClass);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);//去掉动画效果
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Bundle args = new Bundle();
                    args.putString("msg", getMsgFromThrowable(ex));
                    intent.putExtras(args);
                    mContext.startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Intent intent = new Intent(mContext, CrashHandler.getLauncherActivity(mContext));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            mContext.startActivity(intent);
        }

        InitManager.getInstance().unInit();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);

//        if (!handleException(ex) && mDefaultHandler != null) {
//            mDefaultHandler.uncaughtException(thread, ex);
//        } else {
//        }
    }

    public boolean handleException(final Throwable ex) {
        if (ex == null) {
            return false;
        }

//		VideoManager.getInstance().stopRecord();

        new Thread(new Runnable() {

            @Override
            public void run() {
                String fileName = "crash-" + TimeUtils.format(TimeUtils.format5)
                        + ".txt";
                try {
                    Writer info = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(info);
                    ex.printStackTrace(printWriter);

                    String result = info.toString();
                    printWriter.close();

                    FileOutputStream fos = getFileOutputStream(fileName);
                    if (!TextUtils.isEmpty(result)) {
                        fos.write(result.getBytes());
                    }

                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    LogUtils.e(TAG, e.getMessage());
                }
            }
        }).run();

        return false;
    }

    private FileOutputStream getFileOutputStream(String fileName)
            throws FileNotFoundException {
        if (FileUtils.isSdCard()) {
            File directory = new File(FileUtils.getSdPath() + "/dudu/crash");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            return new FileOutputStream(new File(directory, fileName));
        }

        return mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
    }

    private String getMsgFromThrowable(Throwable ex) {
        StringWriter stringWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
