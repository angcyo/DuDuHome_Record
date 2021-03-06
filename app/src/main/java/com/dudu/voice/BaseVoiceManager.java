package com.dudu.voice;

import android.content.Context;

import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.event.DeviceEvent;
import com.dudu.voice.semantic.constant.TTSType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by 赵圣琪 on 2015/12/24.
 */
public abstract class BaseVoiceManager implements VoiceManager {

    public static final int MAX_MISUNDERSTAND_COUNT = 2;

    /**
     * 语音出错次数，限制为3次
     */
    protected AtomicInteger mMisunderstandCounter = new AtomicInteger(0);

    protected Context mContext;

    protected TTSType mType;

    protected Logger log;

    protected boolean isInitOver = false;

    protected int initStep = 0;

    public BaseVoiceManager() {
        log = LoggerFactory.getLogger("voice.manager");

        mContext = LauncherApplication.getContext();
    }

    @Override
    public void startVoiceService() {

        log.debug("开启语义服务... isInitOver {}", isInitOver);

        if (!isInitOver) {
//            startWakeup();
            return;
        }

        EventBus.getDefault().post(new DeviceEvent.Screen(DeviceEvent.ON));

        clearMisUnderstandCount();

        Observable.timer(200, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    startSpeaking(Constants.WAKEUP_WORDS, TTSType.TTS_START_UNDERSTANDING, false);
                });

        FloatWindowUtils.showAnimWindow();


    }

    @Override
    public void startSpeaking(String playText) {
        startSpeaking(playText, TTSType.TTS_START_UNDERSTANDING);
    }

    @Override
    public void startSpeaking(String playText, TTSType type) {
        startSpeaking(playText, TTSType.TTS_START_UNDERSTANDING, true);
    }

    @Override
    public void incrementMisUnderstandCount() {
        mMisunderstandCounter.incrementAndGet();
    }

    @Override
    public void clearMisUnderstandCount() {
        mMisunderstandCounter.set(0);
    }

    protected boolean checkMisUnderstandCount() {
        if (mMisunderstandCounter.get() >= MAX_MISUNDERSTAND_COUNT) {
            return true;
        }

        return false;
    }


}
