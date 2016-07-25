package com.dudu.resource.storage.utils;

import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.persistence.factory.RealmCallFactory;
import com.dudu.persistence.realm.RealmCallBack;
import com.dudu.persistence.realmmodel.video.VideoEntityRealm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import io.realm.Realm;

/**
 * Created by dengjun on 2016/4/14.
 * Description :
 */
public class VideoUtils {

    public static Logger logger = LoggerFactory.getLogger("video.VideoStorage");


    public static void deleteOldestVideo(boolean cameraFlag) {
        RealmCallFactory.removeOneVideo(realmQuery -> {
            Number oldestNumber = realmQuery.min("timeStamp");
            long timeOldest = 0;
            if (oldestNumber != null) {
                timeOldest = realmQuery.min("timeStamp").longValue();
            }
            return (VideoEntityRealm) realmQuery.equalTo("timeStamp", timeOldest).equalTo("cameraFlag", cameraFlag).findFirst();
        }, new RealmCallBack<VideoEntityRealm, Exception>() {
            @Override
            public void onRealm(VideoEntityRealm result) {
                if (result != null) {
                    File fileToDelete = new File(result.getAbsolutePath());
                    if (fileToDelete.exists()) {
                        fileToDelete.delete();
                    }
                    logger.debug("删除视频文件：{}， 时间：{}，时间戳：{}", result.getFileName(), result.getCreateTime(), result.getTimeStamp());
                }
            }

            @Override
            public void onError(Exception error) {

            }
        });
    }

    public static void deleteOldestVideo() {
        RealmCallFactory.tran(realm -> {
            boolean isDelete = false;//是否删除过

            isDelete = isDelete | deleteVideo(realm, false, true);
            isDelete = isDelete | deleteVideo(realm, false, false);

            if (!isDelete) {
                //锁定的视频,在容量特别紧张的情况下,也要无情的删除
                deleteVideo(realm, true, true);
                deleteVideo(realm, true, false);
            }
        });
    }

    private static boolean deleteVideo(Realm realm, boolean isLock, boolean isFace) {
        Number min = realm.where(VideoEntityRealm.class).equalTo("lockFlag", isLock).equalTo("cameraFlag", isFace).min("timeStamp");//
        if (min != null) {
            VideoEntityRealm first = realm.where(VideoEntityRealm.class).equalTo("cameraFlag", isFace).equalTo("timeStamp", min.longValue()).findFirst();
            if (first != null) {
                FileUtil.deleteFile(first.getAbsolutePath());
                FileUtil.deleteFile(first.getThumbnailAbsolutePath());
                logger.debug("\n删除视频文件：{}\n缩略图:{}\n时间：{}\n时间戳：{}\n大小:{}MB\n前置：{}  锁定：{}",
                        first.getAbsolutePath(), first.getThumbnailAbsolutePath(),
                        first.getCreateTime(), first.getTimeStamp(), first.getFileSize(), first.isCameraFlag(), first.isLockFlag());
                first.removeFromRealm();
                return true;
            }
        }
        return false;
    }
}
