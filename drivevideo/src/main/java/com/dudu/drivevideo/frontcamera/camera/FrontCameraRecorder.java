package com.dudu.drivevideo.frontcamera.camera;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.view.Surface;

import com.dudu.commonlib.utils.DataJsonTranslation;
import com.dudu.drivevideo.config.FrontVideoConfigParam;
import com.dudu.drivevideo.exception.CameraErrorCode;
import com.dudu.drivevideo.exception.DirveVideoException;
import com.dudu.drivevideo.utils.AudioUtils;
import com.dudu.drivevideo.video.VideoSaveTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by dengjun on 2016/5/21.
 * Description :
 */
public class FrontCameraRecorder {
    private Logger log = LoggerFactory.getLogger("video.frontdrivevideo");

    private FrontCamera frontCamera;
    private FrontVideoConfigParam frontVideoConfigParam;

    /* 当前录制视频的文件绝对路径*/
    private String curVideoFileAbsolutePath;

    private MediaRecorder mMediaRecorder;
    private String recordingLock = "recordingLock";
    private boolean isRecording = false;

    private boolean recordEnable = false;

    private MediaRecorder.OnErrorListener onErrorListener = null;
    private MediaRecorder.OnInfoListener onInfoListener = null;

    public FrontCameraRecorder(FrontCamera frontCamera, FrontVideoConfigParam frontVideoConfigParam) {
        this.frontCamera = frontCamera;
        this.frontVideoConfigParam = frontVideoConfigParam;
    }


    private boolean prepareMediaRecorder() throws DirveVideoException{
        return prepareMediaRecorder(null);
    }

    private boolean prepareMediaRecorder(Surface surface) throws DirveVideoException {
        log.debug("准备录像");
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        if (frontCamera.getCamera() == null) {
            throw new DirveVideoException(CameraErrorCode.OPEN_CAMERA_ERROR, new NullPointerException("Camera Object is Empty!"));
        }
        frontCamera.getCamera().unlock();
        mMediaRecorder.setCamera(frontCamera.getCamera());

        if (onErrorListener != null) {
            mMediaRecorder.setOnErrorListener(onErrorListener);
        }
        if (onInfoListener != null) {
            mMediaRecorder.setOnInfoListener(onInfoListener);
        }

        CamcorderProfile profile = initCamcorderProfile();
        log.debug("设置参数：{}", DataJsonTranslation.objectToJson(profile));
        initParam(profile);

        //设置录制时间，录制完后会以后通知，收到通知后开启下一个录像
        mMediaRecorder.setMaxDuration(frontVideoConfigParam.getVideoInterval());

        curVideoFileAbsolutePath = VideoSaveTools.generateCurVideoName();
        log.debug("前置当前摄像文件路径：{}", curVideoFileAbsolutePath);
        mMediaRecorder.setOutputFile(curVideoFileAbsolutePath);

        if (surface != null && surface.isValid()){
            mMediaRecorder.setPreviewDisplay(surface);
        }

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            throw new DirveVideoException(CameraErrorCode.START_RECORD_ERROR, e);
        }

        log.debug("准备录像成功");
        return true;
    }

    private CamcorderProfile initCamcorderProfile() {
        CamcorderProfile profile = CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_BACK, frontVideoConfigParam.getQuality());

        profile.audioSampleRate = 16000;//16K, 必须;
        profile.audioCodec = MediaRecorder.AudioEncoder.AMR_WB;//必须

        profile.videoBitRate = frontVideoConfigParam.getVideoBitRate();
        profile.videoFrameWidth = frontVideoConfigParam.getWidth();
        profile.videoFrameHeight = frontVideoConfigParam.getHeight();
        profile.videoFrameRate = frontVideoConfigParam.getRate();
        profile.videoCodec = MediaRecorder.VideoEncoder.H264;
        return profile;
    }

    private void initParam(CamcorderProfile profile) {
        boolean isMultiMic = AudioUtils.isMultiMic();
        if (isMultiMic) {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        }

        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);

        if (isMultiMic) {
                /*一个都不能少*/
            mMediaRecorder.setAudioChannels(profile.audioChannels);
            mMediaRecorder.setAudioSamplingRate(profile.audioSampleRate);
            mMediaRecorder.setAudioEncodingBitRate(profile.audioBitRate);
            mMediaRecorder.setAudioEncoder(profile.audioCodec);
        }

        mMediaRecorder.setVideoEncoder(profile.videoCodec);

        mMediaRecorder.setVideoFrameRate(profile.videoFrameRate);
        mMediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
    }



    public void startRecord() throws DirveVideoException {
        synchronized (recordingLock) {
            if (isRecording == false) {
                if (prepareMediaRecorder()) {
                    try {
                        mMediaRecorder.start();
                    } catch (Exception e) {
                        isRecording = false;
                        //log.error("异常: ", e);
                        throw new DirveVideoException(CameraErrorCode.START_RECORD_ERROR, e);
                    }
                    log.info("开启录像成功");
                    isRecording = true;
                }
            } else {
                log.info("录像正在运行");
            }
        }
    }

    public void startRecord(Surface surface) throws DirveVideoException{
        synchronized (recordingLock) {
            if (isRecording == false) {
                if (prepareMediaRecorder(surface)) {
                    try {
                        mMediaRecorder.start();
                    } catch (Exception e) {
                        isRecording = false;
                        //log.error("异常: ", e);
                        throw new DirveVideoException(CameraErrorCode.START_RECORD_ERROR, e);
                    }
                    log.info("开启录像成功");
                    isRecording = true;
                }
            } else {
                log.info("录像正在运行");
            }
        }
    }

    public void stopRecord() {
        synchronized (recordingLock) {
            if (isRecording == true) {
                log.info("结束录像");
                if (mMediaRecorder != null) {
                    mMediaRecorder.stop();
                }
                isRecording = false;
                log.info("结束录像成功");
            } else {
                log.info("没有在录像");
            }
        }
    }

    public void resetMediaRecorder() {
        if (mMediaRecorder != null) {
            log.info("重置mediaRecorder");
            mMediaRecorder.reset();
            isRecording = false;
            log.info("重置mediaRecorder结束");
        }
    }

    public void releaseMediaRecorder() throws DirveVideoException {
        if (mMediaRecorder != null) {
            try {
                log.debug("释放mediaRecorder");
                mMediaRecorder.reset();   // clear recorder configuration
                mMediaRecorder.release(); // release the recorder object
                mMediaRecorder = null;
                frontCamera.getCamera().lock();
            } catch (Exception e) {
                throw new DirveVideoException(CameraErrorCode.STOP_RECORD_ERROR, e);
            }
            // lock camera for later use
            isRecording = false;
            log.debug("释放mediaRecorder完成");
        }
    }

    public void releaseMediaRecordAndCamera() throws DirveVideoException {
        log.info("释放录像和摄像头");
        releaseMediaRecorder();
        frontCamera.releaseCamera();
    }


    public void setOnErrorListener(MediaRecorder.OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public void setOnInfoListener(MediaRecorder.OnInfoListener onInfoListener) {
        this.onInfoListener = onInfoListener;
    }

    public String getCurVideoFileAbsolutePath() {
        return curVideoFileAbsolutePath;
    }

    public boolean isRecordEnable() {
        return recordEnable;
    }

    public void setRecordEnable(boolean recordEnable) {
        this.recordEnable = recordEnable;
    }

    public boolean isRecording() {
        return isRecording;
    }
}
