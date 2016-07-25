package com.dudu.drivevideo.frontcamera.camera;

import android.graphics.Bitmap;

import com.blur.SoundPlayManager;
import com.dudu.commonlib.utils.image.ImageUtils;
import com.dudu.drivevideo.config.FrontVideoConfigParam;
import com.dudu.drivevideo.utils.FileUtil;
import com.dudu.drivevideo.utils.TimeUtils;
import com.dudu.persistence.factory.RealmCallFactory;
import com.dudu.persistence.realm.RealmCallBack;
import com.dudu.persistence.realmmodel.picture.PictureEntity;
import com.dudu.persistence.realmmodel.picture.PictureEntityRealm;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by robi on 2016-05-31 18:35.
 */
public class PhotoObtain {
    static Logger log = LoggerFactory.getLogger("video.photoobtain");
    private FrontCamera frontCamera;
    private long takeTimeStamp = 0;

    public PhotoObtain(FrontCamera frontCamera) {
        this.frontCamera = frontCamera;
    }

    public static String generatePicturePath() {
        return FileUtil.getTFlashCardDirFile("/dudu", FrontVideoConfigParam.VIDEO_PICTURE_STORAGE_PATH).getAbsolutePath()
                + File.separator + TimeUtils.format(TimeUtils.format5) + ".jpg";
    }

    private static int[] decodeYUV420SP(int[] textureBuffer, byte[] yuv420sp, int width, int height) {

        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;

                textureBuffer[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) &
                        0xff00) | ((b >> 10) & 0xff);
            }
        }
        return textureBuffer;
    }

    public static void savePicture(Bitmap bitmap) {
        Observable
                .timer(0, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(l -> {
                    if (com.dudu.commonlib.utils.File.FileUtil.isTFlashCardExists()) {
                        savePictureAction(bitmap);
                    }
                }, throwable -> {
                    log.error("异常", throwable);
                });
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

    private static Bitmap toBitmap(int[] rgb, int w, int h) {
        return Bitmap.createBitmap(rgb, w, h, Bitmap.Config.ARGB_8888);
    }

    public void takePicture() {
        if (frontCamera.getCamera() != null && (System.currentTimeMillis() - takeTimeStamp > 800)) {
            SoundPlayManager.play();//拍照声效
            takeTimeStamp = System.currentTimeMillis();
            frontCamera.getCamera().setPreviewCallback((data, camera) -> {
                int w = camera.getParameters().getPreviewSize().width;
                int h = camera.getParameters().getPreviewSize().height;
//                textureBuffer = new int[w * h];
//                synchronized (frontCamera) {
//                    int[] rgb = decodeYUV420SP(data, w, h);
//                    savePicture(toBitmap(rgb, w, h));
//                }
                SaveThread.saveData(data, w, h);
                camera.setPreviewCallback(null);
            });
        }
    }

    static class SaveThread extends Thread {

        static Vector<byte[]> datas = new Vector<>();
        private int[] textureBuffer;
        private int w, h;

        public SaveThread(int w, int h) {
            this.w = w;
            this.h = h;
        }

        static synchronized void saveData(byte[] data, int w, int h) {
            SaveThread saveThread = null;
            if (datas.isEmpty()) {
                datas = new Vector<>();
                saveThread = new SaveThread(w, h);
            }
            datas.add(data);
            if (saveThread != null) {
                saveThread.start();
            }
        }

        @Override
        public void run() {
            byte[] data;
            while (!datas.isEmpty()) {
                data = datas.remove(0);
                textureBuffer = new int[w * h];
                int[] rgb = decodeYUV420SP(textureBuffer, data, w, h);
                if (com.dudu.commonlib.utils.File.FileUtil.isTFlashCardExists()) {
                    savePictureAction(toBitmap(rgb, w, h));
                }
            }
        }
    }
}
