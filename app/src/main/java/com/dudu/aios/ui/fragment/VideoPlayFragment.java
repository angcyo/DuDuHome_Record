package com.dudu.aios.ui.fragment;

import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dudu.aios.ui.fragment.base.RBaseFragment;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.ui.view.VideoView;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.persistence.factory.RealmCallFactory;
import com.dudu.persistence.factory.RealmModelFactory;
import com.dudu.persistence.realm.RealmCallBack;
import com.dudu.persistence.realm.RealmResultsQueryCondition;
import com.dudu.persistence.realmmodel.video.VideoEntity;
import com.dudu.persistence.realmmodel.video.VideoEntityRealm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;

import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by robi on 2016-03-15 14:01.
 */
public class VideoPlayFragment extends RBaseFragment implements View.OnClickListener {

    private final static int MESSAGE_PROGRESS_CHANGED = 0;
    private static final long DISAPPEAR_INTERVAL = 3000;

    private Logger log;

    private ImageButton btnBack, btnLast, btnPlay, btnNext, btnUpload, btnCancelUpload;
    private TextView tvDuration, tvNowDuration;
    private SeekBar seekBar;
    private LinearLayout uploadingContainer, uploadSuccessContainer;
    private ImageView uploadingIcon;

    private AnimationDrawable animationDrawable;

    private VideoView videoView;
    private boolean mPaused = false;
    private int position;

    private ArrayList<VideoEntity> mPlayList = new ArrayList<>();

    public VideoPlayFragment() {
        log = LoggerFactory.getLogger("video.videoui");
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_PROGRESS_CHANGED) {
                int position = videoView.getCurrentPosition();

                seekBar.setProgress(position);

                position /= 1000;
                int minutes = position / 60;
                int hours = minutes / 60;
                int seconds = position % 60;
                minutes %= 60;
                tvNowDuration.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

                sendEmptyMessageDelayed(MESSAGE_PROGRESS_CHANGED, 100);
            }
        }
    };

    @Override
    protected int getContentView() {
        return R.layout.activity_video_play;
    }

    @Override
    protected void initView(View rootView) {
        super.initView(rootView);
        btnBack = (ImageButton) mViewHolder.v(R.id.button_back);
        btnLast = (ImageButton) mViewHolder.v(R.id.button_last);
        btnPlay = (ImageButton) mViewHolder.v(R.id.button_play);
        btnNext = (ImageButton) mViewHolder.v(R.id.button_next);
        btnUpload = (ImageButton) mViewHolder.v(R.id.button_upload);
        btnCancelUpload = (ImageButton) mViewHolder.v(R.id.button_cancel_upload);
        tvDuration = (TextView) mViewHolder.v(R.id.tv_video_duration);
        tvNowDuration = (TextView) mViewHolder.v(R.id.tv_now_duration);
        seekBar = (SeekBar)mViewHolder.v(R.id.seekBar);
        uploadingContainer = (LinearLayout) mViewHolder.v(R.id.uploading_container);
        uploadSuccessContainer = (LinearLayout) mViewHolder.v(R.id.upload_successful_container);
        uploadingIcon = (ImageView) mViewHolder.v(R.id.image_uploading);

        videoView = (VideoView) mViewHolder.v(R.id.video_view);
        videoView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initViewData() {
        btnBack.setOnClickListener(this);
        btnLast.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        btnCancelUpload.setOnClickListener(this);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo(progress);
                }
            }
        });

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
//                showController();

                int duration = videoView.getDuration();

                seekBar.setMax(duration);

                duration /= 1000;

                int minutes = duration / 60;
                int hours = minutes / 60;
                int seconds = duration % 60;

                minutes %= 60;
                tvDuration.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

                videoView.start();

                mHandler.sendEmptyMessage(MESSAGE_PROGRESS_CHANGED);
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.seekTo(0);
                videoView.pause();
//                mPauseButton.setVisibility(View.VISIBLE);
                mPaused = true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                onButtonBack();
                break;
            case R.id.button_last:
                actionLast();
                break;
            case R.id.button_play:
                actionPlay();
                break;
            case R.id.button_next:
                actionNext();
                break;
            case R.id.button_upload:
                actionUpload();
                break;
            case R.id.button_cancel_upload:
                actionCancel();
                break;
        }
    }

    @Override
    public void onShow() {
        super.onShow();
        Uri uri = getArguments().getParcelable("uri");
        log.debug("文件地址：{}", uri.toString());
        if (uri != null){
            videoView.stopPlayback();
            videoView.setVideoURI(uri);
        }
        position = getIntent().getIntExtra(Constants.EXTRA_VIDEO_POSITION, 0);


        RealmCallFactory.queryVideoInfo(new RealmResultsQueryCondition<VideoEntityRealm>() {
            @Override
            public RealmResults<VideoEntityRealm> onCondition(RealmQuery realmQuery) {
                return realmQuery.equalTo("cameraFlag", true).findAll();
            }
        }, new RealmCallBack<RealmResults<VideoEntityRealm>, Exception>() {
            @Override
            public void onRealm(RealmResults<VideoEntityRealm> results) {
                ArrayList<com.dudu.persistence.realmmodel.video.VideoEntity> arrayList = RealmModelFactory.getVideoEntityListFromRealmResult(results);
                mPlayList.addAll(arrayList);
            }

            @Override
            public void onError(Exception error) {

            }
        });
    }

    private void onButtonBack(){
        finish();
    }

    private void actionCancel() {
        uploadingContainer.setVisibility(View.GONE);
        if (animationDrawable != null) {
            animationDrawable.stop();
        }
    }

    private void actionUpload() {
        uploadingContainer.setVisibility(View.VISIBLE);
        uploadingIcon.setImageResource(R.drawable.uplaod_video_arrows);
        animationDrawable = (AnimationDrawable) uploadingIcon.getDrawable();
        animationDrawable.start();
    }

    private void actionNext() {
        if (position < mPlayList.size() - 1) {
            position++;
//            mPauseButton.setVisibility(View.GONE);
            mPaused = false;
            videoView.setVideoURI(Uri.fromFile(new File(mPlayList.get(position).getAbsolutePath())));
        } else {
//
        }
    }

    private void actionPlay() {
        if (mPaused) {
            videoView.start();
            mPaused = false;
//            btnPlay.setVisibility(View.GONE);
        }else {
            videoView.pause();
            mPaused = true;
        }
    }

    private void actionLast() {
        if (position > 0) {
//            btnPlay.setVisibility(View.GONE);
            mPaused = false;
            position--;
            videoView.setVideoURI(Uri.fromFile(new File(mPlayList.get(position).getAbsolutePath())));
        } else {
//
        }
    }
}
