package com.dudu.drivevideo.camera;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;

import com.dudu.commonlib.CommonLib;
import com.dudu.drivevideo.config.FrontVideoConfigParam;
import com.dudu.drivevideo.exception.CameraErrorCode;
import com.dudu.drivevideo.exception.DirveVideoException;
import com.dudu.drivevideo.utils.FileUtil;
import com.dudu.drivevideo.utils.TimeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by dengjun on 2016/3/26.
 * Description :
 */
public class CameraInstance {
    private FrontVideoConfigParam frontVideoConfigParam;

    private Camera camera;
    private String openCameraLock = "openCameraLock";
    private Camera.ErrorCallback errorCallback = null;

    private String previewingLock = "previewingLock";

    /* 当前录制视频的文件绝对路径*/
    private String curVideoFileAbsolutePath;

    private MediaRecorder mMediaRecorder;
    private String recordingLock = "recordingLock";
    private boolean isRecording = false;

    private MediaRecorder.OnErrorListener onErrorListener = null;
    private MediaRecorder.OnInfoListener onInfoListener = null;

    /* true 为CameraPreview， false为CameraPreviewWindow*/
    private boolean foregoundPreviewFlag = true;
    //    private  Surface previewSurface;
    /*启动是使用这个预览， 切换到其他activity时需要停掉 */
    //    private SurfaceView surfaceView;
    //private CameraPreviewWindow cameraPreviewWindow;

    private SurfaceHolder backgroundSurfaceHolder;
    private SurfaceHolder foregroundSurfaceHolder;

    /* 拍照*/
    private PictureObtain pictureObtain;
    private long takeTimeStamp = 0;

    private Logger log = LoggerFactory.getLogger("video.frontdrivevideo");
    CameraPreviewWindow cameraPreviewWindow = new CameraPreviewWindow(CommonLib.getInstance().getContext());

    public CameraInstance() {
        frontVideoConfigParam = new FrontVideoConfigParam();
        pictureObtain = new PictureObtain();

        backgroundSurfaceHolder = cameraPreviewWindow.getHolder();
    }

    public SurfaceHolder getBackgroundSurfaceHolder() {
        return backgroundSurfaceHolder;
    }

    public SurfaceHolder getForegroundSurfaceHolder() {
        return foregroundSurfaceHolder;
    }

    public void setForegroundSurfaceHolder(SurfaceHolder foregroundSurfaceHolder) {
        this.foregroundSurfaceHolder = foregroundSurfaceHolder;
    }

    public void initCamera() throws DirveVideoException {
        if (camera != null) {
            return;
        }
        synchronized (openCameraLock) {
            if (checkCameraHardware(Camera.CameraInfo.CAMERA_FACING_BACK)) {
                log.info("开始初始化camera...");
                try {
                    camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                } catch (Exception e) {
                    //log.error("获取相机失败", e);
                    throw new DirveVideoException(CameraErrorCode.OPEN_CAMERA_ERROR, e);
                }

                Camera.Parameters params = camera.getParameters();
                params.setPreviewFormat(PixelFormat.YCbCr_420_SP);

                params.setPreviewSize(frontVideoConfigParam.getWidth(), frontVideoConfigParam.getHeight());
                params.setPictureSize(frontVideoConfigParam.getWidth(), frontVideoConfigParam.getHeight());

                camera.setParameters(params);
                if (errorCallback != null) {
                    camera.setErrorCallback(errorCallback);
                }

//                try {
//                    camera.setPreviewDisplay(cameraPreview.getHolder());
//                } catch (IOException e) {
//                    log.error("开始相机setPreviewDisplay错误: ", e);
//                }
                log.info("初始化camera成功...");
            } else {
                log.debug("摄像头处于关闭状态");
            }
        }
    }

    public void releaseCamera() throws DirveVideoException {
        synchronized (openCameraLock) {
            if (camera != null) {
                try {
                    log.debug("释放前置摄像头");
                    camera.release();
                } catch (Exception e) {
                    throw new DirveVideoException(CameraErrorCode.CLOSE_CAMERA_ERROR, e);
                }
                camera = null;
                log.debug("释放前置摄像头成功");
            }
        }
    }

    public void startPreview() throws DirveVideoException {
        synchronized (previewingLock) {
            if (camera != null) {
                log.info("开启预览");
                camera.stopPreview();
                try {
                    camera.setPreviewDisplay(isForegoundPreview() ? foregroundSurfaceHolder : backgroundSurfaceHolder);
                    camera.startPreview();
                } catch (Exception e) {
                    throw new DirveVideoException(CameraErrorCode.START_PREVIEW_ERROR, e);
                }
            }
        }
    }

    public void stopPreview() throws DirveVideoException {
        synchronized (previewingLock) {
            if ( camera != null) {
                try {
                    log.info("关闭预览");
                    camera.stopPreview();
                } catch (Exception e) {
                    //log.error("结束预览出错", e);
                    throw new DirveVideoException(CameraErrorCode.STOP_PREVIEW_ERROR, e);
                }
            }
        }
    }


    private boolean prepareMediaRecorder() throws DirveVideoException {
        log.debug("准备录像");
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        if (camera == null) {
            throw new DirveVideoException(CameraErrorCode.OPEN_CAMERA_ERROR,  new NullPointerException("Camera Object is Empty!"));
        }
        camera.unlock();
        mMediaRecorder.setCamera(camera);

        if (onErrorListener != null) {
            mMediaRecorder.setOnErrorListener(onErrorListener);
        }
        if (onInfoListener != null) {
            mMediaRecorder.setOnInfoListener(onInfoListener);
        }

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        CamcorderProfile profile = CamcorderProfile.get(frontVideoConfigParam.getQuality());
        if (profile.videoBitRate > frontVideoConfigParam.getVideoBitRate()) {
            mMediaRecorder.setVideoEncodingBitRate(frontVideoConfigParam.getVideoBitRate());
        } else {
            mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
        }

        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        //设置录制时间，录制完后会以后通知，收到通知后开启下一个录像
        mMediaRecorder.setMaxDuration(frontVideoConfigParam.getVideoInterval());

        curVideoFileAbsolutePath = generateCurVideoName();
        log.debug("前置当前摄像文件路径：{}", curVideoFileAbsolutePath);
        mMediaRecorder.setOutputFile(curVideoFileAbsolutePath);

        mMediaRecorder.setVideoFrameRate(frontVideoConfigParam.getRate());
        mMediaRecorder.setVideoSize(frontVideoConfigParam.getWidth(), frontVideoConfigParam.getHeight());

        if (foregoundPreviewFlag == true) {
            if (foregroundSurfaceHolder != null ) {
                mMediaRecorder.setPreviewDisplay(foregroundSurfaceHolder.getSurface());
            }
        } else {
            if (backgroundSurfaceHolder != null) {
                mMediaRecorder.setPreviewDisplay(backgroundSurfaceHolder.getSurface());
            }
        }

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            throw new DirveVideoException(CameraErrorCode.START_RECORD_ERROR, e);
        }

        log.debug("准备录像成功");
        return true;
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
                camera.lock();
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
        releaseCamera();
    }

    public Camera getCamera() {
        return camera;
    }

    private boolean checkCameraHardware(int cameraFacing) {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == cameraFacing) {
                return true;
            }
        }
        return false;
    }


    public void takePicture() {
        if (camera != null && foregoundPreviewFlag == true && (System.currentTimeMillis() - takeTimeStamp > 1000)) {
            camera.takePicture(null, null, pictureObtain);
            takeTimeStamp = System.currentTimeMillis();
        }
    }

    public FrontVideoConfigParam getFrontVideoConfigParam() {
        return frontVideoConfigParam;
    }

    public void setFrontVideoConfigParam(FrontVideoConfigParam frontVideoConfigParam) {
        this.frontVideoConfigParam = frontVideoConfigParam;
    }


    public boolean isRecording() {
        return isRecording;
    }

    /**
     *
     * @return true 前台预览
     */
    public boolean isForegoundPreview() {
        return foregoundPreviewFlag;
    }


    public void setForegoundPreview(boolean foregoundPreviewFlag) {
        this.foregoundPreviewFlag = foregoundPreviewFlag;
    }

    public void setOnErrorListener(MediaRecorder.OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public String getCurVideoFileAbsolutePath() {
        return curVideoFileAbsolutePath;
    }

    public static String generateCurVideoName() {
        return FileUtil.getTFlashCardDirFile("/dudu", FrontVideoConfigParam.VIDEO_STORAGE_PATH).getAbsolutePath()
                + File.separator + TimeUtils.format(TimeUtils.format6) + ".mp4";
    }

    public String generateCurVideoThumbnailPath() {
        return FileUtil.getTFlashCardDirFile("/dudu", FrontVideoConfigParam.VIDEO_THUMBNAIL_STORAGE_PATH).getAbsolutePath()
                + File.separator + TimeUtils.format(TimeUtils.format6) + ".png";
    }

    public void setErrorCallback(Camera.ErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
    }

    public void setOnInfoListener(MediaRecorder.OnInfoListener onInfoListener) {
        this.onInfoListener = onInfoListener;
    }

}
