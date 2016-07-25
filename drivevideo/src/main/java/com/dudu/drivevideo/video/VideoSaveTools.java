package com.dudu.drivevideo.video;

import android.text.TextUtils;

import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.commonlib.utils.image.ImageUtils;
import com.dudu.drivevideo.config.FrontVideoConfigParam;
import com.dudu.drivevideo.utils.TimeUtils;
import com.dudu.persistence.factory.RealmCallFactory;
import com.dudu.persistence.realm.RealmCallBack;
import com.dudu.persistence.realmmodel.video.VideoEntity;
import com.dudu.persistence.realmmodel.video.VideoEntityRealm;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by dengjun on 2016/4/25.
 * Description :
 */
public class VideoSaveTools {
    private static Logger log = LoggerFactory.getLogger("video.frontdrivevideo");
    private static String lastSaveFilePath = "-";

    public static void saveCurVideoInfo(String fileAbPath) {
        String filePath = fileAbPath;
        if (TextUtils.isEmpty(filePath) || lastSaveFilePath.equalsIgnoreCase(filePath) || !new File(filePath).exists())
            return;

        lastSaveFilePath = filePath;
        if (deleteSmallVideoFile(filePath)) {
            return;
        }

        new Thread(() -> {
            File file = new File(filePath);
            try {
                if (file.exists()) {
                    String curVideoThumbnailPath = ImageUtils.generateThumbnailFromVideo(filePath, generateCurVideoThumbnailPath());

                    RealmCallFactory.saveVideoInfo(true, filePath, curVideoThumbnailPath, new RealmCallBack<VideoEntityRealm, Exception>() {
                        @Override
                        public void onRealm(VideoEntityRealm result) {
                            if (result != null) {
                                log.debug("加入视频文件：{}", new Gson().toJson(new VideoEntity(result)));
                            }
                        }

                        @Override
                        public void onError(Exception error) {
                        }
                    });
                }
            } catch (Exception e) {
                log.error("异常", e);
                if (file.exists()) {
                    log.info("删除文件返回结果：{}", file.delete());
                }
            }
        }).start();

    }

    private static boolean deleteSmallVideoFile(String videoAbsolutePath) {
        File file = new File(videoAbsolutePath);
        if (!file.exists())
            return true;
        float fileLenth = Float.valueOf(FileUtil.getFileSizeMb(videoAbsolutePath));
        log.debug("当前文件大小：{} MB", fileLenth);
        if (fileLenth < 2.0) {
            log.info("录像文件大小小于2M，直接删除，不保存数据和生成缩略图");

            if (file.exists()) {
                log.info("删除文件返回结果：{}", com.dudu.drivevideo.utils.FileUtil.deleteFile(videoAbsolutePath));
            }
            return true;
        } else {
            return false;
        }
    }

    public static String generateCurVideoThumbnailPath() {
        return com.dudu.drivevideo.utils.FileUtil.getTFlashCardDirFile("/dudu", FrontVideoConfigParam.VIDEO_THUMBNAIL_STORAGE_PATH).getAbsolutePath()
                + File.separator + TimeUtils.format(TimeUtils.format6) + ".png";
    }

    public static String generateCurVideoName() {
        return com.dudu.drivevideo.utils.FileUtil.getTFlashCardDirFile("/dudu", FrontVideoConfigParam.VIDEO_STORAGE_PATH).getAbsolutePath()
                + File.separator + TimeUtils.format(TimeUtils.format6) + ".mp4";
    }


    public static void deleteCurVideo(String fileAbPath) {
        String filePath = fileAbPath;
        Observable
                .timer(0, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(l -> {
                    File file = new File(filePath);
                    if (file.exists()) {
                        log.info("删除当前录像文件,返回结果：{}", file.delete());
                    }
                }, throwable -> {
                    log.error("异常", throwable);
                });
    }
}
