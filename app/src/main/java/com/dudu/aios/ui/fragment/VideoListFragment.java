package com.dudu.aios.ui.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.dudu.aios.ui.activity.MainRecordActivity;
import com.dudu.aios.ui.base.ObservableFactory;
import com.dudu.aios.ui.base.T;
import com.dudu.aios.ui.fragment.base.BaseFragment;
import com.dudu.aios.ui.fragment.base.RBaseViewHolder;
import com.dudu.aios.ui.fragment.video.FileUploadHelper;
import com.dudu.aios.ui.fragment.video.MediaLoadHelper;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.aios.ui.view.DuduChooseButton;
import com.dudu.aios.ui.view.DuduProgressBar;
import com.dudu.aios.ui.view.DuduUploadBarLayout;
import com.dudu.aios.ui.view.RBaseAdapter;
import com.dudu.aios.ui.view.RRecyclerView;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.ui.view.VideoView;
import com.dudu.android.launcher.utils.Constants;
import com.dudu.commonlib.resource.sdcard.ISdcardListener;
import com.dudu.commonlib.resource.sdcard.SdcardManager;
import com.dudu.commonlib.utils.afinal.FinalBitmap;
import com.dudu.drivevideo.frontcamera.FrontCameraManage;
import com.dudu.drivevideo.utils.FileUtil;
import com.dudu.event.ChooseEvent;
import com.dudu.persistence.realmmodel.video.VideoEntity;
import com.dudu.voice.VoiceManagerProxy;
import com.dudu.voice.semantic.constant.TTSType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.greenrobot.event.EventBus;

public class VideoListFragment extends BaseFragment implements View.OnClickListener, DuduChooseButton.OnChooseListener, ISdcardListener {

//    private CusomSwipeView videoListView;
//    private VideoListViewAdapter videoListViewAdapter;

    private static final int FLAG_FACE = 1;//表示当前显示的是前置视频数据
    private static final int FLAG_BACK = 2;//后置
    private final static int MESSAGE_PROGRESS_CHANGED = 0;
    private Logger log = LoggerFactory.getLogger("video.VideoStorage");
    private LinearLayout emptyView;
    private ImageButton mPreVideoButton, mPostVideoButton, mBackButton;
    private TextView mPreVideoTextChinese, mPostVideoTextChinese, mPreVideoTextEnglish, mPostVideoTextEnglish;
    private int mPerPageItemNum = 6;
    private int mCurrentPage = 0;
    private DuduChooseButton backButton, faceButton;
    private RRecyclerView videoListView;
    private VideoAdapter videoAdapter;
    private ViewSwitcher viewSwitcher;
    private VideoView videoView;
    private MediaLoadHelper mMediaLoadHelper;
    private List<VideoEntity> mFaceVideoData;//前置视频数据
    private List<VideoEntity> mBackVideoData;//后置视频数据
    private int flag = FLAG_FACE;
    private View prevButton;
    private View nextButton;
    private ImageButton btnBack, btnLast, btnPlay, btnNext, btnUpload, btnCancelUpload;
    private TextView tvDuration, tvNowDuration;
    private SeekBar seekBar;
    private boolean mPaused = false;
    private int position;//当前视频播放的位置
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
    private VideoComparator mVideoComparator;
    private DuduProgressBar sdcardProgressBar;

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_video_list, null);
        initFragmentView(view);
        initClickListener();
        initVideoData();
        return view;
    }

    private void initVideoData() {

//        videoListViewAdapter = new VideoListViewAdapter(this, mVideoData);
        //设置布局管理器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        videoListView.setLayoutManager(linearLayoutManager);
//        videoListView.setAdapter(videoListViewAdapter);

        videoAdapter = new VideoAdapter(getActivity(), null);
        videoAdapter.setLayoutManager((LinearLayoutManager) videoListView.getLayoutManager());
        videoListView.setAdapter(videoAdapter);
//        loadVideos();

//        videoListViewAdapter = new VideoListViewAdapter(this, mVideoData);
//        设置布局管理器
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
//        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        videoListView.setLayoutManager(linearLayoutManager);
//        videoListView.setAdapter(videoListViewAdapter);


//        new LoadVideoTask().execute();
        mVideoComparator = new VideoComparator();
        onShow();

        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
    }

    private void initClickListener() {
        mBackButton.setOnClickListener(this);
        faceButton.setChooseListener(this);
        backButton.setChooseListener(this);
    }

    private void initFragmentView(View view) {
        emptyView = (LinearLayout) view.findViewById(R.id.video_empty_container);
        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        faceButton = (DuduChooseButton) view.findViewById(R.id.faceButton);
        backButton = (DuduChooseButton) view.findViewById(R.id.backButton);
        videoListView = (RRecyclerView) view.findViewById(R.id.recyclerView);
        viewSwitcher = (ViewSwitcher) view.findViewById(R.id.viewSwitcher);
        sdcardProgressBar = (DuduProgressBar) view.findViewById(R.id.sdcardProgressBar);
//        videoView = (VideoView) view.findViewById(R.id.video_view);
//        videoView.setZOrderMediaOverlay(true);
//        videoView.setZOrderOnTop(true);
//        MediaController mediaController = new MediaController(getActivity());
//        videoView.setMediaController(mediaController);
//        mediaController.setMediaPlayer(videoView);

        view.findViewById(R.id.button_back_video).setOnClickListener(v -> {
            //视频播放界面,返回按钮
            showPrevious();
        });
        prevButton = view.findViewById(R.id.previous_page_button);
        prevButton.setOnClickListener(v -> {
            //上一页
            videoListView.smoothScrollBy(-videoListView.getMeasuredWidth(), 0);
        });
        nextButton = view.findViewById(R.id.next_page_button);
        nextButton.setOnClickListener(v -> {
            //下一页
            videoListView.smoothScrollBy(videoListView.getMeasuredWidth(), 0);
        });


//        videoListView = (CusomSwipeView)view.findViewById(R.id.video_list_view);
        initVideoLayoutView(view);
    }

    private void showPrevious() {
//        CameraControl.instance().recordControl();
        FrontCameraManage.getInstance().startRecord();

        ObservableFactory.getInstance().getCommonObservable().hasTitle.set(true);
        videoView.setZOrderMediaOverlay(false);
        videoView.setVisibility(View.GONE);
//            videoView.setZOrderOnTop(true);
        videoView.stopPlayback();
        videoView.setVideoPath("");
        viewSwitcher.showPrevious();
    }

    /**
     * 视频播放层
     */
    private void initVideoLayoutView(View view) {
        btnLast = (ImageButton) view.findViewById(R.id.button_last);
        btnPlay = (ImageButton) view.findViewById(R.id.button_play);
        btnNext = (ImageButton) view.findViewById(R.id.button_next);
        tvDuration = (TextView) view.findViewById(R.id.tv_video_duration);
        tvNowDuration = (TextView) view.findViewById(R.id.tv_now_duration);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);

        videoView = (VideoView) view.findViewById(R.id.video_view);

        videoView.setVisibility(View.VISIBLE);
        videoView.setZOrderMediaOverlay(true);

        videoView.setSimpleOnGestureListener(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                actionPlay();
                return true;
            }
        });

        btnLast.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnNext.setOnClickListener(this);

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
                if (minutes <= duration)
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
                showVideoList();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
                replaceFragment(FragmentConstants.FRAGMENT_DRIVING_RECORD);
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
        }
    }

    /*播放下一个*/
    private void actionNext() {
        mPaused = false;
        videoView.setVideoPath(getNextVideoPath());
    }

    private void actionPlay() {
        if (mPaused) {
            videoView.start();
            mPaused = false;
//            btnPlay.setVisibility(View.GONE);
        } else {
            videoView.pause();
            mPaused = true;
        }
    }

    /*播放上一个*/
    private void actionLast() {
        mPaused = false;
        videoView.setVideoPath(getPrevVideoPath());
    }

    private String getPrevVideoPath() {
        String path;
        position--;
        if (flag == FLAG_FACE) {
            path = mFaceVideoData.get(position).getAbsolutePath();
        } else {
            path = mBackVideoData.get(position).getAbsolutePath();
        }
        refreshVideoControl();
        return path;
    }

    private String getNextVideoPath() {
        String path;
        position++;
        if (flag == FLAG_FACE) {
            path = mFaceVideoData.get(position).getAbsolutePath();
        } else {
            path = mBackVideoData.get(position).getAbsolutePath();
        }
        refreshVideoControl();
        return path;
    }

    private String getVideoPath(int pos) {
        String path;
        pos -= 1;
        if (pos < 0) {
            return null;
        }
        if (flag == FLAG_FACE) {
            if (pos >= mFaceVideoData.size()) {
                return null;
            }
            path = mFaceVideoData.get(pos).getAbsolutePath();
        } else {
            if (pos >= mBackVideoData.size()) {
                return null;
            }
            path = mBackVideoData.get(pos).getAbsolutePath();
        }
        return path;
    }

    /**
     * 控制播放的上一个,下一个按钮
     */
    private void refreshVideoControl() {
        int max;
        if (flag == FLAG_FACE) {
            max = mFaceVideoData.size();
        } else {
            max = mBackVideoData.size();
        }
        if (position + 1 >= max) {
            btnNext.setVisibility(View.INVISIBLE);
        } else {
            btnNext.setVisibility(View.VISIBLE);
        }
        if (position - 1 < 0) {
            btnLast.setVisibility(View.INVISIBLE);
        } else {
            btnLast.setVisibility(View.VISIBLE);
        }
    }

    public void replaceFragment(String name) {
        MainRecordActivity activity = (MainRecordActivity) getActivity();
        activity.replaceFragment(name);
    }

    @Override
    public void onPause() {
        super.onPause();
        showVideoList();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onChoose(View view, boolean choose) {
        if (view.getId() == R.id.faceButton && choose) {
            switchChooseButton(choose);
        } else if (view.getId() == R.id.backButton && choose) {
            switchChooseButton(!choose);
        } else {
            ((DuduChooseButton) view).setChoose(!choose);
        }
    }

    private void switchChooseButton(boolean faceChoose) {
        faceButton.setChoose(faceChoose);
        backButton.setChoose(!faceChoose);
        int oldFlag = flag;
        if (faceChoose) {
            flag = FLAG_FACE;
        } else {
            flag = FLAG_BACK;
        }

        if (oldFlag != flag) {
            refreshRecyclerView();
        }
    }

    public void onVideoPlay(String videoPath, int position) {
        if (!new File(videoPath).exists()) {
            T.show(getActivity(), "视频文件不存在,无法播放.");
            return;
        }

//        CameraControl.instance().setRecordNoState(false);
        FrontCameraManage.getInstance().stopRecord();

        ObservableFactory.getInstance().getCommonObservable().hasTitle.set(false);

        this.position = position;
        viewSwitcher.showNext();
        if (TextUtils.isEmpty(videoPath) || !new File(videoPath).exists()) {
            return;
        }
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoPath(videoPath);
        videoView.requestFocus();
        videoView.start();
        refreshVideoControl();
    }

    @Override
    public void onShow() {
        super.onShow();
        SdcardManager.instance().addListener(this);

        MediaLoadHelper.getAllFaceVideos(beans -> {
            mFaceVideoData = beans;
            Collections.sort(mFaceVideoData, mVideoComparator);

            int size = mFaceVideoData.size();
            filterList(mFaceVideoData);

            refreshRecyclerView();
            int size2 = mFaceVideoData.size();

            log.info("查询到前置视频数量:{} 过滤:{}", size2, size - size2);
        });

        MediaLoadHelper.getAllBackVideos(beans -> {
            mBackVideoData = beans;
            Collections.sort(mBackVideoData, mVideoComparator);

            int size = mBackVideoData.size();
            filterList(mBackVideoData);

            refreshRecyclerView();
            int size2 = mBackVideoData.size();
            log.info("查询到后置视频数量:{} 过滤:{}", size2, size - size2);
        });

        calcSdProgress();
    }

    private void filterList(List<VideoEntity> list) {
        for (int i = 0; i < list.size(); i++) {
            if (!isExist(list.get(i).getAbsolutePath())) {
                list.remove(i);
                filterList(list);
                return;
            }
        }
    }

    private boolean isExist(String filePath) {
        return new File(filePath).exists();
    }

    private void calcSdProgress() {
        if (FileUtil.isTFlashCardExists()) {
            sdcardProgressBar.setVisibility(View.VISIBLE);
            float sdFreeSpace = FileUtil.getSdFreeSpace();//剩余空间
            sdcardProgressBar.setProgress(1 - sdFreeSpace);//已用空间比率
            if (sdFreeSpace < 0.3) {
                sdcardProgressBar.setProgressColor(Color.RED);
            } else {
                sdcardProgressBar.setProgressColor(Color.parseColor("#398DEE"));
            }
        } else {
            sdcardProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onHide() {
        super.onHide();
        SdcardManager.instance().removeListener(this);
        videoView.stopPlayback();
        sdcardProgressBar.setProgress(0f, false);

        showVideoList();
    }

    private void showVideoList() {
        if (viewSwitcher.getDisplayedChild() == 1) {
            showPrevious();
        }
    }

    private void refreshRecyclerView() {
        if (flag == FLAG_FACE) {
            if (mFaceVideoData == null || mFaceVideoData.isEmpty()) {
                showEmpty();
            } else {
                hideEmpty();
            }
            videoAdapter.resetData(mFaceVideoData);
        } else {
            if (mBackVideoData == null || mBackVideoData.isEmpty()) {
                showEmpty();
            } else {
                hideEmpty();
            }
            videoAdapter.resetData(mBackVideoData);
        }
    }

    private void showEmpty() {
        emptyView.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.GONE);
        prevButton.setVisibility(View.GONE);

        TextView emptyTip = (TextView) emptyView.findViewById(R.id.emptyTip);
        if (FileUtil.isTFlashCardExists()) {
            emptyTip.setText(R.string.video_empty);
        } else {
            emptyTip.setText(R.string.video_empty_ntf);
        }
    }

    private void hideEmpty() {
        emptyView.setVisibility(View.GONE);
        nextButton.setVisibility(View.VISIBLE);
        prevButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAdd() {
        super.onAdd();
        SdcardManager.instance().addListener(this);
    }

    @Override
    public void onMounted() {
        onShow();
    }

    @Override
    public void onRemoved() {
        showVideoList();
        onShow();
    }

    public void onEvent(ChooseEvent chooseEvent) {

        int position = chooseEvent.getPosition();
        if (chooseEvent.getChooseType() == ChooseEvent.ChooseType.PLAY_VIDEO) {
            String videoPath = getVideoPath(position);
            if (TextUtils.isEmpty(videoPath)) {
                VoiceManagerProxy.getInstance().startSpeaking(Constants.MAP_CHOISE_ERROR, TTSType.TTS_START_UNDERSTANDING, false);
            } else {
                onVideoPlay(videoPath, position);
            }
        }
    }

    class VideoComparator implements Comparator<VideoEntity> {

        @Override
        public int compare(VideoEntity lhs, VideoEntity rhs) {
            return rhs.getFileName().compareToIgnoreCase(lhs.getFileName());
        }
    }

    public class VideoAdapter extends RBaseAdapter<VideoEntity> {
        List<Drawable> list = new ArrayList<>();
        int scrollState = RecyclerView.SCROLL_STATE_IDLE;
        int firstItem = 0;
        int lastItem = 0;
        private LinearLayoutManager layoutManager;
        private FinalBitmap finalBitmap;
        private boolean isFirstLoad;

        public VideoAdapter(Context context, List<VideoEntity> datas) {
            super(context, datas);
            Resources resources = context.getResources();
            list.add(resources.getDrawable(R.drawable.upload_1));
            list.add(resources.getDrawable(R.drawable.upload_2));
            list.add(resources.getDrawable(R.drawable.upload_3));
            list.add(resources.getDrawable(R.drawable.upload_4));

            finalBitmap = new FinalBitmap(context);
            isFirstLoad = true;
        }

        public void setLayoutManager(LinearLayoutManager layoutManager) {
            this.layoutManager = layoutManager;
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            scrollState = newState;
            isFirstLoad = false;
            int first = layoutManager.findFirstVisibleItemPosition();
            int last = layoutManager.findLastVisibleItemPosition();
            layoutManager.getItemCount();
            if (scrollState == RecyclerView.SCROLL_STATE_IDLE && (firstItem != first || lastItem != last)) {
                firstItem = first;
                lastItem = last;
                isFirstLoad = true;
//                notifyItemRangeChanged(firstItem, layoutManager.getChildCount());
                for (int i = first; i <= last; i++) {
                    View view = layoutManager.findViewByPosition(i);
                    if (view != null) {
                        ImageView videoThumbnailView = (ImageView) view.findViewById(R.id.videoThumbnailView);
                        if (videoThumbnailView != null) {
                            Drawable drawable = videoThumbnailView.getDrawable();
                            if (drawable == null) {
                                notifyItemChanged(i);
                            } else if (drawable instanceof BitmapDrawable) {
                                if (((BitmapDrawable) drawable).getBitmap() == null) {
                                    notifyItemChanged(i);
                                }
                            }
                        }
                    }
                }
            }
        }

        @Override
        protected int getItemLayoutId(int viewType) {
            return R.layout.fragment_video_list_item_layout;
        }

        @Override
        protected void onBindView(RBaseViewHolder holder, int position, VideoEntity bean) {
            holder.tV(R.id.videoNameView).setText(bean.getFileName());
            bindUploadBar(holder, bean);
            holder.v("lockLayout").setOnClickListener(v -> {
                //加锁,解锁操作
                bean.setLockFlag(!bean.isLockFlag());
                bindLockView(holder, bean);
                holder.v("deleteView").setVisibility(bean.isLockFlag() ? View.GONE : View.VISIBLE);
//                notifyItemChanged(position);
                MediaLoadHelper.lockVideo(bean);
            });
            bindLockView(holder, bean);
            //删除操作
            holder.v("deleteView").setOnClickListener(v -> {
//                notifyDataSetChanged();
                if (position < getAllDatas().size()) {
                    notifyItemRemoved(position);
                    MediaLoadHelper.deleteVideo(bean);
                    getAllDatas().remove(position);
                    isFirstLoad = true;
                    notifyItemRangeChanged(position, getItemCount());

                    calcSdProgress();
                }
            });
            //播放操作c
            holder.v("videoPlayView").setOnClickListener(v -> {
                onVideoPlay(bean.getAbsolutePath(), position);
            });
            //位置
            holder.tV(R.id.positionTextView).setText((position + 1) + "");
            holder.v("roundLayout").setOnClickListener(v -> {
                onVideoPlay(bean.getAbsolutePath(), position);
            });
            //视频缩略图
            ImageView videoThumbnailView = holder.imgV(R.id.videoThumbnailView);
            videoThumbnailView.setImageBitmap(null);

            if (bean.isLockFlag()) {
                holder.v("deleteView").setVisibility(View.GONE);
            } else {
                holder.v("deleteView").setVisibility(View.VISIBLE);
            }

            if (isFirstLoad) {
                bingVideoThumbnailView(bean, videoThumbnailView);
            }
            lastItem = position;
        }

        private void bingVideoThumbnailView(VideoEntity bean, ImageView videoThumbnailView) {
            finalBitmap.display(videoThumbnailView, bean.getThumbnailAbsolutePath());
        }

        private void bindLockView(RBaseViewHolder holder, VideoEntity bean) {
            if (bean.isLockFlag()) {
                holder.imgV(R.id.lockImageView).setImageResource(R.drawable.video_list_item_lock);
            } else {
                holder.imgV(R.id.lockImageView).setImageResource(R.drawable.video_list_item_unlock);
            }
        }

        @Override
        public void resetData(List<VideoEntity> datas) {
            isFirstLoad = true;
            super.resetData(datas);
        }

        /*上传控件控制方法*/
        private void bindUploadBar(RBaseViewHolder holder, final VideoEntity bean) {
            DuduUploadBarLayout duduUpload = (DuduUploadBarLayout) holder.v("duduUploadBar");
            duduUpload.addFrame(list);
            duduUpload.setOnUploadChangeListener(new DuduUploadBarLayout.OnUploadChangeListener() {
                @Override
                public void onStateChange(View view, int oldState, int newState) {

                }

                @Override
                public void onUploadClick(View view, View uploadView) {
                    //上传
                    bean.setUploadState(DuduUploadBarLayout.STATE_UPING);
                    uploadVideo(bean);
                }

                @Override
                public void onCancelClick(View view, View cancelView) {
                    //取消上传
                    bean.setUploadState(DuduUploadBarLayout.STATE_NORMAL);
                    cancelVideo(bean);
                }
            });

            if (bean.getUploadState() == DuduUploadBarLayout.STATE_UPING) {
                duduUpload.setUpState(DuduUploadBarLayout.STATE_NORMAL);
            } else if (bean.getUploadState() == DuduUploadBarLayout.STATE_UPING) {
                duduUpload.setUpState(DuduUploadBarLayout.STATE_UPING);
            } else if (bean.getUploadState() == DuduUploadBarLayout.STATE_FINISH) {
                duduUpload.setUpState(DuduUploadBarLayout.STATE_FINISH);
            } else if (bean.getUploadState() == DuduUploadBarLayout.STATE_FAILD) {
                duduUpload.setUpState(DuduUploadBarLayout.STATE_FAILD);
            }
        }

        private void cancelVideo(final VideoEntity bean) {
            MediaLoadHelper.uploadVideo(mContext, bean, true, null);
        }

        private void uploadVideo(final VideoEntity bean) {
            MediaLoadHelper.uploadVideo(mContext, bean, false, new FileUploadHelper.IUploadFileListener() {
                public void start(FileUploadHelper.BaseTask task) {
                    MediaLoadHelper.updateVideoState(bean.getTimeStamp(), DuduUploadBarLayout.STATE_UPING);
                }

                @Override
                public void success(FileUploadHelper.BaseTask task) {
                    MediaLoadHelper.updateVideoState(bean.getTimeStamp(), DuduUploadBarLayout.STATE_FINISH);
                }

                @Override
                public void fail(FileUploadHelper.BaseTask task) {
                    MediaLoadHelper.updateVideoState(bean.getTimeStamp(), DuduUploadBarLayout.STATE_FAILD);
                }
            });
        }
    }
}
