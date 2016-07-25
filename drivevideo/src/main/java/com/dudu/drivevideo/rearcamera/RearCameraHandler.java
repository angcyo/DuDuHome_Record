package com.dudu.drivevideo.rearcamera;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.View;

import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.drivevideo.CheckSizeEvent;
import com.dudu.drivevideo.MsgEvent;
import com.dudu.drivevideo.config.RearVideoConfigParam;
import com.dudu.drivevideo.exception.CameraErrorCode;
import com.dudu.drivevideo.exception.DirveVideoException;
import com.dudu.drivevideo.rearcamera.camera.CameraHandlerMessage;
import com.dudu.drivevideo.rearcamera.camera.RearCameraListener;
import com.dudu.drivevideo.rearcamera.camera.RearCameraListenerMessage;
import com.dudu.drivevideo.rearcamera.camera.RearCameraRecorder;
import com.dudu.drivevideo.rearcamera.preview.RearCameraPreview;
import com.dudu.drivevideo.rearcamera.utils.RearVideoDeleteTools;
import com.dudu.drivevideo.rearcamera.utils.RecordFailedHelper;
import com.dudu.drivevideo.utils.RearVideoSaveTools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by dengjun on 2016/5/18.
 * Description :
 */
public class RearCameraHandler implements RearCameraListener, View.OnClickListener {
    /**
     * 共享当前正在录像的文件路径
     */
    public volatile static String curVideoPath = "";
    private static Logger log = LoggerFactory.getLogger("video.reardrivevideo");
    private RearCameraHandler rearCameraHandler = this;
    private HandlerThread handlerThread;
    private boolean handlerThreadInitFlag = false;
    private DriveVideoHandler driveVideoHandler;
    private RearVideoConfigParam rearVideoConfigParam;
    private RearCameraPreview rearCameraPreview;
    private RearCameraRecorder rearCameraRecorder;

    public RearCameraHandler() {
        rearVideoConfigParam = new RearVideoConfigParam();

        rearCameraPreview = new RearCameraPreview(rearVideoConfigParam);
//        rearCameraPreview.setRearCameraListener(this);
//        rearCameraPreview.setBackButtonOnClickListener(this);

        rearCameraRecorder = new RearCameraRecorder(rearVideoConfigParam);
        rearCameraRecorder.setRearCameraListener(this);
    }

    public static void printDevVideos() {
        File file = new File("/dev");
        if (file.exists()) {
            File[] files = file.listFiles();
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("video设备文件：");
            for (File file1 : files) {
                if (file1.getName().startsWith("video")) {
                    stringBuffer.append(file1.getName() + ",");
                }
            }
            log.info(stringBuffer.toString());
        }
    }

    private void initHandlerThread() {
        handlerThread = new HandlerThread("Rear Camera Thread") {
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                log.info("handlerThread 初始化完成");
                driveVideoHandler = new DriveVideoHandler(handlerThread.getLooper());
            }
        };
    }

    @Override
    public void onClick(View v) {
        log.info("行车记录界面，退出后置预览");
        stopPreview();
        if (!rearCameraPreview.detectCamera()) {
            rearCameraPreview.removeFromWindow();
        }
    }

    @Override
    public void onInfo(RearCameraListenerMessage rearCameraListenerMessage) {
        log.info("RearCameraInterface 回调信息 what = {}， message= {}", rearCameraListenerMessage.getWhat(), rearCameraListenerMessage.getMessage());
        if (rearCameraListenerMessage == RearCameraListenerMessage.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            if (FileUtil.isTFlashCardExists()) {
//                sendMessage(CameraHandlerMessage.STOP_RECORD);//停止录像
                sendMessage(CameraHandlerMessage.SAVE_VIDEO);
                sendMessage(CameraHandlerMessage.SET_VIDEO_PATH);
//                sendMessageDelay(CameraHandlerMessage.START_RECORD, 1);//开始录像
//                sendMessageDelay(CameraHandlerMessage.DELETE_UNUSED_264_VIDEO, 8);
            } else {
                log.info("sd卡不存在，停止录像");
                sendMessage(CameraHandlerMessage.STOP_RECORD);
                sendMessage(CameraHandlerMessage.DELETE_CUR_VIDEO);
            }
        }
    }

    @Override
    public void onError(RearCameraListenerMessage rearCameraListenerMessage) {
        log.error("mediaRedcord 报错 what = {}， message = {}", rearCameraListenerMessage.getWhat(), rearCameraListenerMessage.getMessage());
        switch (rearCameraListenerMessage) {
            case CAMERA_ERROR:
                sendMessage(CameraHandlerMessage.INIT_CAMERA);
                break;
            case PREVIEW_ERROR:
                proPreviewError();
                break;
            case RECORD_ERROR:
                sendMessage(CameraHandlerMessage.STOP_RECORD);
                sendMessage(CameraHandlerMessage.DELETE_CUR_VIDEO);
                sendMessageDelay(CameraHandlerMessage.STOP_RECORD, 1);
                break;
        }
    }


    public synchronized void init() {
        synchronized (this) {
            if (handlerThread == null && handlerThreadInitFlag == false) {
                log.info("RearCameraHandler初始化");
                initHandlerThread();
                handlerThread.start();
                handlerThreadInitFlag = true;
            } else {
                log.info("handlerThread 已经初始化了");
            }
        }
    }

    public synchronized void release() {
        synchronized (this) {
            if (handlerThread != null && handlerThread.isAlive()) {
                log.info("RearCameraHandler释放");
                stopPreviewHolder();
                stopPreview();
                if (isRecording()) {
                    stopRecord();
                }
                handlerThread.quitSafely();
                driveVideoHandler = null;
                handlerThread = null;
                handlerThreadInitFlag = false;
            }
        }
    }

    public synchronized void openCamera() {
        sendMessage(CameraHandlerMessage.INIT_CAMERA);
    }

    public synchronized void closeCamera() {
        sendMessage(CameraHandlerMessage.RELEASE_CAMERA);
    }

    public synchronized void startPreview(boolean visibleState) {
        log.info("预览使能状态：{}, 是否正在预览：{}", rearCameraPreview.isPreviewEnable(), rearCameraPreview.isPreviewIng());
        if (rearCameraPreview.detectCamera()) {
            rearCameraPreview.setPreviewEnable(true);//防止有launcher重启，有设备文件的情况下不能预览
            if (rearCameraPreview.isPreviewIng() == false) {
                sendMessage(CameraHandlerMessage.STOP_PREVIEW);
                rearCameraPreview.setBackButtonVisibleState(visibleState);
                openCamera();
                sendMessage(CameraHandlerMessage.START_PREVIEW);
            }
        } else {
            log.info("后置预览设备文件不存在，无法开启预览");
        }
        printDevVideos();
    }

    public synchronized void takePhoto() {
        if (rearCameraPreview.isPreviewIng()) {
            sendMessage(CameraHandlerMessage.TAKE_PICTURE);
            RearCameraPreview.log.info("开始拍照. 预览已开始");
        } else {
            log.info("未开始预览.");
            RearCameraPreview.log.info("开始拍照. 未开始预览");
        }
    }

    public boolean isPreviewing() {
        return rearCameraPreview.isPreviewIng();
    }

    public synchronized void setRecordTime(int time) {
        rearVideoConfigParam.setRecordTime(time);
    }

    public synchronized void startPreviewHolder(SurfaceHolder surfaceHolder) {
        log.info("预览使能状态：{}, 是否正在预览：{}", rearCameraPreview.isPreviewEnable(), rearCameraPreview.isPreviewIng());
        if (rearCameraPreview.detectCamera()) {
            log.info("后置预览设备文件存在，准备开启预览.");

            rearCameraPreview.setPreviewEnable(true);//防止有launcher重启，有设备文件的情况下不能预览
            if (rearCameraPreview.isPreviewIng() == false) {
                sendMessage(CameraHandlerMessage.STOP_PREVIEW_HOLDER);
                rearCameraPreview.setSurfaceHolder(surfaceHolder);
                openCamera();
                sendMessage(CameraHandlerMessage.START_PREVIEW_HOLDER);
            } else {
                log.info("预览已开始");
                EventBus.getDefault().post(new MsgEvent("预览已开始"));
            }
        } else {
            log.info("后置预览设备文件不存在，无法开启预览");
            EventBus.getDefault().post(new MsgEvent("后置预览设备文件不存在，无法开启预览"));
        }
        printDevVideos();
    }

    private synchronized void proPreviewError() {
        closeCamera();
//        sendMessageDelay(CameraHandlerMessage.INIT_CAMERA, 1);
//        sendMessageDelay(CameraHandlerMessage.START_PREVIEW, 2);
    }

    public synchronized void stopPreview() {
        if (driveVideoHandler != null) {
            driveVideoHandler.removeMessages(CameraHandlerMessage.START_PREVIEW.getNum(), CameraHandlerMessage.START_PREVIEW);
            driveVideoHandler.removeMessages(CameraHandlerMessage.INIT_CAMERA.getNum(), CameraHandlerMessage.INIT_CAMERA);
        }

        sendMessage(CameraHandlerMessage.STOP_PREVIEW);
        closeCamera();
    }

    public synchronized void stopPreviewHolder() {
        if (driveVideoHandler != null) {
            driveVideoHandler.removeMessages(CameraHandlerMessage.START_PREVIEW_HOLDER.getNum(), CameraHandlerMessage.START_PREVIEW_HOLDER);
            driveVideoHandler.removeMessages(CameraHandlerMessage.INIT_CAMERA.getNum(), CameraHandlerMessage.INIT_CAMERA);
        }
        sendMessage(CameraHandlerMessage.STOP_PREVIEW_HOLDER);
//        closeCamera();
    }

    public synchronized void stopPreviewHolder2() {
        if (driveVideoHandler != null) {
            driveVideoHandler.removeMessages(CameraHandlerMessage.START_PREVIEW_HOLDER.getNum(), CameraHandlerMessage.START_PREVIEW_HOLDER);
            driveVideoHandler.removeMessages(CameraHandlerMessage.INIT_CAMERA.getNum(), CameraHandlerMessage.INIT_CAMERA);
        }
        sendMessage(CameraHandlerMessage.STOP_PREVIEW_HOLDER);
    }

    public synchronized void startRecord() {
        log.debug("startRecord 后置录制状态：{}，使能状态：{}", rearCameraRecorder.isRecording(), rearCameraRecorder.isRecordEnable());
        if (rearCameraRecorder.detectCamera() && !rearCameraRecorder.isRecording()) {
            sendMessage(CameraHandlerMessage.SET_VIDEO_PATH);
            sendMessage(CameraHandlerMessage.START_RECORD);
        }
    }

    public boolean isRecording() {
        return rearCameraRecorder.isRecording();
    }

    public synchronized void stopRecord() {
        log.debug("stopRecord 后置录制状态：{}，使能状态：{}", rearCameraRecorder.isRecording(), rearCameraRecorder.isRecordEnable());
        sendMessage(CameraHandlerMessage.STOP_RECORD);
        sendMessage(CameraHandlerMessage.SAVE_VIDEO);
    }

    private void sendMessage(CameraHandlerMessage cameraHandlerMessage) {
        if (handlerThread != null && handlerThread.isAlive() && driveVideoHandler != null) {
            Message message = driveVideoHandler.obtainMessage(cameraHandlerMessage.getNum(), cameraHandlerMessage);
            driveVideoHandler.sendMessage(message);
        }
    }

    private void sendMessageDelay(CameraHandlerMessage cameraHandlerMessage, int seconds) {
        if (handlerThread != null && handlerThread.isAlive() && driveVideoHandler != null) {
            Message message = driveVideoHandler.obtainMessage(cameraHandlerMessage.getNum(), cameraHandlerMessage);
            driveVideoHandler.sendMessageDelayed(message, seconds * 1000);
        }
    }

    public RearVideoConfigParam getRearVideoConfigParam() {
        return rearVideoConfigParam;
    }


    public void setPreviewEnable(boolean previewEnable) {
        rearCameraPreview.setPreviewEnable(previewEnable);
    }

    public void setRecordEnable(boolean recordEnable) {
        rearCameraRecorder.setRecordEnable(recordEnable);
    }

    private class DriveVideoHandler extends Handler {
        private Runnable checkRunnable = new Runnable() {
            @Override
            public void run() {
                final boolean sizeChange = RecordFailedHelper.instance().checkSizeChange();
                EventBus.getDefault().post(new MsgEvent("检查文件大小是否改变:" + sizeChange +
                        " size:" + RecordFailedHelper.getFileSize(curVideoPath) + "\n" + curVideoPath));
                if (sizeChange) {
                    EventBus.getDefault().post(new CheckSizeEvent(true));
                    postDelayed(this, 5000);//5秒后,检查文件大小.
                } else {
                    //文件大小没有改变
                    //Native.uvcStoreVideo();//
                    EventBus.getDefault().post(new CheckSizeEvent(false));
                    removeCallbacks(this);
                }
            }
        };

        public DriveVideoHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                CameraHandlerMessage cameraHandlerMessage = (CameraHandlerMessage) msg.obj;
                switch (cameraHandlerMessage) {
                    case INIT_CAMERA:
                        log.info("收到初始化摄像头消息");
                        if (rearCameraPreview.isPreviewEnable()) {
                            rearCameraPreview.init();
                        }
                        break;
                    case RELEASE_CAMERA:
                        log.info("收到释放摄像头消息");
                        rearCameraPreview.release();
                        break;
                    case START_RECORD:
                        log.info("收到开启录像消息");
                        if (FileUtil.isTFlashCardExists() && rearCameraRecorder.isRecordEnable()) {
                            rearCameraRecorder.startRecord();
                        } else {
                            log.info("TF卡不存在或者录制没有使能，不开启录像");
                            rearCameraHandler.sendMessage(CameraHandlerMessage.STOP_RECORD);
                            rearCameraHandler.sendMessage(CameraHandlerMessage.DELETE_CUR_VIDEO);
                        }
                        break;
                    case STOP_RECORD:
                        log.info("收到停止录像消息");
                        rearCameraRecorder.stopRecord();
//                        removeMessages(CameraHandlerMessage.CHECK_FAILED.getNum());
                        removeCallbacks(checkRunnable);
                        break;
                    case TAKE_PICTURE:
                        log.info("拍照:收到拍照消息");
                        rearCameraPreview.takePhoto();
                        break;
                    case START_PREVIEW:
                        log.info("收到开启预览消息");
//                        if (rearCameraPreview.isPreviewEnable() && rearCameraPreview.isPreviewIng() == false) {
//                            rearCameraPreview.startPreview();
//                        }
                        break;
                    case STOP_PREVIEW:
                        log.info("收到停止预览消息");
//                        rearCameraPreview.stopPreview();
                        break;
                    case START_PREVIEW_HOLDER:
                        log.info("收到开启预览消息 Holder");
                        EventBus.getDefault().post(new MsgEvent("收到开启预览消息 Holder"));
                        if (rearCameraPreview.isPreviewEnable() && rearCameraPreview.isPreviewIng() == false) {
                            rearCameraPreview.startPreviewHolder();
                        }
                        break;
                    case STOP_PREVIEW_HOLDER:
                        log.info("收到停止预览消息 Holder");
                        rearCameraPreview.stopPreviewHolder();
                        break;
                    case SAVE_VIDEO:
                        log.debug("收到保存录像消息");
                        RearVideoSaveTools.saveCurVideoInfo(rearCameraRecorder.getCurVideoPath());
                        break;
//                    case CHECK_FAILED:
//                        final boolean sizeChange = RecordFailedHelper.instance().checkSizeChange();
//                        EventBus.getDefault().post(new MsgEvent("检查文件大小是否改变:" + sizeChange + "\n" + curVideoPath));
//                        if (sizeChange) {
//                            EventBus.getDefault().post(new CheckSizeEvent(true));
//                            sendEmptyMessageDelayed(CameraHandlerMessage.CHECK_FAILED.getNum(), 5000);//5秒后,检查文件大小.
//                        } else {
//                            //文件大小没有改变
//                            EventBus.getDefault().post(new CheckSizeEvent(false));
//                            removeMessages(CameraHandlerMessage.CHECK_FAILED.getNum());
//                        }
//                        break;
                    case SET_VIDEO_PATH:
                        log.debug("收到设置后置录像路径消息");
//                        rearCameraRecorder.setCurVideoPath(RearVideoSaveTools.generateCurVideoName264());
                        final String curVideoNameMp4 = RearVideoSaveTools.generateCurVideoNameMp4();
                        curVideoPath = curVideoNameMp4;
                        EventBus.getDefault().post(new MsgEvent("当前录像文件路径:" + curVideoPath));
                        rearCameraRecorder.setCurVideoPath(curVideoPath);

                        RecordFailedHelper.instance().setCurRecordFile(curVideoPath);
                        EventBus.getDefault().post(new MsgEvent("5秒后,检查文件大小."));

                        removeCallbacks(checkRunnable);
                        postDelayed(checkRunnable, 5000);//5秒后,检查文件大小.
                        break;
                    case DELETE_CUR_VIDEO:
                        log.debug("收到删除当前录像文件消息");
//                        RearVideoSaveTools.deleteCurVideo(rearCameraRecorder.getCurVideoPath());
//                        RearVideoSaveTools.saveCurVideoInfo(rearCameraRecorder.getCurVideoPath());
//                        H264Check.start();
                        break;
                    case DELETE_UNUSED_264_VIDEO:
                        RearVideoDeleteTools.deleteUnUsed264VideoFile(rearCameraRecorder.getCurVideoPath());
                        break;
                }
            } catch (DirveVideoException e) {
                CameraErrorCode errorCode = e.getErrorCode();
                log.error(errorCode.getDetailErrMsg(), e);
                switch (errorCode) {
                    case OPEN_CAMERA_ERROR:
                        rearCameraHandler.sendMessage(CameraHandlerMessage.RELEASE_CAMERA);
                        rearCameraHandler.sendMessageDelay(CameraHandlerMessage.INIT_CAMERA, 3);
                        break;
                    case CLOSE_CAMERA_ERROR:
                    case START_PREVIEW_ERROR:
                        proPreviewError();
                        break;
                    case STOP_PREVIEW_ERROR:
                    case STOP_RECORD_ERROR:
                        //not do nothing
                        break;
                    case START_RECORD_ERROR:
                        rearCameraHandler.sendMessage(CameraHandlerMessage.START_RECORD);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                log.error("异常", e);
            }
        }


        public void removeMessage(int what) {
            removeMessages(what);
        }

    }
}
