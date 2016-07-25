package com.dudu.drivevideo.rearcamera;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.drivevideo.CheckSizeEvent;
import com.dudu.drivevideo.MsgEvent;
import com.dudu.drivevideo.config.RearVideoConfigParam;
import com.dudu.drivevideo.exception.CameraErrorCode;
import com.dudu.drivevideo.exception.DirveVideoException;
import com.dudu.drivevideo.rearcamera.camera.BackCarPreviewWindow;
import com.dudu.drivevideo.rearcamera.camera.RearCamera;
import com.dudu.drivevideo.rearcamera.camera.RearCameraInterface;
import com.dudu.drivevideo.rearcamera.camera.RearCameraListener;
import com.dudu.drivevideo.rearcamera.camera.RearCameraListenerMessage;
import com.dudu.drivevideo.rearcamera.utils.RecordFailedHelper;
import com.dudu.drivevideo.utils.RearVideoSaveTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.greenrobot.event.EventBus;

/**
 * Created by dengjun on 2016/4/29.
 * Description :
 */
@Deprecated
public class RearCameraService implements RearCameraListener {
    public static final int INIT_CAMERA = 0;
    public static final int RELEASE_CAMERA = 1;
    public static final int START_RECORD = 2;
    public static final int STOP_RECORD = 3;
    public static final int RESET_RECORD = 4;
    public static final int RELEASE_RECORD = 5;
    public static final int RELEASE_CAMERA_AND_RECORD = 6;
    public static final int TAKE_PICTURE = 7;
    public static final int START_PREVIEW = 8;
    public static final int STOP_PREVIEW = 9;
    public static final int SAVE_VIDEO = 10;
    public static final int SET_VIDEO_PATH = 11;
    public static final int CHECK_FAILED = 12;//检查录像文件5秒内 ,是否改变大小
    /**
     * 共享当前正在录像的文件路径
     */
    public volatile static String curVideoPath = "";
    private static RearCameraService instance = null;
    private RearCameraInterface rearCamera;
    private HandlerThread handlerThread;
    private DriveVideoHandler driveVideoHandler;
    private BackCarPreviewWindow backCarPreviewWindow;
    private Logger log = LoggerFactory.getLogger("video.reardrivevideo");

    private RearCameraService() {
        backCarPreviewWindow = new BackCarPreviewWindow(CommonLib.getInstance().getContext());

        rearCamera = new RearCamera();
        rearCamera.setRearCameraListener(RearCameraService.this);
        rearCamera.setPreviewView(backCarPreviewWindow.getPreviewImageView());


        handlerThread = new HandlerThread("Rear Camera Thread") {
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                log.info("handlerThread 初始化完成");
                driveVideoHandler = new DriveVideoHandler(handlerThread.getLooper());
            }
        };
    }

    public static RearCameraService getInstance() {
        if (instance == null) {
            synchronized (RearCameraService.class) {
                if (instance == null) {
                    instance = new RearCameraService();
                }
            }
        }
        return instance;
    }

    public void init() {
        if (handlerThread.isAlive())
            return;
        log.info("RearCameraService初始化");
        handlerThread.start();
    }

    public void release() {
        log.info("RearCameraService释放");
        closeCamera();
        stopRecord();
        if (handlerThread.isAlive()) {
            handlerThread.quitSafely();
        }
    }

    public void openCamera() {
        sendMessage(INIT_CAMERA);
    }

    public void closeCamera() {
        sendMessage(STOP_PREVIEW);
        driveVideoHandler.removeMessages(INIT_CAMERA);
        sendMessage(RELEASE_CAMERA);
    }


    private void startPreview(boolean visibleState) {
        if (FileUtil.detectFileExist("/dev/" + ((RearVideoConfigParam) rearCamera.getConfigParam()).getVideoDevice())) {
            sendMessage(STOP_PREVIEW);
            backCarPreviewWindow.addToWindow();
            backCarPreviewWindow.setBackButtonVisibleState(visibleState);
            openCamera();
            sendMessage(START_PREVIEW);
        }
    }

    public void stopPreview() {
        sendMessage(STOP_PREVIEW);
        backCarPreviewWindow.removeFromWindow();
    }


    public void startRecord() {
        sendMessage(SET_VIDEO_PATH);
        sendMessage(START_RECORD);
    }

    public void stopRecord() {
        sendMessage(STOP_RECORD);
        sendMessage(SAVE_VIDEO);
    }

    public void takePicture() {
        sendMessage(TAKE_PICTURE);
    }

    @Override
    public void onInfo(RearCameraListenerMessage rearCameraListenerMessage) {
        log.info("RearCameraInterface 回调信息 what = {}， message= {}", rearCameraListenerMessage.getWhat(), rearCameraListenerMessage.getMessage());
        if (rearCameraListenerMessage == RearCameraListenerMessage.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            sendMessage(SAVE_VIDEO);
            sendMessage(SET_VIDEO_PATH);
        }
    }

    @Override
    public void onError(RearCameraListenerMessage rearCameraListenerMessage) {
        log.error("mediaRedcord 报错 what = {}， message = {}", rearCameraListenerMessage.getWhat(), rearCameraListenerMessage.getMessage());
        switch (rearCameraListenerMessage) {
            case CAMERA_ERROR:
                sendMessage(INIT_CAMERA);
                break;
            case PREVIEW_ERROR:
                sendMessage(RELEASE_CAMERA);
                sendMessage(START_PREVIEW);
                break;
        }
    }


    private void sendMessage(int message) {
        if (driveVideoHandler != null) {
            driveVideoHandler.sendEmptyMessage(message);
        }
    }

    private void sendMessageDelay(int message, int milliSeconds) {
        if (driveVideoHandler != null) {
            driveVideoHandler.sendEmptyMessageDelayed(message, milliSeconds);
        }
    }

    public RearVideoConfigParam getRearVideoConfigParam() {
        return (RearVideoConfigParam) rearCamera.getConfigParam();
    }


    public void startDrivingPreview() {
        startPreview(true);
    }

    public void startBackCarPreview() {
        startPreview(false);
    }

    private class DriveVideoHandler extends Handler {
        public DriveVideoHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case INIT_CAMERA:
                        log.info("收到初始化摄像头消息");
                        rearCamera.init();
                        break;
                    case RELEASE_CAMERA:
                        log.info("收到释放摄像头消息");
                        rearCamera.release();
                        break;
                    case START_RECORD:
                        log.info("收到开启录像消息");
                        if (FileUtil.isTFlashCardExists()) {
                            rearCamera.startRecord();
                        } else {
                            log.info("TF卡不存在，不开启录像");
                        }
                        break;
                    case STOP_RECORD:
                        log.info("收到停止录像消息");
                        rearCamera.stopRecord();
                        removeMessages(CHECK_FAILED);
                        break;
                    case RELEASE_RECORD:
                        removeMessages(CHECK_FAILED);
                        break;
                    case RELEASE_CAMERA_AND_RECORD:
                        log.info("收到释放摄像头和停止录像消息");
                        removeMessages(CHECK_FAILED);
                        break;
                    case TAKE_PICTURE:
                        rearCamera.takePicture();
                        break;
                    case START_PREVIEW:
                        log.info("收到开启预览消息");
                        rearCamera.startPreview();
                        break;
                    case STOP_PREVIEW:
                        log.info("收到停止预览消息");
                        rearCamera.stopPreview();
                        break;
                    case CHECK_FAILED:
                        final boolean sizeChange = RecordFailedHelper.instance().checkSizeChange();
                        EventBus.getDefault().post(new MsgEvent("检查文件大小是否改变:" + sizeChange + "\n" + curVideoPath));
                        if (sizeChange) {
                            EventBus.getDefault().post(new CheckSizeEvent(true));
                            sendEmptyMessageDelayed(CHECK_FAILED, 5000);//5秒后,检查文件大小.
                        } else {
                            //文件大小没有改变
                            EventBus.getDefault().post(new CheckSizeEvent(false));
                            removeMessages(CHECK_FAILED);
                        }
                        break;
                    case SAVE_VIDEO:
                        log.info("保存录像信息");
                        RearVideoSaveTools.saveCurVideoInfo(rearCamera.getCurVideoPath());
                        break;
                    case SET_VIDEO_PATH:
                        log.debug("收到设置后置录像路径消息");
//                        rearCamera.setCurVideoPath(RearVideoSaveTools.generateCurVideoName264());
                        final String curVideoNameMp4 = RearVideoSaveTools.generateCurVideoNameMp4();
                        curVideoPath = curVideoNameMp4;

                        EventBus.getDefault().post(new MsgEvent("当前录像文件路径:" + curVideoPath));
                        rearCamera.setCurVideoPath(curVideoPath);
                        RecordFailedHelper.instance().setCurRecordFile(curVideoPath);
                        EventBus.getDefault().post(new MsgEvent("5秒后,检查文件大小."));
                        sendEmptyMessageDelayed(CHECK_FAILED, 5000);//5秒后,检查文件大小.
                        break;
                }
            } catch (DirveVideoException e) {
                CameraErrorCode errorCode = e.getErrorCode();
                log.error(errorCode.getDetailErrMsg(), e);
                switch (errorCode) {
                    case OPEN_CAMERA_ERROR:
                        sendEmptyMessage(RELEASE_CAMERA);
                        sendEmptyMessageDelayed(INIT_CAMERA, 3 * 1000);
                        break;
                    case CLOSE_CAMERA_ERROR:
                    case START_PREVIEW_ERROR:
                    case STOP_PREVIEW_ERROR:
                    case STOP_RECORD_ERROR:
                        //not do nothing
                        break;
                    case START_RECORD_ERROR:
                        sendEmptyMessage(RELEASE_RECORD);
                        sendEmptyMessage(START_RECORD);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                log.error("异常", e);
            }
        }
    }
}
