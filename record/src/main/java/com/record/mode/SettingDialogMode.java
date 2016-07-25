package com.record.mode;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

import com.dudu.commonlib.utils.VersionTools;
import com.record.R;
import com.record.base.RBaseViewHolder;
import com.record.control.ConvertControl;
import com.record.control.MusicControl;
import com.record.control.RecordTimeControl;
import com.record.control.WatermarkControl;

/**
 * Created by robi on 2016-06-03 21:04.
 */
public class SettingDialogMode {

    private RBaseViewHolder mViewHolder;
    private Activity mActivity;

    private DialogFragment mDialogFragment;

    public SettingDialogMode(RBaseViewHolder viewHolder) {
        mViewHolder = viewHolder;
    }

    public SettingDialogMode(Fragment fragment, RBaseViewHolder viewHolder) {
        mViewHolder = viewHolder;
        mActivity = fragment.getActivity();
        mDialogFragment = (DialogFragment) fragment;
    }

    public void initView() {
        initCloseView();
        initVersionView();
        initWatermarkView();
        initConvertView();
        initRecordTimeView();
        initTakePhotoView();
    }

    private void initTakePhotoView() {
        ((ToggleButton) mViewHolder.v(R.id.takePhotoView)).setChecked(MusicControl.instance().isMusic());

        ((ToggleButton) mViewHolder.v(R.id.takePhotoView)).setOnCheckedChangeListener((buttonView, isChecked) -> MusicControl.instance().setMusic(isChecked));
    }

    private void initRecordTimeView() {
        ((RadioGroup) mViewHolder.v(R.id.rbGroup)).setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb1:
                    RecordTimeControl.setRecordTime(1);
                    break;
                case R.id.rb5:
                    RecordTimeControl.setRecordTime(5);
                    break;
                case R.id.rb10:
                    RecordTimeControl.setRecordTime(10);
                    break;
                case R.id.rb15:
                    RecordTimeControl.setRecordTime(15);
                    break;
            }
        });

        switch (RecordTimeControl.getRecordTime()) {
            case 10:
                ((RadioGroup) mViewHolder.v(R.id.rbGroup)).check(R.id.rb10);
                break;
            case 15:
                ((RadioGroup) mViewHolder.v(R.id.rbGroup)).check(R.id.rb15);
                break;
            case 5:
                ((RadioGroup) mViewHolder.v(R.id.rbGroup)).check(R.id.rb5);
                break;
            default:
                ((RadioGroup) mViewHolder.v(R.id.rbGroup)).check(R.id.rb1);
                break;
        }
    }

    private void initWatermarkView() {
        ((ToggleButton) mViewHolder.v(R.id.watermarkView)).setChecked(WatermarkControl.isMark());

        ((ToggleButton) mViewHolder.v(R.id.watermarkView)).setOnCheckedChangeListener((buttonView, isChecked) -> WatermarkControl.setMark(isChecked));
    }

    private void initConvertView() {

        boolean convertMp4 = ConvertControl.isConvertMp4();
        ((ToggleButton) mViewHolder.v(R.id.convertView)).setChecked(convertMp4);
        ((ToggleButton) mViewHolder.v(R.id.deleteH264View)).setChecked(ConvertControl.isDeleteH264());

        updateDeleteH264Layout(convertMp4);

        ((ToggleButton) mViewHolder.v(R.id.convertView)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConvertControl.setConvertMp4(isChecked);
            updateDeleteH264Layout(isChecked);
        });

        ((ToggleButton) mViewHolder.v(R.id.deleteH264View)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConvertControl.setDeleteH264(isChecked);
        });
    }

    private void updateDeleteH264Layout(boolean show) {
        if (show) {
            mViewHolder.v(R.id.deleteH264layout).setVisibility(View.VISIBLE);
        } else {
            mViewHolder.v(R.id.deleteH264layout).setVisibility(View.GONE);
        }

    }

    private void initVersionView() {
        mViewHolder.tV(R.id.versionView).setText(VersionTools.getAppVersion(mActivity));
    }

    private void initCloseView() {
        mViewHolder.v(R.id.closeView).setOnClickListener(v -> mDialogFragment.dismiss());
    }

    private void refreshRecycler() {

    }


    public void onDismiss() {
    }
}
