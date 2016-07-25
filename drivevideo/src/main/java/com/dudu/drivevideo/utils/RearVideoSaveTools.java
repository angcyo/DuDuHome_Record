package com.dudu.drivevideo.utils;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.commonlib.utils.image.ImageUtils;
import com.dudu.commonlib.utils.shell.ShellExe;
import com.dudu.drivevideo.MsgEvent;
import com.dudu.drivevideo.config.RearVideoConfigParam;
import com.dudu.persistence.factory.RealmCallFactory;
import com.dudu.persistence.realm.RealmCallBack;
import com.dudu.persistence.realmmodel.picture.PictureEntity;
import com.dudu.persistence.realmmodel.picture.PictureEntityRealm;
import com.dudu.persistence.realmmodel.video.VideoEntity;
import com.dudu.persistence.realmmodel.video.VideoEntityRealm;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by dengjun on 2016/4/25.
 * Description :
 */
public class RearVideoSaveTools {
    public static boolean convertToMp4 = true;//是否转换成mp4文件
    public static boolean deleteH264 = true;//是否删除转换完成的H264文件
    public static String BASE_PATH = "/_Record/";
    private static Logger log = LoggerFactory.getLogger("video.reardrivevideo");

    static {
        try {
            final File file = new File("/storage/sdcard1" + BASE_PATH);
            if (!file.exists()) {
                file.mkdirs();
            }
            final File tipFile = new File(file, "_Record是行车记录文件夹，请勿删除!");
            if (!tipFile.exists()) {
                tipFile.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveCurVideoInfo(String fileAbPath) {
        String filePath = fileAbPath;
        if (TextUtils.isEmpty(filePath) || !new File(filePath).exists())
            return;

        /**删除小视频*/
//        if (deleteSmallVideoFile(filePath)) {
//            return;
//        }
//
//        if (convertToMp4) {
//            //自动转成mp4文件
//        new Thread() {
//            @Override
//            public void run() {
//                File file = new File(filePath);
//                try {
//                    if (file.exists()) {
//                        String videoMp4Path = generateCurVideoNameMp4(getVideoNameWithOutSuffix(filePath));
//                        log.debug("{} 开始转码：{}", filePath, videoMp4Path);
//                        String transcodingReturn = video264ToMp4(filePath, videoMp4Path);
//                        log.debug("转码返回值：{}", transcodingReturn);
//
//                        File mp4File = new File(videoMp4Path);
//                        if (mp4File.exists()) {
//                            String curVideoThumbnailPath = ImageUtils.generateThumbnailFromVideo(videoMp4Path, generateCurVideoThumbnailPath());
//
//                            RealmCallFactory.saveVideoInfo(false, videoMp4Path, curVideoThumbnailPath, new RealmCallBack<VideoEntityRealm, Exception>() {
//                                @Override
//                                public void onRealm(VideoEntityRealm result) {
//                                    if (result != null) {
//                                        log.debug("加入视频文件：{}", new Gson().toJson(new VideoEntity(result)));
//                                    }
//                                }
//
//                                @Override
//                                public void onError(Exception error) {
//                                }
//                            });
//                            if (file.exists()) {
////                                if (deleteH264) {
//                                log.info("保存完毕 删除264文件：{}", file.delete());
////                                } else {
////                                    log.info("保存完毕 准备移动H264文件.");
////                                    moveH264File(filePath);
////                                }
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    log.error("异常", e);
//                    if (file.exists()) {
//                        log.info("删除文件返回结果：{}", file.delete());
//                    }
//                }
//            }
//        }.start();
//        } else {
//            //直接保存h264文件
//            String h264File = moveH264File(filePath);
//            if (!TextUtils.isEmpty(h264File)) {
//                //保存h264文件信息至数据库
//                RealmCallFactory.saveVideoInfo(false, h264File, "", new RealmCallBack<VideoEntityRealm, Exception>() {
//                    @Override
//                    public void onRealm(VideoEntityRealm result) {
//                        if (result != null) {
//                            log.debug("加入视频文件：{}", new Gson().toJson(new VideoEntity(result)));
//                        }
//                    }
//
//                    @Override
//                    public void onError(Exception error) {
//                    }
//                });
//            }
//        }

//        new Thread() {
//            @Override
//            public void run() {
//                final String moveH264File = moveH264File(filePath);
//                File file = new File(moveH264File);
//                try {
//                    if (file.exists()) {
//                        String curVideoThumbnailPath = ImageUtils.generateThumbnailFromVideo(moveH264File, generateCurVideoThumbnailPath());
//                        RealmCallFactory.saveVideoInfo(false, moveH264File, curVideoThumbnailPath, new RealmCallBack<VideoEntityRealm, Exception>() {
//                            @Override
//                            public void onRealm(VideoEntityRealm result) {
//                                if (result != null) {
//                                    log.debug("加入视频文件：{}", new Gson().toJson(new VideoEntity(result)));
//                                }
//                            }
//
//                            @Override
//                            public void onError(Exception error) {
//                            }
//                        });
//                    }
//                } catch (Exception e) {
//                    log.error("异常", e);
//                    if (file.exists()) {
//                        log.info("删除文件返回结果：{}", file.delete());
//                    }
//                }
//            }
//        }.start();

        new Thread() {
            @Override
            public void run() {
                //直接保存
                final String newFile = moveH264File(filePath);
                final File file = new File(newFile);
                EventBus.getDefault().post(new MsgEvent(file.exists() + " 视频移动至:" + newFile));
//                String curVideoThumbnailPath = ImageUtils.generateThumbnailFromVideo(newFile, generateCurVideoThumbnailPath());
                String curVideoThumbnailPath = filePath + ".jpg";
                final String jpgPath = moveJpgFile(curVideoThumbnailPath);
                EventBus.getDefault().post(new MsgEvent("缩略图移动至:" + jpgPath));

                RealmCallFactory.saveVideoInfo(false, newFile, jpgPath, new RealmCallBack<VideoEntityRealm, Exception>() {
                    @Override
                    public void onRealm(VideoEntityRealm result) {
                        String msg;
                        if (result != null) {
                            log.debug("加入视频文件：{}", new Gson().toJson(new VideoEntity(result)));
                            msg = Thread.currentThread().getId() + " " + System.currentTimeMillis() + " 加入视频文件:" + new Gson().toJson(new VideoEntity(result));
                        } else {
                            msg = Thread.currentThread().getId() + " " + System.currentTimeMillis() + " 保存数据库失败.";
                        }

                        EventBus.getDefault().post(new MsgEvent(msg));
                    }

                    @Override
                    public void onError(Exception error) {
                    }
                });
            }
        }.start();

    }

    /**
     * 移动h264文件,到保存的位置
     */
    private static String moveH264File(String h264file) {
        File file = new File(h264file);
        String h264VideoPath = generateCurH264VideoPath(getVideoNameWithOutSuffix(h264file, ".tmp") + ".mp4");
        if (file.exists()) {
            File newFile = new File(h264VideoPath);
            if (file.renameTo(newFile)) {
                return h264VideoPath;
            }
            return "";
        }
        return "";
    }

    /**
     * 移动缩略图到新路径
     */
    private static String moveJpgFile(String jpgFile) {
        File file = new File(jpgFile);
        if (!file.exists()) {
            return "";
        }
        String jpfPath = generateCurVideoThumbnailPath() + file.getName().replace(".tmp", "");
        File newFile = new File(jpfPath);
        if (file.renameTo(newFile)) {
            return jpfPath;
        }
        return "";
    }

    private static boolean deleteSmallVideoFile(String videoAbsolutePath) {
        File file = new File(videoAbsolutePath);
        if (!file.exists())
            return true;
        float fileLenth = Float.valueOf(FileUtil.getFileSizeMb(videoAbsolutePath));
        log.debug("当前文件大小：{} MB", fileLenth);
        if (fileLenth < 3.0) {
            log.info("录像文件大小小于3M，直接删除，不保存数据和生成缩略图");

            //不删除
            if (file.exists()) {
                log.info("删除文件返回结果：{}", file.delete());
            }

            try {
                File tempFile = new File(videoAbsolutePath + ".jpg");
                tempFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        } else {
            return false;
        }
    }

    public static String generateCurVideoThumbnailFullPath() {
        return com.dudu.drivevideo.utils.FileUtil.getTFlashCardDirFile(BASE_PATH, RearVideoConfigParam.VIDEO_THUMBNAIL_STORAGE_PATH).getAbsolutePath()
                + File.separator + TimeUtils.format(TimeUtils.format6) + ".png";
    }

    public static String generateCurVideoThumbnailPath() {
        return com.dudu.drivevideo.utils.FileUtil.getTFlashCardDirFile(BASE_PATH, RearVideoConfigParam.VIDEO_THUMBNAIL_STORAGE_PATH).getAbsolutePath()
                + File.separator;
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


    public static String generateCurVideoNameTmp() {
        return com.dudu.drivevideo.utils.FileUtil.getTFlashCardDirFile(BASE_PATH, RearVideoConfigParam.VIDEO_STORAGE_PATH_TEMP).getAbsolutePath()
                + File.separator + TimeUtils.format(TimeUtils.format6) + ".tmp";
    }

    public static String generateCurVideoNameMp4(String fileName) {
        return com.dudu.drivevideo.utils.FileUtil.getTFlashCardDirFile(BASE_PATH, RearVideoConfigParam.VIDEO_STORAGE_PATH).getAbsolutePath()
                + File.separator + fileName + ".mp4";
    }

    public static String generateCurVideoNameMp4() {
//        return com.dudu.drivevideo.utils.FileUtil.getTFlashCardDirFile("/_Record", RearVideoConfigParam.VIDEO_STORAGE_PATH).getAbsolutePath()
//                + File.separator + TimeUtils.format(TimeUtils.format6) + ".mp4";
        return generateCurVideoNameTmp();
    }

    public static String generateCurH264VideoPath(String fileName) {
        return com.dudu.drivevideo.utils.FileUtil.getTFlashCardDirFile(BASE_PATH, RearVideoConfigParam.VIDEO_STORAGE_PATH).getAbsolutePath()
                + File.separator + fileName;
    }

    private static String getVideoNameWithOutSuffix(String videoPath) {
        File videoFile = new File(videoPath);
        String fileName = videoFile.getName();
        return fileName.split(".h264")[0];
    }

    private static String getVideoNameWithOutSuffix(String videoPath, String suffix) {
        File videoFile = new File(videoPath);
        String fileName = videoFile.getName();
        return fileName.split(suffix)[0];
    }

    private static String video264ToMp4(String filePath264, String filePathMp4) {
//        String cmd = "ffmpeg -f h264 -i " + filePath264 + " -vcodec copy -r 30 " + filePathMp4;
        String cmd = "ffmpeg -i " + filePath264 + " -vcodec copy -y " + filePathMp4;
        return ShellExe.execShellCmd(cmd);
    }

    private static String generateRearCameraPicturePath() {
        return com.dudu.drivevideo.utils.FileUtil.getTFlashCardDirFile(BASE_PATH, RearVideoConfigParam.VIDEO_PICTURE_STORAGE_PATH).getAbsolutePath()
                + File.separator + TimeUtils.format(TimeUtils.format5) + ".jpg";
    }

    public static void savePictureAction(Bitmap bitmap) {
        new Thread(() -> {
            String pictureAbApth = generateRearCameraPicturePath();
            log.info("拍摄照片：{}", pictureAbApth);
            ImageUtils.saveBitmapToJpg(bitmap, pictureAbApth);
            if (new File(pictureAbApth).exists()) {
                RealmCallFactory.savePictureInfo(false, pictureAbApth, new RealmCallBack<PictureEntityRealm, Exception>() {
                    @Override
                    public void onRealm(PictureEntityRealm result) {
                        log.debug("保存的照片信息：{}", new Gson().toJson(new PictureEntity(result)));
                    }

                    @Override
                    public void onError(Exception error) {

                    }
                });
            }
        }).start();
    }
}
