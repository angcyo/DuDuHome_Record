package com.dudu.aios.ui.fragment.video;

import android.view.View;
import android.widget.ImageButton;

import com.dudu.aios.ui.base.ObservableFactory;
import com.dudu.aios.ui.base.T;
import com.dudu.aios.ui.fragment.base.RBaseFragment;
import com.dudu.aios.ui.utils.contants.FragmentConstants;
import com.dudu.android.launcher.R;
import com.dudu.drivevideo.frontcamera.FrontCameraManage;
import com.dudu.drivevideo.utils.FileUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2016/2/2.
 */
public class DrivingRecordFragment extends RBaseFragment implements /*SurfaceHolder.Callback, */View.OnClickListener {

    View bottomLayout;
    private ImageButton mCheckVideoButton, mSwitchVideoButton, mTakePhotoButton, mCheckPhotoButton, mBackButton;
    private boolean isFrontCameraPreView = true;
    private Logger log;

    public DrivingRecordFragment() {
        log = LoggerFactory.getLogger("video.drivevideo");
    }

    @Override
    protected int getContentView() {
        return R.layout.fragment_driving_record;
    }

    @Override
    protected void initView(View rootView) {
        super.initView(rootView);
        initFragmentView(rootView);
    }

    @Override
    protected void initViewData() {

        initClickListener();
    }

    private void initFragmentView(View view) {
        mCheckVideoButton = (ImageButton) view.findViewById(R.id.check_video);
        mSwitchVideoButton = (ImageButton) view.findViewById(R.id.switch_video);
        mCheckPhotoButton = (ImageButton) view.findViewById(R.id.check_photo);
        mTakePhotoButton = (ImageButton) view.findViewById(R.id.take_photo);
        mBackButton = (ImageButton) view.findViewById(R.id.button_back);
        bottomLayout = view.findViewById(R.id.video_button_bottom);

        startAnim();
    }

    private void initClickListener() {
        mSwitchVideoButton.setOnClickListener(this);
        mCheckVideoButton.setOnClickListener(this);
        mCheckPhotoButton.setOnClickListener(this);
        mTakePhotoButton.setOnClickListener(this);
        mBackButton.setOnClickListener(this);

        mTakePhotoButton.setOnLongClickListener(v1 -> {
//            if (CommonLib.getInstance().getVersionManage().isDemoVersionFlag()){
//                ToastUtils.showToast("停止后置录像");
//            }
//            stopRearCamera();
            return true;
        });
        mSwitchVideoButton.setOnLongClickListener(v -> {
//            if (CommonLib.getInstance().getVersionManage().isDemoVersionFlag()){
//                ToastUtils.showToast("开启后置录像");
//            }
//            startRearCamera();
            return true;
        });
    }

    private void startRearCamera() {
        //后门开启后置摄像头
        ObservableFactory.getInstance().getCommonObservable().startRearCamera();
    }

    private void stopRearCamera() {
        //后门关闭后置摄像头
        ObservableFactory.getInstance().getCommonObservable().stopRearCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        log.debug("onResume");
        mCheckPhotoButton.setEnabled(true);
        mBackButton.setEnabled(true);
        mCheckVideoButton.setEnabled(true);

//        mBaseActivity.setNoBlur();
//        FrontCameraManage.getInstance().setPreviewBlur(false);
        ObservableFactory.getInstance().getCommonObservable().hasBackground.set(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_video:
                replaceFragment(FragmentConstants.FRAGMENT_VIDEO_LIST);
                break;
            case R.id.take_photo:
                if (FileUtil.isTFlashCardExists()) {
                    float sdFreeSpace = FileUtil.getSdFreeSpace();
                    log.debug("拍照请求,T卡剩余空间比例:{}", sdFreeSpace);
                    if (sdFreeSpace < 0.01) {
                        showToast("存储卡空间不足, 无法拍照.");
                    } else {
//                        SoundPlayManager.play();//拍照声效的及时响应
                        FrontCameraManage.getInstance().takePicture();
                    }
                } else {
                    showToast("请确认存储卡是否已装置好");
                }
                break;
            case R.id.switch_video:
                log.debug("切换显示");
                startRearPreview();
                break;
            case R.id.check_photo:
                replaceFragment(FragmentConstants.FRAGMENT_PHOTO_LIST);

                break;
            case R.id.button_back:
                ObservableFactory.getInstance().getCommonObservable().hasBackground.set(true);
                FrontCameraManage.getInstance().setPreviewBlur(true);
                replaceFragment(FragmentConstants.FRAGMENT_MAIN_PAGE);
                break;
        }
    }

    private void showToast(String s) {
        T.show(mBaseActivity, s);
    }

    private void stopRearPreview() {
        ObservableFactory.getInstance().getCommonObservable().stopRearPreview();
    }

    private void startRearPreview() {
        ObservableFactory.getInstance().getCommonObservable().startRearPreview();
    }

    private void startAnim() {
        if (bottomLayout == null) {
            return;
        }

        bottomLayout.setScaleX(0f);
        bottomLayout.setScaleY(0f);

        bottomLayout.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(300)
                .withEndAction(() -> bottomLayout.animate().scaleX(1f).scaleY(1f).setDuration(300).start()).start();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
//        log.debug("onHiddenChanged : {}", hidden);
        super.onHiddenChanged(hidden);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onAdd() {
        log.debug("onAdd");
        onShow();
    }

    @Override
    public void onShow() {
//        ((MainRecordActivity) getActivity()).setBlur(false);
        log.debug("onshow");
        FrontCameraManage.getInstance().setPreviewBlur(false);
        startAnim();
    }

    @Override
    public void onHide() {
        log.debug("onHide");
//        ((MainRecordActivity) getActivity()).setBlur(true);
        FrontCameraManage.getInstance().setPreviewBlur(true);
    }
}
