package com.dudu.aios.ui.base;

import android.databinding.ObservableBoolean;

import com.dudu.android.launcher.databinding.ActivityLayoutCommonBinding;
import com.dudu.drivevideo.rearcamera.RearCameraManage;

/**
 * Created by lxh on 2016/1/20.
 */
public class CommonObservable {

    public final ObservableBoolean hasTitle = new ObservableBoolean();

    public final ObservableBoolean hasBack = new ObservableBoolean();

    public final ObservableBoolean hasBackground = new ObservableBoolean();

    public final ObservableBoolean hasRearCameraPreview= new ObservableBoolean();

    public ActivityLayoutCommonBinding activityLayoutCommonBinding;
    
    public CommonObservable(ActivityLayoutCommonBinding activityLayoutCommonBinding) {
        this.activityLayoutCommonBinding = activityLayoutCommonBinding;
        this.hasTitle.set(true);
        this.hasBack.set(true);
        this.hasBackground.set(true);
        this.hasRearCameraPreview.set(false);
    }




    public void startRearPreview(){
//        RearCameraService.getInstance().startDrivingPreview();

        RearCameraManage.getInstance().startDrivingPreview();
    }

    public void stopRearPreview(){

    }

    public void startRearCamera() {
        //后门开启后置摄像头

    }

    public void stopRearCamera() {
        //后门关闭后置摄像头

    }
}
