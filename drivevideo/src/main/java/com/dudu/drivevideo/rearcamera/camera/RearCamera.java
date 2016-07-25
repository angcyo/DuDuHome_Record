package com.dudu.drivevideo.rearcamera.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.dudu.aios.uvc.Native;
import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.drivevideo.config.RearVideoConfigParam;
import com.dudu.drivevideo.exception.CameraErrorCode;
import com.dudu.drivevideo.exception.DirveVideoException;
import com.dudu.drivevideo.utils.RearVideoSaveTools;
import com.hclydao.webcam.Ffmpeg;
import com.hclydao.webcam.V4L2VALUE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by dengjun on 2016/4/29.
 * Description :
 */
public class RearCamera implements RearCameraInterface<RearVideoConfigParam, ImageView> {
    private RearVideoConfigParam rearVideoConfigParam;

    private String curVideoPath;

    private boolean cameraOpenFlag = false;
    private RearCameraListener rearCameraListener;

    private V4L2VALUE v4l2value;
    private byte[] frameDataBuffer;
    private int numBuffer = 4;

    private Bitmap bitmap;

    private ImageView previewView;
    private boolean preViewFlag = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    private boolean takePictureFlag = false;


    private Subscription recordSubscription;

    private Logger log = LoggerFactory.getLogger("video.reardrivevideo");

    public RearCamera() {
        rearVideoConfigParam = new RearVideoConfigParam();

        frameDataBuffer = new byte[rearVideoConfigParam.getWidth() * rearVideoConfigParam.getHeight() *3/ 2];
//        bitmap = Bitmap.createBitmap(rearVideoConfigParam.getDwidth(), rearVideoConfigParam.getDheight(), Bitmap.Config.RGB_565);
    }

    @Override
    public void init() throws DirveVideoException {
        if (cameraOpenFlag){
            return;
        }
        if (!detectCamera()){
            log.error("摄像头设备文件不存在");
            throw new DirveVideoException(CameraErrorCode.OPEN_CAMERA_ERROR);
        }
        log.info("初始化摄像头{}", "/dev/"+ rearVideoConfigParam.getVideoDevice());
        int ret = Ffmpeg.open("/dev/"+ rearVideoConfigParam.getVideoDevice());
        if(ret < 0) {
            log.error("Ffmpeg.open错误");
            throw new DirveVideoException(CameraErrorCode.OPEN_CAMERA_ERROR);
        }
        ret = Ffmpeg.init(rearVideoConfigParam.getWidth(), rearVideoConfigParam.getHeight(), numBuffer);
        if(ret < 0) {
            log.error("Ffmpeg.init错误");
            throw new DirveVideoException(CameraErrorCode.OPEN_CAMERA_ERROR);
        }
        ret = Ffmpeg.streamon();
        if(ret < 0) {
            log.error("Ffmpeg.streamon错误");
            throw new DirveVideoException(CameraErrorCode.OPEN_CAMERA_ERROR);
        }
        log.info("初始化摄像头成功-------------------------");
        cameraOpenFlag = true;
    }

    @Override
    public void release() {
        log.info("释放摄像头");
        Ffmpeg.release();
        cameraOpenFlag = false;
    }

    @Override
    public void startPreview() throws DirveVideoException {
        if (preViewFlag == true){
            log.debug("startPreview  0");
            return;
        }
        log.debug("startPreview  1");
        preViewFlag =true;
        doPreview();
    }

    @Override
    public void stopPreview() throws DirveVideoException {
        log.info("停止预览-----------");
        preViewFlag = false;
    }

    @Override
    public String getCurVideoPath() {
        return curVideoPath;
    }

    private void doPreview(){
        new Thread(()->{
            try {
                log.info("开始预览--------------------------------------");
                while (preViewFlag){
                    if (cameraOpenFlag == false)
                        continue;
                    v4l2value = Ffmpeg.dqbufstruct(frameDataBuffer);
                    if(v4l2value.index < 0){
                        preViewFlag = false;
                        log.error("Ffmpeg.dqbuf error");
                        if (rearCameraListener != null){
                            rearCameraListener.onError(RearCameraListenerMessage.PREVIEW_ERROR);//重新开启
                        }
                        break;
                    }
                    try {
//                        log.debug("预览-----ing");
                        bitmap = decodeBitmapFromByteArray(frameDataBuffer);
                    } catch (Exception e) {
                        log.error("error", e);
                    }
                    if (previewView != null){
                        postInvalidate();
                    }
                    Ffmpeg.qbuf(v4l2value.index);
                }
                log.info("预览结束------------------------------------------");
            } catch (Exception e) {
                log.error("异常", e);
                if (rearCameraListener != null){
                    rearCameraListener.onError(RearCameraListenerMessage.PREVIEW_ERROR);
                }
            }
        }).start();
    }


    private void postInvalidate(){
        handler.post(()->{
            try {
                if (bitmap != null && previewView != null){
                    previewView.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                log.error("异常", e);
            }
        });
    }



    private Bitmap decodeBitmapFromByteArray(byte[] bitmapDataBuffer){
//        BitmapFactory.Options options=new BitmapFactory.Options();
//        options.inJustDecodeBounds = false;
//        options.inSampleSize = Math.round((float) 720/ (float) 480);
//        return BitmapFactory.decodeByteArray(bitmapDataBuffer, 0, bitmapDataBuffer.length, options);
        return BitmapFactory.decodeByteArray(bitmapDataBuffer, 0, v4l2value.byteused);
    }




    @Override
    public void setPreviewView(ImageView view) {
        this.previewView = view;
    }

    @Override
    public ImageView getPreviewView() {
        return previewView;
    }

    @Override
    public void startRecord() {
        new Thread(()->{
            log.info("开启后置录像:{}", "/dev/"+rearVideoConfigParam.getVideoRecordDevice());

            int ret = Native.uvcAP("/dev/"+rearVideoConfigParam.getVideoRecordDevice());
            log.info("UVC AP 录像结束 Exit:{}", ret);
        }).start();
    }

    @Override
    public void stopRecord() {
        log.info("停止后置录像");
        cancerSendVideoFinishMessage();
        Native.uvcStop();
    }

    @Override
    public void setCofigParam(RearVideoConfigParam configParam) {
        this.rearVideoConfigParam = configParam;
    }

    @Override
    public RearVideoConfigParam getConfigParam() {
        return rearVideoConfigParam;
    }

    @Override
    public void setRearCameraListener(RearCameraListener rearCameraListener) {
        this.rearCameraListener = rearCameraListener;
    }

    @Override
    public void setCurVideoPath(String videoPath) {
        curVideoPath = videoPath;
        log.info("当前录像文件路径：{}", curVideoPath);
        byte[] fn = curVideoPath.getBytes();
        Native.uvcSetFileName(fn, fn.length);

        delaySendVideoFinishMessage();
    }

    private boolean detectCamera(){
        return FileUtil.detectFileExist("/dev/"+ rearVideoConfigParam.getVideoDevice());
    }


    private void delaySendVideoFinishMessage(){
        recordSubscription =
                Observable
                .timer(60, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .subscribe(l->{
                    if (rearCameraListener != null){
                        rearCameraListener.onInfo(RearCameraListenerMessage.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED);
                    }
                },throwable -> {
                    log.error("异常", throwable);
                });
    }

    private void cancerSendVideoFinishMessage(){
        if (recordSubscription != null && !recordSubscription.isUnsubscribed()){
            recordSubscription.unsubscribe();
        }
    }


    @Override
    public void takePicture() {
        takePictureFlag = true;
    }

    private void doTakePicture(){
        if (takePictureFlag == true){
            takePictureFlag = false;
            Bitmap bitmapToSave = Bitmap.createBitmap(bitmap);
            RearVideoSaveTools.savePictureAction(bitmapToSave);
        }
    }
}
