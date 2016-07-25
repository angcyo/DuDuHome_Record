package com.record.state;

import android.content.Context;
import android.provider.Settings;

/**
 * Created by robi on 2016-06-06 16:48.
 */
public class RecordControl {

    public static final String DOCK_AUDIO_MEDIA_ENABLED = "dock_audio_media_enabled";

    public static void saveRecordState(Context context, int n) {
        Settings.Global.putInt(context.getContentResolver(), "record", n);
//        Settings.Global.putInt(context.getContentResolver(), DOCK_AUDIO_MEDIA_ENABLED, n);
    }

    public static int getSaveRecordState(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "record", -1);
//        return Settings.Global.getInt(context.getContentResolver(), DOCK_AUDIO_MEDIA_ENABLED, -1);
    }
}
