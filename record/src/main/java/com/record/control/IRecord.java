package com.record.control;

import android.view.SurfaceHolder;

/**
 * Created by robi on 2016-06-04 14:36.
 */
public interface IRecord {
    void startPreview(SurfaceHolder surfaceHolder);

    void stopPreview();

    void startRecord();

    void stopRecord();

    void takePhoto();

    boolean isPreviewing();

    boolean isRecording();

    void release();

    void init();
}
