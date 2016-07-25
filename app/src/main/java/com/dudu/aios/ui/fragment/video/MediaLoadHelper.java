package com.dudu.aios.ui.fragment.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.dudu.aios.ui.utils.DuduImageLoader;
import com.dudu.aios.ui.utils.MD5;
import com.dudu.aios.ui.utils.Rx;
import com.dudu.aios.ui.view.DuduUploadBarLayout;
import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.persistence.factory.RealmCallFactory;
import com.dudu.persistence.realm.RealmCallBack;
import com.dudu.persistence.realmmodel.picture.PictureEntity;
import com.dudu.persistence.realmmodel.picture.PictureEntityRealm;
import com.dudu.persistence.realmmodel.video.VideoEntity;
import com.dudu.persistence.realmmodel.video.VideoEntityRealm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by robi on 2016-04-18 15:40.
 */
public class MediaLoadHelper {

    public static final boolean DEBUG = true;
    static final String base_path = "/storage/sdcard1/dudu/";
    static final String back_path = "/storage/sdcard1/dudu/video4/";
    static final String face_path = "/storage/sdcard1/dudu/frontVideo/";
    static final String photos_path = "/storage/sdcard1/dudu/photos/";
    static final String thumbnail_path = "/storage/sdcard1/dudu/thumbnail/";
    static Logger logger = LoggerFactory.getLogger("ui.media");
    public static void getAllPhotos(OnPhotoCallback callback, boolean isFace) {
//        Observable.just(photos_path)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .map(s -> {
//                    File file = new File(s);
//                    List<PhotoItemBean> beans = new ArrayList<>();
//                    for (File f : file.listFiles()) {
//                        if (f.isFile() && isImage(f.getName())) {
//                            PhotoItemBean bean = new PhotoItemBean();
//                            bean.photoPath = f.getAbsolutePath();
//                            beans.add(bean);
//                        }
//                    }
//                    return beans;
//                })
//                .subscribe(callback::onPhotos, throwable -> {
//                    callback.onPhotos(new ArrayList<>());
//                });

        if (com.dudu.drivevideo.utils.FileUtil.isTFlashCardExists()) {
            RealmCallFactory.queryPictureEntityInfoAsync(realmQuery -> realmQuery.equalTo("cameraFlag", isFace).findAllAsync(),
                    new RealmCallBack<ArrayList<PictureEntity>, Exception>() {
                        @Override
                        public void onRealm(ArrayList<PictureEntity> result) {
                            callback.onPhotos(result);
                        }

                        @Override
                        public void onError(Exception error) {
                            callback.onPhotos(new ArrayList<>());
                        }
                    });
        } else {
            callback.onPhotos(new ArrayList<>());
        }
    }

    public static boolean isImage(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }

        if (name.endsWith("png") || name.endsWith("jpeg") || name.endsWith("jpg")) {
            return true;
        }

        return false;
    }

    /**
     * 异步获取所有前置录制的视频
     */
    public static void getAllFaceVideos(OnVideoCallback callback) {
        getVideos(callback, true);
//        Observable.just(face_path)
//                .map(s -> {
//                    File file = new File(s);
//                    List<VideoItemBean> beans = new ArrayList<>();
//                    for (File f : file.listFiles()) {
//                        if (f.isFile()) {
//                            VideoItemBean bean = new VideoItemBean();
//                            bean.videoPath = f.getAbsolutePath();
//                            bean.videoName = f.getName();
////                            bean.videoThumbnailPath=;
//                            beans.add(bean);
//                        }
//                    }
//                    return beans;
//                })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(callback::onVideos);
    }

    /**
     * 异步获取所有后置录制的视频
     */
    public static void getAllBackVideos(OnVideoCallback callback) {
        getVideos(callback, false);
//        Observable.just(back_path)
//                .map(s -> {
//                    File file = new File(s);
//                    List<VideoItemBean> beans = new ArrayList<>();
//                    for (File f : file.listFiles()) {
//                        if (f.isFile()) {
//                            VideoItemBean bean = new VideoItemBean();
//                            bean.videoPath = f.getAbsolutePath();
//                            bean.videoName = f.getName();
//                            //                            bean.videoThumbnailPath=;
//                            beans.add(bean);
//                        }
//                    }
//                    return beans;
//                })
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(callback::onVideos);
    }

    private static void getVideos(OnVideoCallback callback, boolean isFace) {
//        RealmCallFactory.queryVideoEntityInfoAsync(realmQuery -> realmQuery.equalTo("cameraFlag", isFace).findAllAsync(),
//                new RealmCallBack<ArrayList<VideoEntity>, Exception>() {
//                    @Override
//                    public void onRealm(ArrayList<VideoEntity> results) {
//                        if (!results.isEmpty()) {
//                            LoggerFactory.getLogger("video.videoui").debug("加载视频 results size：{}, {}", results.size(), results.get(0).getAbsolutePath());
//                        }
////                List<VideoItemBean> beans = new ArrayList<>();
////                for (VideoEntity entity : results) {
////                    VideoItemBean bean = new VideoItemBean();
////                    bean.videoPath = entity.getAbsolutePath();
////                    bean.videoName = entity.getFileName();
//////                    bean.videoThumbnailPath = entity.getFileName();
////                    beans.add(bean);
////                }
//                        callback.onVideos(results);
//                    }
//
//                    @Override
//                    public void onError(Exception error) {
//                        callback.onVideos(new ArrayList<>());
//                    }
//                });

//        RealmCallFactory.queryVideoAsync(isFace, new RealmCallBack<ArrayList<VideoEntity>, Exception>() {
//            @Override
//            public void onRealm(ArrayList<VideoEntity> result) {
//                callback.onVideos(result);
//            }
//
//            @Override
//            public void onError(Exception error) {
//                callback.onVideos(new ArrayList<>());
//            }
//        });

        if (com.dudu.drivevideo.utils.FileUtil.isTFlashCardExists()) {
            RealmCallFactory.queryVideo(isFace, new RealmCallBack<ArrayList<VideoEntity>, Exception>() {
                @Override
                public void onRealm(ArrayList<VideoEntity> result) {
                    callback.onVideos(result);
                }

                @Override
                public void onError(Exception error) {
                    callback.onVideos(new ArrayList<>());
                }
            });
        } else {
            callback.onVideos(new ArrayList<>());
        }

//        RealmCallFactory.tran(realm -> {
//            RealmResults<VideoEntityRealm> list = realm.where(VideoEntityRealm.class).equalTo("cameraFlag", isFace).findAll();
//            e("查询到前置视频数量-Robi " + list.size());
//        });
    }

    public static void getRangeVideoThumbnail(List<String> videoPathList, List<Integer> posList, OnVideoThumbnailCallback callback) {
        Observable.just(videoPathList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(strings -> {
                    for (String path : videoPathList) {
                        Bitmap bitmap = DuduImageLoader.du().getBitmapFromMemCache(path);
                        if (bitmap == null) {
                            bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
                            DuduImageLoader.du().addBitmapToMemoryCache(path, bitmap);

                        }
                    }
                    return true;
                })
                .subscribe(result -> {
                    callback.onRangeVideoThumbnail(videoPathList, posList);
                }, throwable -> logger.error("getRangeVideoThumbnail", throwable));

    }

    /**
     * 回调缩略图的磁盘路径
     */
    public static void getVideoThumbnail(String videoPath, OnVideoThumbnailCallback callback) {
        Observable.just(videoPath)
                .delay(300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(s -> {
                    if (TextUtils.isEmpty(s)) {
                        return null;
                    }
                    String videoMd5Path = getVideoMd5Path(s);
                    try {
                        if (!new File(videoMd5Path).exists()) {
                            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(s, MediaStore.Video.Thumbnails.MINI_KIND);
                            saveBitmap(bitmap, videoMd5Path);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return videoMd5Path;
                })
                .subscribe(path -> {
                    callback.onVideoThumbnailPath(path);
                }, throwable -> logger.error("display", throwable));

    }

    /**
     * 内存缓存缩略图的对象
     */
    public static void getVideoThumbnail(String videoPath, int pos, OnVideoThumbnailCallback callback) {
        Bitmap b = DuduImageLoader.du().getBitmapFromMemCache(videoPath);
        if (b != null) {
            callback.onVideoThumbnail(pos, b);
        } else {
            Observable.just(videoPath)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(s -> {
                        if (TextUtils.isEmpty(s)) {
                            return null;
                        }
                        Bitmap bitmap = DuduImageLoader.du().getBitmapFromMemCache(s);
                        if (bitmap == null) {
                            bitmap = ThumbnailUtils.createVideoThumbnail(s, MediaStore.Video.Thumbnails.MINI_KIND);
                            DuduImageLoader.du().addBitmapToMemoryCache(s, bitmap);
                        }
                        return bitmap;
                    })
                    .subscribe(bitmap -> {
                        callback.onVideoThumbnail(pos, bitmap);
                    }, throwable -> logger.error("getVideoThumbnail", throwable));

        }
    }

    private static String getVideoMd5Path(String videoPath) {
        StringBuilder name = new StringBuilder();
        name.append(getVideoTempPath());
        name.append(File.separator);
        name.append(MD5.toMD5(videoPath));
        name.append(".png");
        return name.toString();
    }

    private static String getVideoTempPath() {
        return thumbnail_path;
    }

    /**
     * 保存Bitmap到文件
     */
    public static void saveBitmap(Bitmap bmp, String filePath) throws Exception {
        File file = new File(filePath);
        File parentFile = new File(file.getParent());
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
//        file.createNewFile();
        FileOutputStream outputStream = new FileOutputStream(file);
        bmp.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
    }

    /**
     * 上传视频
     */
    public static void uploadPhoto(Context context, final PictureEntity bean, boolean isCancel, FileUploadHelper.IUploadFileListener listener) {
        if (isCancel) {
            FileUploadHelper.cancelPhoto(bean.getTimeStamp());
            updatePhotoState(bean.getTimeStamp(), DuduUploadBarLayout.STATE_NORMAL);
        } else {
            FileUploadHelper.uploadPhoto(context, bean.getTimeStamp(), bean.getAbsolutePath(), listener);
        }
    }

    /**
     * 上传视频
     */
    public static void uploadVideo(Context context, final VideoEntity bean, boolean isCancel, FileUploadHelper.IUploadFileListener listener) {
        if (isCancel) {
            FileUploadHelper.cancelVideo(bean.getTimeStamp());
            updateVideoState(bean.getTimeStamp(), DuduUploadBarLayout.STATE_NORMAL);
        } else {
            FileUploadHelper.uploadVideo(context, bean.getTimeStamp(), bean.getAbsolutePath(), listener);
        }
    }

    public static void updateVideoState(long timeStamp, int upState) {
        RealmCallFactory.tran(realm -> {
            VideoEntityRealm videoEntityRealm = realm.where(VideoEntityRealm.class).equalTo("timeStamp", timeStamp).findFirst();
            int state = VideoEntity.UPLOAD_NORMAL_STATE;
            if (upState == DuduUploadBarLayout.STATE_UPING) {
                state = VideoEntity.UPLOADING_STATE;
            } else if (upState == DuduUploadBarLayout.STATE_NORMAL) {
                state = VideoEntity.UPLOAD_NORMAL_STATE;
            } else if (upState == DuduUploadBarLayout.STATE_FINISH) {
                state = VideoEntity.UPLOAD_SUCCESS_STATE;
            } else if (upState == DuduUploadBarLayout.STATE_FAILD) {
                state = VideoEntity.UPLOAD_FAIL_STATE;
            }
            videoEntityRealm.setUploadState(state);
        });
    }

    public static void updatePhotoState(long timeStamp, int upState) {
        RealmCallFactory.tran(realm -> {
            PictureEntityRealm pictureEntityRealm = realm.where(PictureEntityRealm.class).equalTo("timeStamp", timeStamp).findFirst();
            int state = PictureEntity.UPLOAD_NORMAL_STATE;
            if (upState == DuduUploadBarLayout.STATE_UPING) {
                state = PictureEntity.UPLOADING_STATE;
            } else if (upState == DuduUploadBarLayout.STATE_NORMAL) {
                state = PictureEntity.UPLOAD_NORMAL_STATE;
            } else if (upState == DuduUploadBarLayout.STATE_FINISH) {
                state = PictureEntity.UPLOAD_SUCCESS_STATE;
            } else if (upState == DuduUploadBarLayout.STATE_FAILD) {
                state = PictureEntity.UPLOAD_FAIL_STATE;
            }
            pictureEntityRealm.setUploadState(state);
        });
    }

    /**
     * 删除视频数据库操作
     */
    public static void deleteVideo(VideoEntity bean) {
        Rx.base(bean, videoEntity -> {
            RealmCallFactory.tran(realm -> {
                VideoEntityRealm first = realm.where(VideoEntityRealm.class).equalTo("timeStamp", videoEntity.getTimeStamp())
                        .findFirst();
                if (first != null) {
                    first.removeFromRealm();
                }
            });
            FileUtil.deleteFile(videoEntity.getAbsolutePath());//视频
            FileUtil.deleteFile(videoEntity.getThumbnailAbsolutePath());//视频缩略图
            return null;
        });
    }

    /**
     * 删除图片数据库操作
     */
    public static void deletePhoto(PictureEntity bean) {
        Rx.base(bean, pictureEntity -> {
            RealmCallFactory.tran(realm -> {
                PictureEntityRealm timeStamp = realm.where(PictureEntityRealm.class).equalTo("timeStamp", pictureEntity.getTimeStamp())
                        .findFirst();
                if (timeStamp != null) {
                    timeStamp.removeFromRealm();
                }
            });
            FileUtil.deleteFile(pictureEntity.getAbsolutePath());//图片
            return null;
        });
    }

    public static void deletePhoto(List<PictureEntity> bean, Action1 onNext) {
        Rx.base(bean, pictureEntities -> {
            for (int i = 0; i < pictureEntities.size(); i++) {
                PictureEntity pictureEntity = pictureEntities.get(i);
                RealmCallFactory.tran(realm -> {
                    PictureEntityRealm timeStamp = realm.where(PictureEntityRealm.class).equalTo("timeStamp", pictureEntity.getTimeStamp())
                            .findFirst();
                    if (timeStamp != null) {
                        timeStamp.removeFromRealm();
                    }
                });
                FileUtil.deleteFile(pictureEntity.getAbsolutePath());//图片
            }
            return new Object();
        }, onNext);
    }

    /**
     * 加锁/解锁数据库操作
     */
    public static void lockVideo(VideoEntity bean) {
        RealmCallFactory.tran(realm -> {
            VideoEntityRealm videoEntityRealm = realm.where(VideoEntityRealm.class).equalTo("timeStamp", bean.getTimeStamp()).findFirst();
            if (videoEntityRealm != null)
                videoEntityRealm.setLockFlag(bean.isLockFlag());
        });
    }

    public static void e(String msg) {
        if (DEBUG) {
            Log.e("angcyo-->" + Thread.currentThread().getId(), msg);
        }
    }

    public interface OnVideoCallback {
        void onVideos(List<VideoEntity> beans);
    }

    public interface OnPhotoCallback {
        void onPhotos(List<PictureEntity> beans);
    }

    public interface OnVideoThumbnailCallback {
        void onVideoThumbnailPath(String path);

        void onVideoThumbnail(int position, Bitmap bitmap);

        void onRangeVideoThumbnail(List<String> videoPathList, List<Integer> posList);
    }

//    public static class VideoItemBean {
//        public String videoPath;//视频路径,用于获取缩略图
//        public int uploadState;//上传的状态
//        public boolean isLock;
//        public String videoName;
//        public String videoThumbnailPath;//视频缩略路径
//    }
//
//    public static class PhotoItemBean {
//        public String photoPath;
//        public int uploadState = 0;//上传的状态
//        public int itemPosition = 0;//在Adapter中的位置,用于刷新Recycler
//    }
}
