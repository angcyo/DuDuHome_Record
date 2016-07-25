package com.record.mode;

import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.record.R;
import com.record.base.RBaseViewHolder;
import com.record.util.L;
import com.record.view.OutlineContainer;
import com.record.view.PinchImageView;
import com.record.view.PinchImageViewPager;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by robi on 2016-07-01 22:03.
 */
public class ImagesDialogMode extends IDialogViewMode {

    //    JazzyViewPager mJazzyViewPager;
//    ViewPager mViewPager;
    PinchImageViewPager mPinchImageViewPager;
    ImagePagerAdapter mImagePagerAdapter;
    List<String> imageList;

    public ImagesDialogMode() {
        imageList = new ArrayList<>();
    }

    @Override
    public void initView(DialogFragment dialogFragment, RBaseViewHolder viewHolder) {
        super.initView(dialogFragment, viewHolder);
        initViewPager();
        initCloseView();
    }

    private void initCloseView() {
        mViewHolder.v(R.id.closeView).setOnClickListener(v -> mDialogFragment.dismiss());
    }

    private void initViewPager() {
//        mJazzyViewPager = (JazzyViewPager) mViewHolder.v(R.id.jazzyViewPager);
//        mJazzyViewPager.setTransitionEffect(JazzyViewPager.TransitionEffect.Stack);
        mImagePagerAdapter = new ImagePagerAdapter();
//        mJazzyViewPager.setAdapter(mImagePagerAdapter);

        mPinchImageViewPager = (PinchImageViewPager) mViewHolder.v(R.id.pinchViewPager);
        mPinchImageViewPager.setAdapter(mImagePagerAdapter);
        mPinchImageViewPager.setPageTransformer(true, new DepthPageTransformer());
    }

    public void setImageList(List<String> imageList, int position) {
        this.imageList = imageList;
        mImagePagerAdapter.notifyDataSetChanged();

        L.i("设置位置:" + position);
//        mViewHolder.postDelay(() -> mJazzyViewPager.setCurrentItem(position, false), 20);
        mViewHolder.postDelay(() -> mPinchImageViewPager.setCurrentItem(position, false), 10);
    }

    /**
     * 图片适配器
     */
    private class ImagePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return imageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            if (view instanceof OutlineContainer) {
                return ((OutlineContainer) view).getChildAt(0) == object;
            } else {
                return view == object;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            L.i("加载位置:" + position);

            PinchImageView imageView = new PinchImageView(mFragmentActivity);
//            imageView.addOuterMatrixChangedListener(new PinchImageView.OuterMatrixChangedListener() {
//                @Override
//                public void onOuterMatrixChanged(PinchImageView pinchImageView) {
////                    pinchImageView.getPinchMode()t
//                    L.i("加载位置");
//                }
//            });

//            TouchImageView imageView = new TouchImageView(mFragmentActivity);
//            imageView.setImageResource(R.drawable.photo);
            Picasso.with(mFragmentActivity).load(new File(imageList.get(position))).into(imageView);
            container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            mJazzyViewPager.setObjectForPosition(imageView, position);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//            container.removeView(mJazzyViewPager.findViewFromObject(position));
            container.removeView((View) object);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (object != null) {
                PinchImageView imageView = (PinchImageView) object;
                imageView.setAlpha(1f);
                mPinchImageViewPager.setMainPinchImageView(imageView);
            }
        }
    }

    public class DepthPageTransformer implements PinchImageViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);
            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);
            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);
                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);
                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }

            L.e("position:" + position);
        }
    }

}
