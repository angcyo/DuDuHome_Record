package com.dudu.commonlib.utils.process;

import android.app.ActivityManager;
import android.content.Context;

/**
 * Created by dengjun on 2016/5/23.
 * Description :
 */
public class ProcessUtils {
    public  static  String getCurProcessName (Context context) {
        int     pid = android.os.Process.myPid ();
        ActivityManager mActivityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses())  {
            if(appProcess.pid == pid){
                return appProcess.processName;
            }
        }
        return null ;
    }
}
