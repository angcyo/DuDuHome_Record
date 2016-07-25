package com.record.mode;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ToggleButton;

import com.dudu.persistence.realmmodel.picture.PictureEntity;
import com.record.R;
import com.record.base.RBaseAdapter;
import com.record.base.RBaseViewHolder;
import com.record.base.T;
import com.record.control.IPhotos;
import com.record.control.IPhotosListener;
import com.record.control.PhotosImpl;
import com.record.util.L;
import com.record.view.DeleteConfirmDialogFragment;
import com.record.view.ImageDialogFragment;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by robi on 2016-06-03 21:04.
 */
public class PhotoDialogMode implements IPhotosListener {

    IPhotos mIPhotos;
    private RBaseViewHolder mViewHolder;
    private FragmentActivity mActivity;

    private List<PictureEntity> mPictureEntities;
    private RecyclerView mRecyclerView;
    private PhotoAdapter mPhotoAdapter;
    private DialogFragment mDialogFragment;
    private DeleteConfirmDialogFragment mDeleteConfirmDialogFragment;

    private ImageDialogFragment mImageDialogFragment;
    private List<String> mImageList;

    public PhotoDialogMode(Fragment fragment, RBaseViewHolder viewHolder) {
        mViewHolder = viewHolder;
        mActivity = fragment.getActivity();
        mDialogFragment = (DialogFragment) fragment;
        mIPhotos = new PhotosImpl();
        mDeleteConfirmDialogFragment = new DeleteConfirmDialogFragment();
        mImageDialogFragment = new ImageDialogFragment();
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

        mPictureEntities = mIPhotos.getPhotos(this);
        initImageList();
        mPhotoAdapter = new PhotoAdapter(mActivity, mPictureEntities);
        mRecyclerView.setAdapter(mPhotoAdapter);
    }

    private void refreshRecycler() {

    }


    public void onDismiss() {
    }

    private void initImageList() {
        mImageList = new ArrayList<>();
        for (PictureEntity entity : mPictureEntities) {
            mImageList.add(entity.getAbsolutePath());
        }
    }

    @Override
    public void onPhotos(List<PictureEntity> photos) {
        mPictureEntities = photos;
        initImageList();
        mPhotoAdapter.resetData(photos);

        if (photos.size() == 0) {
            T.show(mActivity, "暂无图片数据");
        }
    }

    class PhotoAdapter extends RBaseAdapter<PictureEntity> {

        public PhotoAdapter(Context context, List<PictureEntity> datas) {
            super(context, datas);
        }

        @Override
        protected int getItemLayoutId(int viewType) {
            return R.layout.adapter_photo_video_item;
        }

        @Override
        protected void onBindView(RBaseViewHolder holder, int position, PictureEntity bean) {
            Picasso.with(mActivity).load(new File(bean.getAbsolutePath())).resize(100, 100).transform(new CircleTransform()).centerCrop().into(holder.imageView(R.id.imageView));
            holder.v(R.id.playView).setVisibility(View.GONE);
            holder.tV(R.id.nameView).setText(bean.getFileName());

            holder.v(R.id.deleteView).setOnClickListener(v -> {
                if (bean.isLockFlag()) {
                    T.show(mContext, "请先解锁.");
                    return;
                }

                mDeleteConfirmDialogFragment.show(mActivity.getSupportFragmentManager(), "mDeleteConfirmDialogFragment");
                mDeleteConfirmDialogFragment.setDialogContent("确认删除照片?");
                mDeleteConfirmDialogFragment.setDialogTitle("警告");
                mDeleteConfirmDialogFragment.setDialogListener(() -> {
                    mIPhotos.deletePhoto(bean);
                    notifyItemRemoved(mAllDatas.indexOf(bean));
                    mAllDatas.remove(bean);
                });
            });

            ((ToggleButton) holder.v(R.id.lockView)).setChecked(bean.isLockFlag());
            ((ToggleButton) holder.v(R.id.lockView)).setOnCheckedChangeListener((buttonView, isChecked) -> {
                mIPhotos.lockPhoto(bean);
                bean.setLockFlag(isChecked);
            });

            holder.imageView(R.id.imageView).setOnClickListener(v -> {
                //打开图片
                // TODO: 16-06-06-006
//                try {
////                    Utils.openFile(mContext, new File(bean.getAbsolutePath()));
//                    FileUtilOs.openFile(mContext, new File(bean.getAbsolutePath()));
//                } catch (ActivityNotFoundException e) {
//                    T.show(mContext, "没有安装可以打开文件的应用");
//                }
                L.i("点击Item:" + position + " 总数:" + mImageList.size());
                mImageDialogFragment.setImageList(mImageList, position);
                if (!mImageDialogFragment.isVisible()) {
                    mImageDialogFragment.show(mActivity.getSupportFragmentManager(), "image dialog");
                }
            });
        }
    }
}
