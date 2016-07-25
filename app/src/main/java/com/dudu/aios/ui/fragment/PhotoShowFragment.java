package com.dudu.aios.ui.fragment;

import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dudu.aios.ui.fragment.base.RBaseFragment;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.R;

/**
 * Created by robi on 2016-03-15 11:46.
 */
public class PhotoShowFragment extends RBaseFragment implements View.OnClickListener {

    private ImageButton btnBack, btnDelete, btnUpload, btnCancelUpload;

    private LinearLayout uploadingContainer, uploadSuccessContainer;

    private ImageView uploadingIcon, imageShow;

    private AnimationDrawable animationDrawable;

    @Override
    protected int getContentView() {
        return R.layout.activity_photo_show;
    }

    @Override
    protected void initView(View rootView) {
        super.initView(rootView);
        btnBack = (ImageButton) mViewHolder.v(R.id.button_back);
        btnDelete = (ImageButton) mViewHolder.v(R.id.button_delete);
        btnUpload = (ImageButton) mViewHolder.v(R.id.button_upload);
        btnCancelUpload = (ImageButton) mViewHolder.v(R.id.button_cancel_upload);
        uploadingContainer = (LinearLayout) mViewHolder.v(R.id.uploading_container);
        uploadSuccessContainer = (LinearLayout) mViewHolder.v(R.id.upload_successful_container);
        uploadingIcon = (ImageView) mViewHolder.v(R.id.image_uploading);
        imageShow = (ImageView)mViewHolder.v(R.id.image_show);
    }

    @Override
    protected void initViewData() {
        btnBack.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnCancelUpload.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_back:
//                finish();
//                mBaseActivity.showMain();
                mBaseActivity.replaceFragment(FragmentConstants.FRAGMENT_PHOTO_LIST);
                break;
            case R.id.button_delete:
                actionDelete();
                break;
            case R.id.button_upload:
                actionUpload();
                break;
            case R.id.button_cancel_upload:
                actionCancelUpload();
                break;
        }
    }

    private void actionCancelUpload() {
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

    private void actionDelete() {

    }

    @Override
    public void onShow() {
        super.onShow();
        showImage();
    }

    private void showImage(){
        Uri uri = getArguments().getParcelable("uri");
        if (uri != null){
            imageShow.setImageURI(uri);
        }
    }
}
