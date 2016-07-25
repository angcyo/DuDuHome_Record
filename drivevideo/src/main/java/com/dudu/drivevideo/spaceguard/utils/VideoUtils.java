package com.dudu.drivevideo.spaceguard.utils;

import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.drivevideo.config.FrontVideoConfigParam;
import com.dudu.drivevideo.config.RearVideoConfigParam;
import com.dudu.persistence.factory.RealmCallFactory;
import com.dudu.persistence.realm.RealmCallBack;
import com.dudu.persistence.realmmodel.video.VideoEntity;
import com.dudu.persistence.realmmodel.video.VideoEntityRealm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by dengjun on 2016/4/14.
 * Description :
 */
public class VideoUtils {

    public static Logger log = LoggerFactory.getLogger("video.VideoStorage");


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
                    log.debug("删除视频文件：{}， 时间：{}，时间戳：{}", result.getFileName(), result.getCreateTime(), result.getTimeStamp());
                }
            }

            @Override
            public void onError(Exception error) {

            }
        });
    }


    public static void deleteOldestVideo(){
        RealmCallFactory.tran(realm -> {
            boolean isDelete = false;//是否删除过

            isDelete = isDelete | deleteVideo(realm, false, true);
            isDelete = isDelete | deleteVideo(realm, false, false);

          /*  if (!isDelete) {
                //锁定的视频,在容量特别紧张的情况下,也要无情的删除
                deleteVideo(realm, true, true);
                deleteVideo(realm, true, false);
            }*/
        });
    }

    private static boolean deleteVideo(Realm realm, boolean isLock, boolean isFace) {
        Number min = realm.where(VideoEntityRealm.class).equalTo("lockFlag", isLock).equalTo("cameraFlag", isFace).min("timeStamp");//
        if (min != null) {
            VideoEntityRealm first = realm.where(VideoEntityRealm.class).equalTo("cameraFlag", isFace).equalTo("timeStamp", min.longValue()).findFirst();
            if (first != null) {
                FileUtil.deleteFile(first.getAbsolutePath());
                FileUtil.deleteFile(first.getThumbnailAbsolutePath());
                log.debug("\n删除视频文件：{}\n缩略图:{}\n时间：{}\n时间戳：{}\n大小:{}MB\n前置：{}  锁定：{}",
                        first.getAbsolutePath(), first.getThumbnailAbsolutePath(),
                        first.getCreateTime(), first.getTimeStamp(), first.getFileSize(), first.isCameraFlag(), first.isLockFlag());
                first.deleteFromRealm();
                return true;
            }
        }
        return false;
    }



    public static float getTotalVideoSize(Realm realm){
        RealmResults<VideoEntityRealm> videoEntityRealms = realm.where(VideoEntityRealm.class).findAll();
        return getAllVideoTotalSizeMb(videoEntityRealms);
    }

    public static float getAllVideoTotalSizeMb(RealmResults<VideoEntityRealm> videoEntityArrayList){
        float allVideoTotalSizeMb = 0;
        for (VideoEntityRealm videoEntityRealm : videoEntityArrayList) {
            allVideoTotalSizeMb += Float.valueOf(videoEntityRealm.getFileSize());
        }
        return allVideoTotalSizeMb;
    }


    public static void  testDeleteVideoNoRealmRecord(){
        new Thread(()->{
            deleteVideoNoRealmRecord();
        }).start();
    }

    /**
     * 删除数据库没记录的视频文件
     * 包括缩列图
     */
    public static void deleteVideoNoRealmRecord(){
        RealmCallFactory.findAllVideo(true, new RealmCallBack<ArrayList<VideoEntity>, Exception>() {
            @Override
            public void onRealm(ArrayList<VideoEntity> result) {
                deleteFontVideoNoRealmRecord(result);
            }

            @Override
            public void onError(Exception error) {

            }
        });

        RealmCallFactory.findAllVideo(false, new RealmCallBack<ArrayList<VideoEntity>, Exception>() {
            @Override
            public void onRealm(ArrayList<VideoEntity> result) {
                deleteRearVideoNoRealmRecord(result);
            }

            @Override
            public void onError(Exception error) {

            }
        });
    }


    private static void deleteFontVideoNoRealmRecord(ArrayList<VideoEntity> videoEntityArrayList){
        deleteFileHaveNoRecordAtList(FrontVideoConfigParam.FRONT_VIDEO_STORAGE_PATH, false, videoEntityArrayList);
        deleteFileHaveNoRecordAtList(FrontVideoConfigParam.FRONT_VIDEO_THUMBNAIL_STORAGE_PATH,true,  videoEntityArrayList);
    }

    private static void deleteRearVideoNoRealmRecord(ArrayList<VideoEntity> videoEntityArrayList){
        deleteFileHaveNoRecordAtList(RearVideoConfigParam.REAR_VIDEO_STORAGE_PATH, false, videoEntityArrayList);
        deleteFileHaveNoRecordAtList(RearVideoConfigParam.REAR_VIDEO_THUMBNAIL_STORAGE_PATH, true, videoEntityArrayList);
    }

    private static void deleteFileHaveNoRecordAtList(String filePath, boolean isThumbnail,ArrayList<VideoEntity> videoEntityArrayList){
        File videoDir = new File(filePath);
        if (videoDir.exists()){
            String[] fileNameArray = videoDir.list();
            for(String fileName: fileNameArray){
                boolean haveFlag = false;
                File file = new File(filePath+"/"+fileName);
                if (System.currentTimeMillis() - file.lastModified() > 10*60*1000){//只对10分钟之前的文件进行处理，防止删除正在录制的视频
                    for (VideoEntity videoEntity : videoEntityArrayList){
                        if (isThumbnail){
                            if (fileName.equals(new File(videoEntity.getThumbnailAbsolutePath()).getName())){
                                haveFlag = true;
                            }
                        }else {
                            if (fileName.equals(videoEntity.getFileName())){
                                haveFlag = true;
                            }
                        }
                    }
                    if (haveFlag == false){
                        log.debug("删除数据库中没有记录的文件：{}", filePath+"/"+fileName);
                        FileUtil.deleteFile(filePath+"/"+fileName);
                    }
                }
            }
        }else {
            log.info("路径不存在，不清理");
        }
    }


    /**
     * 删除数据库中有记录，但是实际文件不存在的数据库记录
     */
    public static void deleteFileHaveRecordButNotExist(){
        RealmCallFactory.tran(realm -> {
            RealmResults<VideoEntityRealm> videoEntityRealms = realm.where(VideoEntityRealm.class).findAll();
            deleteFileHaveRecordButNotExist(videoEntityRealms);
        });
    }

    private static void deleteFileHaveRecordButNotExist(RealmResults<VideoEntityRealm> videoEntityRealms){
        Iterator<VideoEntityRealm> iterator = videoEntityRealms.iterator();
        List<VideoEntityRealm> videoEntityRealmList = new ArrayList<VideoEntityRealm>();
        while (iterator.hasNext()){
            VideoEntityRealm videoEntityRealm = iterator.next();
            File videoFile = new File(videoEntityRealm.getAbsolutePath());
            if (!videoFile.exists()){
                videoEntityRealmList.add(videoEntityRealm);
            }
        }
        for (VideoEntityRealm videoEntityRealm: videoEntityRealmList){
            log.info("删除数据库有记录，实际不存在的视频文件的记录：{}", videoEntityRealm.getAbsolutePath());
            videoEntityRealm.deleteFromRealm();
        }
    }

}
