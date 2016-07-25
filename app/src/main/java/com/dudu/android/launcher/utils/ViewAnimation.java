package com.dudu.android.launcher.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by Administrator on 2015/11/19.
 */
public class ViewAnimation {

    private static OnAnimPlayListener animPlayListener;

    public static void onAnimPlayListener(OnAnimPlayListener listener) {
        animPlayListener = listener;
    }

    public static void startAnimation(final View view, int animId, Context context) {
        Animation anim = AnimationUtils.loadAnimation(context, animId);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (animPlayListener != null && view.getVisibility() == View.VISIBLE) {
                    animPlayListener.play(true);
                }
                view.setVisibility(view.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(anim);
    }

    public interface OnAnimPlayListener {
        public void play(boolean isPlay);
    }
}
