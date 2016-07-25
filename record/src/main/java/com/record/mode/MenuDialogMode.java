package com.record.mode;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.record.MainActivity;
import com.record.R;
import com.record.base.RBaseViewHolder;
import com.record.control.RecordControl;
import com.record.control.WatermarkControl;
import com.record.util.Debug;
import com.record.view.PhotoDialogFragment;
import com.record.view.SettingDialogFragment;
import com.record.view.VideoDialogFragment;

/**
 * Created by robi on 2016-06-03 21:04.
 */
public class MenuDialogMode {

    PhotoDialogFragment mPhotoDialogFragment = new PhotoDialogFragment();
    VideoDialogFragment mVideoDialogFragment = new VideoDialogFragment();
    SettingDialogFragment mSettingDialogFragment = new SettingDialogFragment();
    private RBaseViewHolder mViewHolder;
    private FragmentActivity mActivity;
    private DialogFragment mDialogFragment;

    public MenuDialogMode(RBaseViewHolder viewHolder) {
        mViewHolder = viewHolder;
    }

    public MenuDialogMode(DialogFragment fragment, RBaseViewHolder viewHolder) {
        mViewHolder = viewHolder;
        mDialogFragment = fragment;
        mActivity = fragment.getActivity();
    }

    public void initView() {
        initQuitView();
        initPhotosView();
        initVideosView();
//        initMuteView();
        initSettingView();
        initWatermarkView();
    }

    private void initWatermarkView() {
        updateWaterMarkView();
        (mViewHolder.v(R.id.watermarkView)).setOnClickListener(v -> {
            WatermarkControl.setMark(!WatermarkControl.isMark());
            updateWaterMarkView();
        });
    }

    private void updateWaterMarkView() {
        (mViewHolder.v(R.id.watermarkView)).setSelected(WatermarkControl.isMark());
    }

    /**
     * 打开设置菜单
     */
    private void initSettingView() {
        mViewHolder.v(R.id.settingView).setOnClickListener(v -> mSettingDialogFragment.show(mActivity.getSupportFragmentManager(), "mSettingDialogFragment"));
    }

//    /**
//     * 音量控制
//     */
//    private void initMuteView() {
//        updateMuteView();
//        mViewHolder.v(R.id.voiceView).setOnClickListener(v -> {
//            MusicControl.instance().setMusic();
//            updateMuteView();
//        });
//    }
//
//    private void updateMuteView() {
//        if (MusicControl.instance().isMusic()) {
//            ((TextView) mViewHolder.v(R.id.voiceView)).setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.v_mute_selector, 0, 0);
//        } else {
//            ((TextView) mViewHolder.v(R.id.voiceView)).setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.v_voice_selector, 0, 0);
//        }
//    }

    private void initQuitView() {
        mViewHolder.v(R.id.quitView).setOnClickListener(v -> onQuit());
    }

    /**
     * 打开图片列表
     */
    private void initPhotosView() {
        mViewHolder.v(R.id.photoView).setOnClickListener(v -> mPhotoDialogFragment.show(mActivity.getSupportFragmentManager(), "mPhotoDialogFragment"));
    }

    /**
     * 打开视频列表
     */
    private void initVideosView() {
        mViewHolder.v(R.id.videoView).setOnClickListener(v -> mVideoDialogFragment.show(mActivity.getSupportFragmentManager(), "mVideoDialogFragment"));
    }

    /**
     * 退出回调
     */
    private void onQuit() {
//        RecordControl.setRecordEnable(false);
//        RecordControl.sendRecord(mActivity, false);
//        RecordImpl.instance().stopRecord();
//        RearCameraManage.getInstance().stopPreViewHolder2();
//        RecordImpl.instance().stopPreview();
//        RearCameraManage.getInstance().release();

//        RecordService.sendRecordState(mActivity, RecordService.STOP_RECORD);
//        RecordControl.setRecordEnable(false);
//        ((MainActivity) mActivity).waitQuit();
//        RecordControl.sendRecord(mActivity, false);
//        RecordImpl.instance().release();

//        Intent intent = new Intent("com.record", Uri.parse("record:stop"));
//        mActivity.stopService(new Intent(intent));

//        if (RecordControl.isRecord()) {
//            RecordControl.sendRecord(mActivity, false);
//        }

//        mActivity.onBackPressed();
//        mActivity.finish();

//        android.os.Process.killProcess(android.os.Process.myPid());
//        System.exit(1);

        Debug.show(mActivity, "请求退出...");
        mDialogFragment.dismiss();
        RecordControl.setRecordEnable(false);
        if (RecordControl.isRecord()) {
            Debug.show(mActivity, "已开始录像...");
            ((MainActivity) mActivity).waitQuit();
            RecordControl.sendRecord(mActivity, false);
        } else {
            Debug.show(mActivity, "未开始录像...");
            mActivity.finish();
            System.exit(1);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public void onDismiss() {
        if (mActivity instanceof OnMenuListener) {
            ((OnMenuListener) mActivity).onDismiss();
        }
    }

    public interface OnMenuListener {
        void onDismiss();
    }
}
