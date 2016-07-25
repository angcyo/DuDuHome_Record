package com.dudu.drivevideo.frontcamera;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.drivevideo.exception.CameraErrorCode;
import com.dudu.drivevideo.exception.DirveVideoException;
import com.dudu.drivevideo.frontcamera.camera.CameraHandlerMessage;
import com.dudu.drivevideo.frontcamera.event.VideoEvent;
import com.dudu.drivevideo.frontcamera.preview.PreviewState;
import com.dudu.drivevideo.video.VideoSaveTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;

/**
 * Created by dengjun on 2016/5/22.
 * Description :
 */
public class DriveVideoHandler extends Handler {
    private Logger log = LoggerFactory.getLogger("video.frontdrivevideo");

    private DriveVideoHandler driveVideoHandler = this;
    private FrontCameraService frontCameraService;

    public DriveVideoHandler(Looper looper, FrontCameraService frontCameraService) {
        super(looper);
        this.frontCameraService = frontCameraService;
    }

    @Override
    public void handleMessage(Message msg) {
        try {
            CameraHandlerMessage cameraHandlerMessage = (CameraHandlerMessage) msg.obj;
            switch (cameraHandlerMessage) {
                case INIT_CAMERA:
                    log.info("收到初始化摄像头消息");
                    frontCameraService.getFrontCamera().initCamera();
                    break;
                case RELEASE_CAMERA:
                    log.info("收到释放摄像头消息");
                    frontCameraService.getFrontCamera().releaseCamera();
                    break;
                case START_RECORD:
                    log.info("收到开启录像消息");
                    startRecord();
                    break;
                case RELEASE_RECORD:
                    log.info("收到停止录像消息");
                    frontCameraService.getFrontCameraRecorder().releaseMediaRecorder();
                    EventBus.getDefault().post(new VideoEvent(VideoEvent.OFF));
                    break;
                case RESET_RECORD:
                    break;
                case RELEASE_CAMERA_AND_RECORD:
                    log.info("释放摄像头和录像");
                    frontCameraService.getFrontCameraRecorder().releaseMediaRecordAndCamera();
                    EventBus.getDefault().post(new VideoEvent(VideoEvent.OFF));
                    break;
                case START_PREVIEW:
                    log.info("收到开启预览消息");
                    startPreview();
                    break;
                case STOP_PREVIEW:
                    log.info("收到停止预览消息");
                    frontCameraService.getFrontCamera().stopPreview();
                    break;
                case SAVE_VIDEO:
                    log.debug("收到保存录像消息");
                    VideoSaveTools.saveCurVideoInfo(frontCameraService.getFrontCameraRecorder().getCurVideoFileAbsolutePath());
                    break;
                case TAKE_PICTURE:
                    log.debug("收到拍照消息");
//                    frontCameraService.getPictureObtain().takePicture();
                    frontCameraService.getPhotoObtain().takePicture();
                    break;
                case START_UPLOAD_VIDEO_STREAM:
                    log.debug(CameraHandlerMessage.START_UPLOAD_VIDEO_STREAM.getMessage());
                    frontCameraService.getUploadVideoStream().startUploadVideoStream();
                    break;
                case STOP_UPLOAD_VIDEO_STREAM:
                    log.debug(CameraHandlerMessage.STOP_UPLOAD_VIDEO_STREAM.getMessage());
                    frontCameraService.getUploadVideoStream().stopUploadVideoStream();
                    break;
                case DESTROY_UPLOAD_VIDEO_STREAM:
                    log.debug(CameraHandlerMessage.DESTROY_UPLOAD_VIDEO_STREAM.getMessage());
                    frontCameraService.getUploadVideoStream().destroyUploadVideoStream();
                    break;
            }
        } catch (DirveVideoException e) {
            CameraErrorCode errorCode = e.getErrorCode();
            log.error(errorCode.getDetailErrMsg(), e);
            switch (errorCode) {
                case OPEN_CAMERA_ERROR:
                    sendMessageDelay(CameraHandlerMessage.INIT_CAMERA, 3);
                    break;
                case CLOSE_CAMERA_ERROR:
                case START_PREVIEW_ERROR:
                    sendMessage(CameraHandlerMessage.RELEASE_CAMERA);
                    sendMessageDelay(CameraHandlerMessage.INIT_CAMERA, 1);
                    sendMessageDelay(CameraHandlerMessage.START_PREVIEW, 3);
                    break;
                case START_PREVIEW_SURFACE_NULL_ERROR:
                    sendMessageDelay(CameraHandlerMessage.START_PREVIEW, 5);
                    break;
                case STOP_PREVIEW_ERROR:
                case STOP_RECORD_ERROR:
                    //not do nothing
                    break;
                case START_RECORD_ERROR:
                    sendMessage(CameraHandlerMessage.RELEASE_RECORD);
                    sendMessage(CameraHandlerMessage.START_RECORD);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("异常", e);
        }
    }

    private void startPreview() throws DirveVideoException {
        if (frontCameraService.getPreviewState() == PreviewState.MAINACITITY_PREVIEW && frontCameraService.getBlurGLSurfaceView() != null) {
            frontCameraService.getFrontCamera().startPreview(frontCameraService.getBlurGLSurfaceView().getSurfaceTexture());
        } else if (frontCameraService.getPreviewState() == PreviewState.WINDOW_PREVIEW) {
            frontCameraService.getFrontCamera().startPreview(frontCameraService.getFrontCameraPreviewWindow().getHolder());
        }
    }

    private void startRecord() throws DirveVideoException {
        if (FileUtil.isTFlashCardExists() && frontCameraService.getFrontCameraRecorder().isRecordEnable()) {
            if (frontCameraService.getPreviewState() == PreviewState.MAINACITITY_PREVIEW && frontCameraService.getBlurGLSurfaceView() != null) {
                frontCameraService.getFrontCameraRecorder().startRecord();
            } else if (frontCameraService.getPreviewState() == PreviewState.WINDOW_PREVIEW) {
                frontCameraService.getFrontCameraRecorder().startRecord();
            }
            EventBus.getDefault().post(new VideoEvent(VideoEvent.ON));
        } else {
            log.info("TF卡不存在 或者录像未使能，不开启录像");
        }
    }


    public void sendMessage(CameraHandlerMessage cameraHandlerMessage) {
        if (driveVideoHandler != null) {
            Message message = driveVideoHandler.obtainMessage(cameraHandlerMessage.getNum(), cameraHandlerMessage);
            driveVideoHandler.sendMessage(message);
        }
    }

    public void sendMessageDelay(CameraHandlerMessage cameraHandlerMessage, int seconds) {
        if (driveVideoHandler != null) {
            Message message = driveVideoHandler.obtainMessage(cameraHandlerMessage.getNum(), cameraHandlerMessage);
            driveVideoHandler.sendMessageDelayed(message, seconds * 1000);
        }
    }


    public void removeMessage(CameraHandlerMessage cameraHandlerMessage){
        if (driveVideoHandler != null) {
            driveVideoHandler.removeMessages(cameraHandlerMessage.getNum(), cameraHandlerMessage);
        }
    }
}
