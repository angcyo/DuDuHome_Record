package com.dudu.voice.semantic.chain;

import android.content.Context;
import android.media.AudioManager;

import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.voice.semantic.bean.SemanticBean;

/**
 * Created by Robi on 2016-04-06 15:46.
 */
public class VolumeDefaultChain extends DefaultChain {

    public static final int VOLUME_INCREMENTAL = 3;
    private static String SMALL = "小一点";
    private static String LARGE = "大一点";
    private static String SMALL_TWO = "低一点";
    private static String LARGE_TWO = "高一点";
    private AudioManager mAudioManager;
    private int mMaxVolume;
    private int mCurVolume;

    public VolumeDefaultChain() {

        mAudioManager = (AudioManager) LauncherApplication.getContext()
                .getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean matchSemantic(String service) {
        return true;
    }

    @Override
    public boolean doSemantic(SemanticBean bean) {

        return changeVolume(bean);
    }

    private boolean changeVolume(SemanticBean bean) {

        if (bean != null) {
            String text = bean.getText();

            if (text.contains(SMALL) || text.contains(SMALL_TWO)) {
                turnDownVolume();
                return true;
            } else if (text.contains(LARGE) || text.contains(LARGE_TWO)) {
                turnUpVolume();
                return true;
            }
        }
        mVoiceManager.startSpeaking(Constants.UNDERSTAND_MISUNDERSTAND);

        return false;
    }

    private void turnUpVolume() {
        mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                mCurVolume + VOLUME_INCREMENTAL >= mMaxVolume ? mMaxVolume
                        : mCurVolume + VOLUME_INCREMENTAL,
                AudioManager.FLAG_PLAY_SOUND
                        | AudioManager.FLAG_SHOW_UI);
        mVoiceManager.startUnderstanding();
    }

    private void turnDownVolume() {
        mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                mCurVolume - VOLUME_INCREMENTAL <= 0 ? 0
                        : mCurVolume - VOLUME_INCREMENTAL,
                AudioManager.FLAG_PLAY_SOUND
                        | AudioManager.FLAG_SHOW_UI);
        mVoiceManager.startUnderstanding();
    }
}
