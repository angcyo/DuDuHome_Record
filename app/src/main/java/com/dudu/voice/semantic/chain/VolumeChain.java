package com.dudu.voice.semantic.chain;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;

import com.dudu.aios.ui.activity.MainRecordActivity;
import com.dudu.aios.ui.base.BaseFragmentManagerActivity;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.utils.ActivitiesManager;
import com.dudu.map.NavigationProxy;
import com.dudu.voice.semantic.bean.SemanticBean;
import com.dudu.voice.semantic.bean.VolumeBean;
import com.dudu.voice.semantic.constant.SceneType;
import com.dudu.voice.semantic.constant.SemanticConstant;
import com.dudu.voice.semantic.engine.SemanticEngine;

/**
 * Created by 赵圣琪 on 2015/10/28.
 */
public class VolumeChain extends SemanticChain {

    public static final int VOLUME_INCREMENTAL = 3;

    private AudioManager mAudioManager;

    private int mMaxVolume;

    private int mCurVolume;

    private int mPreVolume;

    public VolumeChain() {
        super();
        mAudioManager = (AudioManager) LauncherApplication.getContext()
                .getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean matchSemantic(String service) {
        return SemanticConstant.SERVICE_VOLUME.equalsIgnoreCase(service);
    }

    @Override
    public boolean doSemantic(SemanticBean semantic) {

        VolumeBean bean = (VolumeBean) semantic;
        String operation = bean.getOperation();

        if (operation.equals("+")) {
            turnUpVolume();
        } else if (operation.equals("-")) {
            turnDownVolume();
        } else if (operation.equals("max")) {
            turnVolumeToValue(mMaxVolume);
        } else if (operation.equals("min")) {
            turnVolumeToValue(1);
        } else if (operation.equals("mute_on") || operation.equals("0")) {
            turnVolumeToValue(0);
        } else if (operation.equals("mute_off") || operation.equals("open")) {
            return turnOnVolume();
        } else {
            return false;
        }

        mVoiceManager.startUnderstanding();
        return true;
    }

    private void turnUpVolume() {
        mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                mCurVolume + VOLUME_INCREMENTAL >= mMaxVolume ? mMaxVolume
                        : mCurVolume + VOLUME_INCREMENTAL,
                AudioManager.FLAG_PLAY_SOUND);
        setSceneType();

    }

    private void turnDownVolume() {
        mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mAudioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                mCurVolume - VOLUME_INCREMENTAL <= 0 ? 0
                        : mCurVolume - VOLUME_INCREMENTAL,
                AudioManager.FLAG_PLAY_SOUND);

        setSceneType();


    }


    private void turnVolumeToValue(int value) {
        if (value == 0) {
            mPreVolume = mCurVolume;
        }

        mCurVolume = value;
        mAudioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                value,
                AudioManager.FLAG_PLAY_SOUND);

        setSceneType();


    }

    private boolean turnOnVolume() {
        if (mCurVolume != 0) {
            return false;
        }

        mVoiceManager.startUnderstanding();

        mAudioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                10,
                AudioManager.FLAG_PLAY_SOUND);
        setSceneType();
        return true;

    }

    private void setSceneType() {
        if (!NavigationProxy.getInstance().isShowList() && !isCarChecking()) {
            SemanticEngine.getProcessor().switchSemanticType(SceneType.VOLUME);

        }
    }

    private boolean isCarChecking() {

        Activity topActivity = ActivitiesManager.getInstance().getTopActivity();

        if (topActivity instanceof MainRecordActivity
                && ((BaseFragmentManagerActivity) topActivity).getCurrentStackTag().equals(FragmentConstants.REPAIR_FAULT_CODE_FRAGMENT)) {
            return true;
        }
        return false;

    }
}
