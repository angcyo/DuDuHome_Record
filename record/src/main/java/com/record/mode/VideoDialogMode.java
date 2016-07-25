package com.record.mode;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ToggleButton;

import com.dudu.persistence.realmmodel.video.VideoEntity;
import com.record.R;
import com.record.base.RBaseAdapter;
import com.record.base.RBaseViewHolder;
import com.record.base.T;
import com.record.control.IVideoListener;
import com.record.control.IVideos;
import com.record.control.VideoImpl;
import com.record.view.DeleteConfirmDialogFragment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Created by robi on 2016-06-03 21:04.
 */
public class VideoDialogMode implements IVideoListener {

    IVideos mIVideos;
    private RBaseViewHolder mViewHolder;
    private FragmentActivity mActivity;
    private DialogFragment mDialogFragment;
    private List<VideoEntity> mVideoEntities;
    private RecyclerView mRecyclerView;
    private VideoAdapter mVideoAdapter;

    private DeleteConfirmDialogFragment mDeleteConfirmDialogFragment;

    public VideoDialogMode(Fragment fragment, RBaseViewHolder viewHolder) {
        mViewHolder = viewHolder;
        mActivity = fragment.getActivity();
        mDialogFragment = (DialogFragment) fragment;
        mIVideos = new VideoImpl();
        mDeleteConfirmDialogFragment = new DeleteConfirmDialogFragment();
    }

    public void initView() {
        initRecyclerView();
        initCloseView();
    }

    private void initCloseView() {
        mViewHolder.v(R.id.closeView).setOnClickListener(v -> mDialogFragment.dismiss());
    }

    private void initRecyclerView() {
        mRecyclerView = mViewHolder.reV(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, 2, LinearLayoutManager.HORIZONTAL, false));

        mVideoEntities = mIVideos.getVideos(this);
        mVideoAdapter = new VideoAdapter(mActivity, mVideoEntities);
        mRecyclerView.setAdapter(mVideoAdapter);
    }


    private void onQuit() {
    }

    public void onDismiss() {
        onQuit();
    }

    @Override
    public void onVideos(List<VideoEntity> videos) {
        mVideoAdapter.resetData(videos);
        if (videos.size() == 0) {
            T.show(mActivity, "暂无视频数据");
        }
    }

    class VideoAdapter extends RBaseAdapter<VideoEntity> {

        public VideoAdapter(Context context, List<VideoEntity> datas) {
            super(context, datas);
        }

        @Override
        protected int getItemLayoutId(int viewType) {
            return R.layout.adapter_photo_video_item;
        }

        @Override
        protected void onBindView(RBaseViewHolder holder, int position, VideoEntity bean) {
            Picasso.with(mActivity).load(new File(bean.getThumbnailAbsolutePath())).resize(100, 100).transform(new CircleTransform()).centerCrop().into(holder.imageView(R.id.imageView));
            holder.tV(R.id.nameView).setText(bean.getFileName());

            holder.v(R.id.deleteView).setOnClickListener(v -> {
                if (bean.isLockFlag()) {
                    T.show(mContext, "请先解锁.");
                    return;
                }

                mDeleteConfirmDialogFragment.show(mActivity.getSupportFragmentManager(), "mDeleteConfirmDialogFragment");
                mDeleteConfirmDialogFragment.setDialogContent("确认删除视频?");
                mDeleteConfirmDialogFragment.setDialogTitle("警告");
                mDeleteConfirmDialogFragment.setDialogListener(() -> {
                    mIVideos.deleteVideo(bean);
                    notifyItemRemoved(mAllDatas.indexOf(bean));
                    mAllDatas.remove(bean);
                });
            });

            ((ToggleButton) holder.v(R.id.lockView)).setChecked(bean.isLockFlag());
            ((ToggleButton) holder.v(R.id.lockView)).setOnCheckedChangeListener((buttonView, isChecked) -> {
                mIVideos.lockVideo(bean);
                bean.setLockFlag(isChecked);
            });

            holder.v(R.id.playView).setOnClickListener(v -> {
                //打开视频
                // TODO: 16-06-06-006
                try {
//                    Utils.openFile(mContext, new File(bean.getAbsolutePath()));
//                    FileUtilOs.openFile(mContext, new File(bean.getAbsolutePath()));
//                    mDialogFragment.dismiss();
                    Intent intent = new Intent();
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setClassName("com.android.gallery3d", "com.android.gallery3d.app.MovieActivity");
//                    intent.addCategory(Intent.CATEGORY_DEFAULT);
//                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setDataAndType(Uri.fromFile(new File(bean.getAbsolutePath())), "video/mp4");
                    mContext.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    T.show(mContext, "没有安装可以打开文件的应用");
                }
            });
        }
    }
}
