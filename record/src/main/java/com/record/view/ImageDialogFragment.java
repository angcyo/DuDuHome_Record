package com.record.view;

import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;

import com.record.R;
import com.record.base.RBaseDialogFragment;
import com.record.mode.ImagesDialogMode;

import java.util.List;

/**
 * Created by robi on 2016-07-01 21:57.
 */
public class ImageDialogFragment extends RBaseDialogFragment {

    ImagesDialogMode mImagesDialogMode = new ImagesDialogMode();
    List<String> imageList;
    int position;

    @Override
    protected int getContentView() {
        return R.layout.fragment_images;
    }

    @Override
    protected int getGravity() {
        return Gravity.CENTER;
    }


    @Override
    protected int getWindowWidth() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mImagesDialogMode.initView(this, mViewHolder);
        mImagesDialogMode.setImageList(imageList, position);
    }

    public void setImageList(List<String> list, int position) {
        this.imageList = list;
        this.position = position;
    }

    @Override
    protected int getWindowHeight() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }
}

