package com.dudu.drivevideo.frontcamera.camera;

import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.dudu.drivevideo.config.FrontVideoConfigParam;
import com.dudu.drivevideo.exception.CameraErrorCode;
import com.dudu.drivevideo.exception.DirveVideoException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dengjun on 2016/5/21.
 * Description :
 */
public class FrontCamera {
    private Logger log = LoggerFactory.getLogger("video.frontdrivevideo");

    private FrontVideoConfigParam frontVideoConfigParam;

    private Camera camera = null;
    private String openCameraLock = "openCameraLock";
    private Camera.ErrorCallback errorCallback = null;

    private String previewingLock = "previewingLock";

    public FrontCamera(FrontVideoConfigParam frontVideoConfigParam) {
        this.frontVideoConfigParam = frontVideoConfigParam;
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
                camera.setDisplayOrientation(frontVideoConfigParam.getDegrees());
                if (errorCallback != null) {
                    camera.setErrorCallback(errorCallback);
                }
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

    public void startPreview(SurfaceTexture surfaceTexture) throws DirveVideoException {
        synchronized (previewingLock) {
            if (camera != null && surfaceTexture != null) {
                log.info("开启预览  surfaceTexture");
                try {
                    camera.setPreviewTexture(surfaceTexture);
                    camera.startPreview();
                } catch (Exception e) {
                    throw new DirveVideoException(CameraErrorCode.START_PREVIEW_ERROR, e);
                }
            }
            if (surfaceTexture == null){
                log.info("surfaceTexture == null");
                throw new DirveVideoException(CameraErrorCode.START_PREVIEW_SURFACE_NULL_ERROR, new Exception(CameraErrorCode.START_PREVIEW_SURFACE_NULL_ERROR.getDetailErrMsg()));
            }
        }
    }

    public void startPreview(SurfaceHolder surfaceHolder) throws DirveVideoException {
        synchronized (previewingLock) {
            if (camera != null && surfaceHolder != null) {
                log.info("开启预览 surfaceHolder");
                try {
                    camera.setPreviewDisplay(surfaceHolder);
                    camera.startPreview();
                } catch (Exception e) {
                    throw new DirveVideoException(CameraErrorCode.START_PREVIEW_ERROR, e);
                }
            }
            if (surfaceHolder == null){
                log.info("surfaceHolder == null");
                throw new DirveVideoException(CameraErrorCode.START_PREVIEW_SURFACE_NULL_ERROR, new Exception(CameraErrorCode.START_PREVIEW_SURFACE_NULL_ERROR.getDetailErrMsg()));
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


    public void setErrorCallback(Camera.ErrorCallback errorCallback) {
        this.errorCallback = errorCallback;
    }
}
