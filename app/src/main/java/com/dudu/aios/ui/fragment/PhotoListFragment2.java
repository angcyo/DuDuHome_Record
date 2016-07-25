package com.dudu.aios.ui.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.dudu.aios.ui.base.T;
import com.dudu.aios.ui.fragment.base.RBaseFragment;
import com.dudu.aios.ui.fragment.base.RBaseViewHolder;
import com.dudu.aios.ui.fragment.video.FileUploadHelper;
import com.dudu.aios.ui.fragment.video.MediaLoadHelper;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.aios.ui.view.DuduUploadBarLayout;
import com.dudu.aios.ui.view.RBaseAdapter;
import com.dudu.aios.ui.view.TouchImageView;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.LogUtils;
import com.dudu.commonlib.resource.sdcard.ISdcardListener;
import com.dudu.commonlib.resource.sdcard.SdcardManager;
import com.dudu.commonlib.utils.afinal.FinalBitmap;
import com.dudu.drivevideo.utils.FileUtil;
import com.dudu.persistence.realmmodel.picture.PictureEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by robi on 2016-03-15 12:02.
 */
public class PhotoListFragment2 extends RBaseFragment implements FileUploadHelper.IUploadFileListener, ISdcardListener {
    public static final String TAG = "PhotoListFragment2";
    List<Drawable> list = new ArrayList<>();
    private boolean isSelectMode = false;//进入选择模式
    private List<PictureEntity> photoItemBeanList;
    private PhotosAdapter photosAdapter;
    private FinalBitmap finalBitmap;
    private DuduUploadBarLayout uploadBarLayout;
    private ViewSwitcher viewSwitcher;
    private int[] lastLocation;
    private PhotoListComparator mListComparator;
    private Logger log = LoggerFactory.getLogger("photo");

    @Override
    protected int getContentView() {
        return R.layout.fragment_photo_list2;
    }

    @Override
    protected void initViewData() {
        mListComparator = new PhotoListComparator();

        Resources resources = getResources();
        list.add(resources.getDrawable(R.drawable.upload_1));
        list.add(resources.getDrawable(R.drawable.upload_2));
        list.add(resources.getDrawable(R.drawable.upload_3));
        list.add(resources.getDrawable(R.drawable.upload_4));

        finalBitmap = new FinalBitmap(mBaseActivity);
        photosAdapter = new PhotosAdapter(mBaseActivity, photoItemBeanList);

        uploadBarLayout = (DuduUploadBarLayout) mViewHolder.v(R.id.duduUploadBar);
        uploadBarLayout.addFrame(list);
        uploadBarLayout.setOnUploadChangeListener(new DuduUploadBarLayout.OnUploadChangeListener() {
            @Override
            public void onStateChange(View view, int oldState, int newState) {

            }

            @Override
            public void onUploadClick(View view, View uploadView) {
                PictureEntity bean = (PictureEntity) view.getTag();
                if (bean != null) {
                    uploadPhoto(bean);
                }
            }

            @Override
            public void onCancelClick(View view, View cancelView) {
                PictureEntity bean = (PictureEntity) view.getTag();
                if (bean != null) {
                    bean.setUploadState(PictureEntity.UPLOAD_NORMAL_STATE);
                    refreshUploadBarLayout(bean);
                    cancelPhoto(bean);
                }
            }
        });

        viewSwitcher = (ViewSwitcher) mViewHolder.v("viewSwitcher");

        mViewHolder.reV("gridView").setAdapter(photosAdapter);
        photosAdapter.setLayoutManager(mViewHolder.reV("gridView").getLayoutManager());
        mViewHolder.v(R.id.selectView).setOnClickListener(v -> {
            setSelectMode(!isSelectMode);
        });
        /*图片大图界面的返回按钮*/
        mViewHolder.v("photo_button_back").setOnClickListener(v -> {
                    showPrevious();
                }
        );
        /*图片列表界面的返回按钮*/
        mViewHolder.v("button_back").setOnClickListener(v -> {
                    setSelectMode(false);
                    replaceFragment(FragmentConstants.FRAGMENT_DRIVING_RECORD);
                }
        );

        /*图片列表,删除按钮*/
        mViewHolder.v(R.id.deleteView).setOnClickListener(v -> {
            photosAdapter.deleteSelect();
        });
        /*图片列表,上传图片*/
        mViewHolder.v(R.id.uploadView).setOnClickListener(v -> {
            photosAdapter.uploadSelect();
        });

        /*大图界面,上传图片*/
        mViewHolder.v(R.id.photoUpload).setOnClickListener(v -> {
            PictureEntity bean = (PictureEntity) uploadBarLayout.getTag();
            bean.setUploadState(PictureEntity.UPLOADING_STATE);
            photosAdapter.notifyItemChanged(bean.itemPosition);
            refreshUploadBarLayout(bean);

            uploadPhoto(bean);
        });

        /*大图界面,删除图片*/
        mViewHolder.v(R.id.photoDelete).setOnClickListener(v -> {
            viewSwitcher.showPrevious();
            PictureEntity bean = (PictureEntity) uploadBarLayout.getTag();
            deletePhoto(bean);
//            photosAdapter.removeItem(bean.itemPosition);
//            if (photosAdapter.getAllDatas().size() == 0) {
//                showEmptyLayout();
//            }
            onShow();
        });

        /*大图界面,上一个图片*/
        mViewHolder.v(R.id.button_last).setOnClickListener(v -> {
            PictureEntity bean = (PictureEntity) uploadBarLayout.getTag();
            showImageWidthPosition(mViewHolder.v("touchImageView"), bean.itemPosition - 1);

        });
        /*大图界面,下一个图片*/
        mViewHolder.v(R.id.button_next).setOnClickListener(v -> {
            PictureEntity bean = (PictureEntity) uploadBarLayout.getTag();
            showImageWidthPosition(mViewHolder.v("touchImageView"), bean.itemPosition + 1);
        });

        hideRightControlLayout();
        onShow();
    }

    private void showPrevious() {
        if (lastLocation == null) {
            return;
        }

        View touchImageView = mViewHolder.v("touchImageView");
        Animation animation = makeTouchImageViewExitAnim(lastLocation);
        touchImageView.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mViewHolder.v(R.id.photoDelete).setVisibility(View.GONE);
                mViewHolder.v(R.id.photoUpload).setVisibility(View.GONE);
                mViewHolder.v(R.id.photo_button_back).setVisibility(View.GONE);
                uploadBarLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                viewSwitcher.showPrevious();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void showImageWidthPosition(View v, int position) {
        PictureEntity showBean = photoItemBeanList.get(position);
        int[] rect = getAnimInts(v);
        showImage(showBean, false, rect);
    }

    private void setSelectMode(boolean selectMode) {
        isSelectMode = selectMode;
        if (isSelectMode) {
            showRightControlLayout();
            mViewHolder.tV(R.id.selectView).setText(getResources().getString(R.string.text_cancel));
        } else {
            hideRightControlLayout();
            mViewHolder.tV(R.id.selectView).setText(getResources().getString(R.string.text_select));
        }
        photosAdapter.setSelectMode(isSelectMode);
    }

    private void refreshGridView() {
        if (photoItemBeanList == null || photoItemBeanList.size() == 0) {
            showEmptyLayout();
        } else {
            hideEmptyLayout();
        }
        if (photosAdapter != null) {
            photosAdapter.resetData(photoItemBeanList);
        }
    }

    private void showEmptyLayout() {
        if (mViewHolder == null) {
            return;
        }

        mViewHolder.v(R.id.photo_empty_container).setVisibility(View.VISIBLE);
        mViewHolder.v(R.id.selectView).setVisibility(View.GONE);
        hideRightControlLayout();

        TextView emptyTip = (TextView) mViewHolder.v(R.id.photo_empty_container).findViewById(R.id.emptyTip);
        TextView emptyTipEn = (TextView) mViewHolder.v(R.id.photo_empty_container).findViewById(R.id.emptyTipEn);
        if (FileUtil.isTFlashCardExists()) {
            emptyTip.setText(R.string.photo_empty_chinese);
            emptyTipEn.setText(R.string.photo_empty_english);
        } else {
            emptyTip.setText(R.string.photo_empty_chinese_ntf);
            emptyTipEn.setText(R.string.photo_empty_english_ntf);
        }
    }

    private void hideEmptyLayout() {
        mViewHolder.v(R.id.photo_empty_container).setVisibility(View.GONE);
        mViewHolder.v(R.id.selectView).setVisibility(View.VISIBLE);
//        showRightControlLayout();
    }

    private void showRightControlLayout() {
        mViewHolder.v(R.id.deleteView).setVisibility(View.VISIBLE);
//        mViewHolder.v(R.id.uploadView).setVisibility(View.VISIBLE);
    }

    private void hideRightControlLayout() {
        mViewHolder.v(R.id.deleteView).setVisibility(View.GONE);
        mViewHolder.v(R.id.uploadView).setVisibility(View.GONE);
    }

    private void showImage(PictureEntity bean, boolean showNext, int[] rect) {
        File file = new File(bean.getAbsolutePath());
        if (!file.exists()) {
            T.show(mBaseActivity, "图片不存在,无法查看.");
            return;
        }

        TouchImageView touchImageView = (TouchImageView) mViewHolder.v("touchImageView");
        touchImageView.setZoom(1);
//        finalBitmap.display(touchImageView, bean.getAbsolutePath());
        touchImageView.setImageURI(Uri.fromFile(file));
        if (showNext) {
            viewSwitcher.showNext();
        }
        uploadBarLayout.setTag(bean);
        refreshUploadBarLayout(bean);

        lastLocation = rect;

        mViewHolder.v(R.id.photoDelete).setVisibility(View.GONE);
        mViewHolder.v(R.id.photoUpload).setVisibility(View.GONE);
        mViewHolder.v(R.id.button_next).setVisibility(View.GONE);
        mViewHolder.v(R.id.button_last).setVisibility(View.GONE);
        uploadBarLayout.setVisibility(View.GONE);
        mViewHolder.v(R.id.photo_button_back).setVisibility(View.GONE);
        Animation animation = makeTouchImageViewEnterAnim(lastLocation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewHolder.v(R.id.photoDelete).setVisibility(View.VISIBLE);
//                mViewHolder.v(R.id.photoUpload).setVisibility(View.VISIBLE);
                mViewHolder.v(R.id.photo_button_back).setVisibility(View.VISIBLE);
                PictureEntity entity = (PictureEntity) uploadBarLayout.getTag();
                refreshUploadBarLayout(entity);

                if (entity.itemPosition > 0) {
                    mViewHolder.v(R.id.button_last).setVisibility(View.VISIBLE);
                }
                if ((entity.itemPosition + 1) < photoItemBeanList.size()) {
                    mViewHolder.v(R.id.button_next).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        touchImageView.startAnimation(animation);
    }

    private Animation makeTouchImageViewEnterAnim(int[] rect) {
        float fromX, fromY;
        fromX = (float) rect[2] / viewSwitcher.getMeasuredWidth();
        fromY = (float) rect[3] / viewSwitcher.getMeasuredHeight();
        float pivotX, pivotY;
        pivotX = rect[0];// + rect[2] / 2;
        pivotY = rect[1];// + rect[3] / 2;

        AnimationSet animationSet = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.2f, 1f);
        ScaleAnimation scaleAnimation = new ScaleAnimation(fromX, 1f, fromY, 1f, pivotX, pivotY);

        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(scaleAnimation);

        animationSet.setDuration(300);
        animationSet.setInterpolator(new AccelerateInterpolator());
        animationSet.setFillAfter(true);
        return animationSet;
    }

    private Animation makeTouchImageViewExitAnim(int[] rect) {
        float fromX, fromY;
        fromX = (float) rect[2] / viewSwitcher.getMeasuredWidth();
        fromY = (float) rect[3] / viewSwitcher.getMeasuredHeight();
        float pivotX, pivotY;
        pivotX = rect[0];// + rect[2] / 2;
        pivotY = rect[1];// + rect[3] / 2;

        AnimationSet animationSet = new AnimationSet(true);

        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, fromX, 1f, fromY, pivotX, pivotY);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0.2f);

        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(scaleAnimation);

        animationSet.setDuration(300);
        animationSet.setInterpolator(new AccelerateInterpolator());
        animationSet.setFillAfter(true);
        return animationSet;
    }

    private void refreshUploadBarLayout(PictureEntity bean) {
        if (bean.getUploadState() == PictureEntity.UPLOAD_NORMAL_STATE) {
            uploadBarLayout.setVisibility(View.GONE);
        } else {
            if (bean.getUploadState() == PictureEntity.UPLOADING_STATE) {
                uploadBarLayout.setUpState(DuduUploadBarLayout.STATE_UPING);
            } else if (bean.getUploadState() == PictureEntity.UPLOAD_SUCCESS_STATE) {
                uploadBarLayout.setUpState(DuduUploadBarLayout.STATE_FINISH);
            } else if (bean.getUploadState() == PictureEntity.UPLOAD_FAIL_STATE) {
                uploadBarLayout.setUpState(DuduUploadBarLayout.STATE_FAILD);
            }
//            uploadBarLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAdd() {
        onShow();
    }

    @Override
    public void onHide() {
        SdcardManager.instance().removeListener(this);
    }

    @Override
    public void onShow() {
        super.onShow();
        SdcardManager.instance().addListener(this);
        MediaLoadHelper.getAllPhotos(beans -> {
            photoItemBeanList = beans;
            MediaLoadHelper.getAllPhotos(beans2 -> {
                if (photoItemBeanList == null) {
                    photoItemBeanList = beans2;
                } else {
                    photoItemBeanList.addAll(beans2);
                }
                Collections.sort(photoItemBeanList, mListComparator);
                for (int i = 0; i < photoItemBeanList.size(); i++) {
                    photoItemBeanList.get(i).itemPosition = i;
                }

                int size = photoItemBeanList.size();
                filterList(photoItemBeanList);

                int size2 = photoItemBeanList.size();
                log.info("获取到图片数量:{} 过滤不存在的图片数量:{}", size2, size - size2);
                checkPhotoUploadState();
                refreshGridView();
            }, false);
        }, true);
    }

    private void filterList(List<PictureEntity> list) {
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

    private void checkPhotoUploadState() {
        for (PictureEntity entity : photoItemBeanList) {
            if (entity.getUploadState() == PictureEntity.UPLOADING_STATE) {
                uploadPhoto(entity);
            }
        }
    }

    public void deletePhoto(PictureEntity bean) {
        log.info("删除图片:{}", bean.getAbsolutePath());
        MediaLoadHelper.deletePhoto(bean);
    }

    private void cancelPhoto(final PictureEntity bean) {
        MediaLoadHelper.uploadPhoto(mBaseActivity, bean, true, null);
    }

    private void uploadPhoto(final PictureEntity bean) {
        MediaLoadHelper.uploadPhoto(mBaseActivity, bean, false, this);
    }

    private void refreshPhotosList() {
        mBaseActivity.runOnUiThread(() -> onShow());
    }

    @Override
    public void start(FileUploadHelper.BaseTask task) {
        LogUtils.i(TAG, "开始上传:" + task.path);
        PictureEntity bean = getEntity(task.timeStamp);
        if (bean == null) {
            return;
        }
        MediaLoadHelper.updatePhotoState(bean.getTimeStamp(), DuduUploadBarLayout.STATE_UPING);
//                photosAdapter.notifyDataSetChanged();
        refreshPhotosList();
    }

    @Override
    public void success(FileUploadHelper.BaseTask task) {
        LogUtils.i(TAG, "上传成功:" + task.path);
        PictureEntity bean = getEntity(task.timeStamp);
        if (bean == null) {
            return;
        }
        MediaLoadHelper.updatePhotoState(bean.getTimeStamp(), DuduUploadBarLayout.STATE_FINISH);
//                photosAdapter.notifyDataSetChanged();
        refreshPhotosList();
    }

    @Override
    public void fail(FileUploadHelper.BaseTask task) {
        LogUtils.i(TAG, "上传失败:" + task.path);
        PictureEntity bean = getEntity(task.timeStamp);
        if (bean == null) {
            return;
        }
        MediaLoadHelper.updatePhotoState(bean.getTimeStamp(), DuduUploadBarLayout.STATE_FAILD);
//                photosAdapter.notifyDataSetChanged();
        refreshUploadBarLayout(bean);
        refreshPhotosList();
    }

    private PictureEntity getEntity(long timeStamp) {
        for (PictureEntity entity : photoItemBeanList) {
            if (entity.getTimeStamp() == timeStamp) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public void onMounted() {
        onShow();
    }

    @Override
    public void onRemoved() {
        showPrevious();
        onShow();
    }

    private int[] getAnimInts(View v) {
        int[] locat = new int[2];
        int[] rect = new int[4];
        v.getLocationInWindow(locat);
        System.arraycopy(locat, 0, rect, 0, 2);
        rect[2] = v.getMeasuredWidth();
        rect[3] = v.getMeasuredHeight();
        return rect;
    }

    class PhotoListComparator implements Comparator<PictureEntity> {

        @Override
        public int compare(PictureEntity lhs, PictureEntity rhs) {
            return (int) (rhs.getTimeStamp() - lhs.getTimeStamp());
        }
    }

    class PhotosAdapter extends RBaseAdapter<PictureEntity> {
        int scrollState = RecyclerView.SCROLL_STATE_IDLE;
        int firstItem = 0;
        int lastItem = 0;
        private boolean isSelectMode = false;
        private List<Integer> selectList;//选中的item
        private boolean isFirstLoad;
        private GridLayoutManager layoutManager;

        public PhotosAdapter(Context context, List<PictureEntity> datas) {
            super(context, datas);
            selectList = new ArrayList<>();
            setFirstLoad(true);
        }


        public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
            this.layoutManager = (GridLayoutManager) layoutManager;
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            scrollState = newState;
            setFirstLoad(false);
            int first = layoutManager.findFirstVisibleItemPosition();
            int last = layoutManager.findLastVisibleItemPosition();
            if (scrollState == RecyclerView.SCROLL_STATE_IDLE && (firstItem != first || lastItem != last)) {
                firstItem = first;
                lastItem = last;
                setFirstLoad(true);
//                notifyItemRangeChanged(firstItem, layoutManager.getChildCount());
//                loadVideoThumbnail();
                for (int i = first; i <= last; i++) {
                    View view = layoutManager.findViewByPosition(i);
                    if (view != null) {
                        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
                        if (imageView != null) {
//                            Drawable drawable = imageView.getDrawable();
//                            if (drawable == null) {
//                                notifyItemChanged(i);
//                            } else if (drawable instanceof BitmapDrawable) {
//                                if (((BitmapDrawable) drawable).getBitmap() == null) {
//                                    notifyItemChanged(i);
//                                }
//                            }
                            if (imageView.getTag() != null) {
                                notifyItemChanged(i);
                            }
                        }
                    }
                }
            }
        }

        public void setFirstLoad(boolean firstLoad) {
            isFirstLoad = firstLoad;
        }

        public void setSelectMode(boolean selectMode) {
            this.isSelectMode = selectMode;
            if (!isSelectMode) {
                cancelSelect();
            }
            setFirstLoad(true);
            notifyDataSetChanged();
        }

        @Override
        protected int getItemLayoutId(int viewType) {
            return R.layout.fragment_photo_list2_item_layout;
        }

        public void removeItem(int position) {
            if (mAllDatas != null && mAllDatas.size() > position) {
                mAllDatas.remove(position);
                notifyItemRemoved(position);
            }
        }

        public void deleteSelect() {
            if (selectList.size() > 0) {
                List<PictureEntity> pictureEntities = new ArrayList<>();
                for (Integer position : selectList) {
                    pictureEntities.add(mAllDatas.get(position));
                }
                MediaLoadHelper.deletePhoto(pictureEntities, onNext -> {
                    cancelSelect();
//                    notifyDataSetChanged();
                    onShow();
                });
            }
        }

        public void uploadSelect() {
            if (selectList.size() > 0) {
                List<PictureEntity> pictureEntities = new ArrayList<>();
                for (Integer position : selectList) {
                    PictureEntity entity = mAllDatas.get(position);
                    pictureEntities.add(entity);
                    entity.setUploadState(PictureEntity.UPLOADING_STATE);
                    uploadPhoto(entity);
                }
                cancelSelect();
                notifyDataSetChanged();
            }
        }

        @Override
        public void resetData(List<PictureEntity> datas) {
            isFirstLoad = true;
            super.resetData(datas);
        }

        public void cancelSelect() {
            selectList = new ArrayList<>();
        }

        public void cancelUpload(int position) {
            cancelPhoto(mAllDatas.get(position));
            mAllDatas.get(position).setUploadState(PictureEntity.UPLOAD_NORMAL_STATE);
            notifyItemChanged(position);
        }

        @Override
        protected void onBindView(RBaseViewHolder holder, int position, PictureEntity bean) {
            holder.v(R.id.selectImageView).setVisibility(View.GONE);
            if (this.isSelectMode) {
                //选择模式
                holder.v(R.id.itemLayout).setOnClickListener(v -> {
                    if (selectList.contains(position)) {
                        selectList.remove(Integer.valueOf(position));
                    } else {
                        selectList.add(position);
                    }
                    isFirstLoad = true;
                    notifyItemChanged(position);
                });
                if (selectList.contains(position)) {
                    holder.v(R.id.selectImageView).setVisibility(View.VISIBLE);
                }
            } else {
                //非选择模式
                holder.v(R.id.itemLayout).setOnClickListener(v -> {
                    int[] rect = getAnimInts(v);
                    showImage(bean, true, rect);
                });

            }
//            DuduImageLoader.du().display(holder.imageView(R.id.imageView), bean.photoPath);
            bean.itemPosition = position;
            ImageView imageView = holder.imageView(R.id.imageView);
            imageView.setImageResource(R.drawable.default_png);
            imageView.setTag("load");
            if (isFirstLoad) {
                finalBitmap.display(imageView, bean.getAbsolutePath(), 300, 150);//imageView.getMeasuredWidth() / 2, imageView.getMeasuredHeight() / 2
                imageView.setTag(null);
            }
            bindUploadBarLayout(holder, position, bean);
            lastItem = position;
        }

        private void bindUploadBarLayout(RBaseViewHolder holder, int position, PictureEntity bean) {
            DuduUploadBarLayout uploadBarLayout = (DuduUploadBarLayout) holder.v(R.id.duduUploadBar);
            uploadBarLayout.addFrame(list);
            uploadBarLayout.setFaile(false);
            if (bean.getUploadState() == PictureEntity.UPLOAD_NORMAL_STATE) {
                uploadBarLayout.setVisibility(View.GONE);
            } else {
                if (bean.getUploadState() == PictureEntity.UPLOADING_STATE) {
                    uploadBarLayout.setUpState(DuduUploadBarLayout.STATE_UPING);
                } else if (bean.getUploadState() == PictureEntity.UPLOAD_SUCCESS_STATE) {
                    uploadBarLayout.setUpState(DuduUploadBarLayout.STATE_FINISH);
                } else if (bean.getUploadState() == PictureEntity.UPLOAD_FAIL_STATE) {
                    uploadBarLayout.setUpState(DuduUploadBarLayout.STATE_FAILD);
                }
//                uploadBarLayout.setVisibility(View.VISIBLE);
            }
            uploadBarLayout.setOnUploadChangeListener(new DuduUploadBarLayout.OnUploadChangeListener() {
                @Override
                public void onStateChange(View view, int oldState, int newState) {

                }

                @Override
                public void onUploadClick(View view, View uploadView) {
                    uploadPhoto(bean);
                }

                @Override
                public void onCancelClick(View view, View cancelView) {
                    cancelUpload(position);
                }
            });
        }
    }
}
