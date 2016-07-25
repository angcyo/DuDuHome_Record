package com.dudu.voice.semantic.chain;

import android.app.Activity;
import android.provider.Settings;
import android.view.WindowManager;

import com.dudu.aios.ui.activity.MainRecordActivity;
import com.dudu.aios.ui.base.VolBrightnessSetting;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.voice.semantic.bean.BrightnessBean;
import com.dudu.voice.semantic.bean.SemanticBean;
import com.dudu.voice.semantic.constant.SemanticConstant;

/**
 * Created by lxh on 2016-04-20 16:04.
 */
public class BrightnessChain extends SemanticChain {

    public static final String DOWN = "down";
    public static final String UP = "up";
    public static final int BRIGHTNESS_STEP = 60;
    public static int staticBrightness = 255;

    private int currentBrightness;

    @Override
    public boolean matchSemantic(String service) {
        return SemanticConstant.SERVICE_BRIGHTNESS.equalsIgnoreCase(service);
    }

    @Override
    public boolean doSemantic(SemanticBean bean) {

        if (bean != null) {

            String action = ((BrightnessBean) bean).getAction();
            switch (action) {
                case DOWN:
                case "-":
                    down();
                    break;
                case UP:
                case "+":
                    up();
                    break;
            }
            synchroBrightness(currentBrightness);
            mVoiceManager.startUnderstanding();
            return true;
        }
        return false;
    }

    private void down() {
        currentBrightness = Settings.System.getInt(ActivitiesManager.getInstance().getTopActivity().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, -1);
        if (currentBrightness >= 17) {
            currentBrightness = currentBrightness - BRIGHTNESS_STEP;
            if (currentBrightness < 17) {
                currentBrightness = 0;
            }
        }
    }

    private void up() {
        currentBrightness = Settings.System.getInt(ActivitiesManager.getInstance().getTopActivity().getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS, -1);
        currentBrightness = currentBrightness + BRIGHTNESS_STEP;
        if (currentBrightness > staticBrightness) {
            currentBrightness = staticBrightness;
        }
    }

    public void synchroBrightness(int brightness) {
        Activity activity = ActivitiesManager.getInstance().getTopActivity();
        VolBrightnessSetting.setScreenMode(activity, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
        currentBrightness = Settings.System.getInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
        MainRecordActivity.appActivity.showBrightnessView(currentBrightness);

//        staticBrightness = currentBrightness;

        WindowManager.LayoutParams wl = activity.getWindow().getAttributes();
        float tmpFloat = (float) brightness / 255;
        if (tmpFloat > 0 && tmpFloat <= 1) {
            wl.screenBrightness = tmpFloat;
        }
        activity.getWindow().setAttributes(wl);
    }
}
