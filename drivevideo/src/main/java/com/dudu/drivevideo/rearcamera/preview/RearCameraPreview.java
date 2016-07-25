package com.dudu.drivevideo.rearcamera.preview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.View;

import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.commonlib.utils.image.ImageUtils;
import com.dudu.drivevideo.MsgEvent;
import com.dudu.drivevideo.config.FrontVideoConfigParam;
import com.dudu.drivevideo.config.RearVideoConfigParam;
import com.dudu.drivevideo.exception.CameraErrorCode;
import com.dudu.drivevideo.exception.DirveVideoException;
import com.dudu.drivevideo.rearcamera.camera.RearCameraListener;
import com.dudu.drivevideo.rearcamera.camera.RearCameraListenerMessage;
import com.dudu.drivevideo.utils.TimeUtils;
import com.dudu.persistence.factory.RealmCallFactory;
import com.dudu.persistence.realm.RealmCallBack;
import com.dudu.persistence.realmmodel.picture.PictureEntity;
import com.dudu.persistence.realmmodel.picture.PictureEntityRealm;
import com.google.gson.Gson;
import com.hclydao.webcam.Ffmpeg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;

/**
 * Created by dengjun on 2016/5/17.
 * Description :
 */
public class RearCameraPreview implements SurfaceHolder.Callback {
    public static Logger log = LoggerFactory.getLogger("video.reardrivevideo");
    Rect src = new Rect(0, 0, 1280, 720);
    Rect dst = new Rect(0, 0, 854, 480);
    private RearVideoConfigParam rearVideoConfigParam;
    private RearCameraPreviewWindow rearCameraPreviewWindow;
    private boolean cameraOpenFlag = false;
    //private V4L2VALUE v4l2value;
    private int v4l2_index;
    private byte[] frameDataBuffer;
    private int numBuffer = 4;
    private boolean preViewFlag = false;
    private RearCameraListener rearCameraListener;
    private boolean previewEnable = false;
    private Canvas canvas = null;
    private Paint mPaint = new Paint();
    private SurfaceHolder mSurfaceHolder;
    private AtomicInteger mAtomicInteger = new AtomicInteger(0);
    private ByteBuffer Imagbuf;
    private boolean previewFinishFlag = true;
    private Bitmap bitmap;
    private Matrix matrix;

    public RearCameraPreview(RearVideoConfigParam rearVideoConfigParam) {
        this.rearVideoConfigParam = rearVideoConfigParam;
//        rearCameraPreviewWindow = new RearCameraPreviewWindow(CommonLib.getInstance().getContext());
//        rearCameraPreviewWindow.addSurfaceHolderCallback(this);

        frameDataBuffer = new byte[rearVideoConfigParam.getWidth() * rearVideoConfigParam.getHeight() * 3 / 2];
    }

    private static void savePictureAction(Bitmap bitmap) {
        String pictureAbApth = generatePicturePath();
        log.info("拍摄照片：{}", pictureAbApth);
        try {
            ImageUtils.saveBitmap(bitmap, pictureAbApth);
            if (new File(pictureAbApth).exists()) {
                RealmCallFactory.savePictureInfo(true, pictureAbApth, new RealmCallBack<PictureEntityRealm, Exception>() {
                    @Override
                    public void onRealm(PictureEntityRealm result) {
                        log.debug("保存的照片信息：{}", new Gson().toJson(new PictureEntity(result)));
                    }

                    @Override
                    public void onError(Exception error) {

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String generatePicturePath() {
        return FileUtil.getTFlashCardDirFile("/_Record", FrontVideoConfigParam.VIDEO_PICTURE_STORAGE_PATH).getAbsolutePath()
                + File.separator + TimeUtils.format(TimeUtils.format5) + ".jpg";
    }

    public void init() throws DirveVideoException {
        if (cameraOpenFlag) {
            return;
        }
        if (!detectCamera()) {
            log.error("摄像头设备文件不存在");
            throw new DirveVideoException(CameraErrorCode.OPEN_CAMERA_ERROR);
        }
        log.info("初始化摄像头{}", "/dev/" + rearVideoConfigParam.getVideoDevice());
        int ret = Ffmpeg.open("/dev/" + rearVideoConfigParam.getVideoDevice());
        if (ret < 0) {
            log.error("Ffmpeg.open错误");
            throw new DirveVideoException(CameraErrorCode.OPEN_CAMERA_ERROR);
        }
        ret = Ffmpeg.init(rearVideoConfigParam.getWidth(), rearVideoConfigParam.getHeight(), numBuffer);
        if (ret < 0) {
            log.error("Ffmpeg.init错误");
            throw new DirveVideoException(CameraErrorCode.OPEN_CAMERA_ERROR);
        }
        ret = Ffmpeg.streamon();
        if (ret < 0) {
            log.error("Ffmpeg.streamon错误");
            throw new DirveVideoException(CameraErrorCode.OPEN_CAMERA_ERROR);
        }
        log.info("初始化摄像头成功-------------------------");
        EventBus.getDefault().post(new MsgEvent("初始化摄像头成功-------------------------"));
        cameraOpenFlag = true;
    }

    public void release() {
        log.info("释放摄像头");
        Ffmpeg.release();
        cameraOpenFlag = false;
    }

    public synchronized void takePhoto() {
        mAtomicInteger.getAndIncrement();
        RearCameraPreview.log.info("开始拍照. 剩余次数:{}", mAtomicInteger.get());

//        log.debug("mAtomicInteger  {}", mAtomicInteger.get());
    }

    public void startPreview() throws DirveVideoException {
//        if (preViewFlag == true) {
//            log.debug("startPreview  0");
//            return;
//        }
//        log.debug("startPreview  1");
//        if (cameraOpenFlag == false) {
//            log.info("摄像头未打开");
//            throw new DirveVideoException(CameraErrorCode.START_PREVIEW_ERROR);
//        }
//        preViewFlag = true;
//        rearCameraPreviewWindow.addToWindow();
    }

    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
    }

    public synchronized void startPreviewHolder() throws DirveVideoException {
        if (mSurfaceHolder != null) {

            if (preViewFlag == true) {
                log.debug("startPreview  0");
                return;
            }
            log.debug("startPreview  1");
            if (cameraOpenFlag == false) {
                log.info("摄像头未打开");
                EventBus.getDefault().post(new MsgEvent("摄像头未打开"));
                throw new DirveVideoException(CameraErrorCode.START_PREVIEW_ERROR);
            }
            preViewFlag = true;

            doPreview(mSurfaceHolder);
        } else {
            log.debug("mSurfaceHolder is null ");
            EventBus.getDefault().post(new MsgEvent("mSurfaceHolder is null"));
        }
    }

    public void stopPreviewHolder() {
        log.info("停止预览-------stopPreviewHolder----");
        preViewFlag = false;
        if (canvas != null) {
            canvas = null;
        }
    }

    public void stopPreview() {
//        log.info("停止预览-----------");
//        rearCameraPreviewWindow.removeFromWindow();
//        preViewFlag = false;
    }

    private void initBuffer() {
        bitmap = Bitmap.createBitmap(1280, 720, Bitmap.Config.RGB_565);
        frameDataBuffer = new byte[1280 * 720 * 2];
        Imagbuf = ByteBuffer.wrap(frameDataBuffer);
    }

    private void releaseBuffer() {
        if (Imagbuf != null) {
            Imagbuf.clear();
            Imagbuf = null;
        }
        frameDataBuffer = null;
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    private void initMatix() {
        matrix = new Matrix();
        float scaleFactor = (float) 480 / 720;
        matrix.setScale(scaleFactor, scaleFactor, 0, 0);
    }

    private void doPreview(SurfaceHolder holder) {
        Thread previewThread = new Thread(() -> {
            try {
                log.info("开始预览--------------------------------------");
                EventBus.getDefault().post(new MsgEvent("开始预览--------------------------------------"));
                initBuffer();
                initMatix();
                long index = 0;
                long startTime;
//                synchronized (holder) {
                while (preViewFlag) {
                    previewFinishFlag = false;
                    v4l2_index = Ffmpeg.dqbufdecode(frameDataBuffer);
                    if (v4l2_index < 0) {
                        preViewFlag = false;
                        previewFinishFlag = true;
                        proDqBugError();
                        break;
                    }
                    try {
//                        log.debug("预览-----ing--------0");
                        canvas = holder.lockCanvas();
                        if (canvas != null) {
//                            Bitmap bitmap = decodeBitmapFromByteArray(frameDataBuffer);
                            bitmap.copyPixelsFromBuffer(Imagbuf);

                            if (mAtomicInteger.get() > 0) {
                                log.info("拍照:准备获取拍照.");

                                mAtomicInteger.getAndDecrement();

                                if (FileUtil.isTFlashCardExists()) {
                                    savePictureAction(bitmap);
                                } else {
                                    log.info("TF卡不存在,无法拍照");
                                }
                            }

//                            log.debug("预览-----ing--------1");
                            //      startTime = System.currentTimeMillis();
                            //    EventBus.getDefault().post(new MsgEvent("预览- " + index++));
//                            canvas.drawBitmap(bitmap, src, dst, mPaint);
                            //    long nowTime = System.currentTimeMillis();
                            //   EventBus.getDefault().post(new MsgEvent("预览- " + (nowTime - startTime)));

                            canvas.drawBitmap(bitmap, matrix, mPaint);

//                                bitmap.recycle();
//                                bitmap = null;
                            Imagbuf.clear();
                        }
                    } catch (Exception e) {
                        log.error("error", e);
                    } finally {
                        if (canvas != null) {
                            holder.unlockCanvasAndPost(canvas);
                        }
                        Imagbuf.clear();
                    }

                    //2016-7-1
                    if (Ffmpeg.qbuf(v4l2_index) < 0) {
                        preViewFlag = false;
                        previewFinishFlag = true;
                        proDqBugError();
                        break;
                    }
                }
//                }
                log.info("预览结束------------------------------------------");
                previewFinishFlag = true;
                preViewFlag = false;
                releaseBuffer();
//                release();
            } catch (Exception e) {
                log.error("异常", e);
                preViewFlag = false;
                previewFinishFlag = true;
                releaseBuffer();
//                release();
                if (rearCameraListener != null) {
                    rearCameraListener.onError(RearCameraListenerMessage.PREVIEW_ERROR);
                }
            }
        });
        previewThread.setPriority(Thread.MAX_PRIORITY);
        previewThread.start();
    }

    private void proDqBugError() {
        log.error("Ffmpeg.dqbuf error 错误");
        if (rearCameraListener != null) {
            rearCameraListener.onError(RearCameraListenerMessage.PREVIEW_ERROR);//重新开启
        }
    }

    private Bitmap decodeBitmapFromByteArray(byte[] bitmapDataBuffer) {
        // return BitmapFactory.decodeByteArray(bitmapDataBuffer, 0, v4l2value.byteused);
        return null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        log.debug("RearCameraPreview surfaceCreated创建");
//        doPreview(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        log.debug("RearCameraPreview  surfaceChanged改变  format = {}  w = {}  h=  {}", format, width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        log.debug("RearCameraPreview surfaceDestroyed销毁");
        if (canvas != null) {
            canvas = null;
        }
    }


    public void setBackButtonOnClickListener(View.OnClickListener onClickListener) {
        rearCameraPreviewWindow.setBackButtonOnClickListener(onClickListener);
    }

    public void setRearCameraListener(RearCameraListener rearCameraListener) {
        this.rearCameraListener = rearCameraListener;
    }

    public void setBackButtonVisibleState(boolean visibleState) {
        rearCameraPreviewWindow.setBackButtonVisibleState(visibleState);
    }

    public boolean detectCamera() {
        return FileUtil.detectFileExist("/dev/" + rearVideoConfigParam.getVideoDevice());
    }

    public boolean isPreviewEnable() {
        return previewEnable;
    }

    public void setPreviewEnable(boolean previewEnable) {
        this.previewEnable = previewEnable;
    }

    public void removeFromWindow() {
        rearCameraPreviewWindow.removeFromWindow();
    }

    public boolean isPreviewIng() {
        return preViewFlag;
    }
}
