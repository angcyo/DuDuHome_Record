package com.dudu.persistence.factory;

import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.commonlib.utils.TimeUtils;
import com.dudu.persistence.realmmodel.picture.PictureEntity;
import com.dudu.persistence.realmmodel.picture.PictureEntityRealm;
import com.dudu.persistence.realmmodel.video.VideoEntity;
import com.dudu.persistence.realmmodel.video.VideoEntityRealm;

import java.io.File;
import java.util.ArrayList;

import io.realm.RealmResults;

/**
 * Created by dengjun on 2016/4/12.
 * Description :
 */
public class RealmModelFactory {
    public static VideoEntityRealm createVideoEntityRealm(boolean cameraFlag, String absolutePath){
        VideoEntityRealm videoEntityRealm = new VideoEntityRealm();
        videoEntityRealm.setFileName(new File(absolutePath).getName());
        videoEntityRealm.setCameraFlag(cameraFlag);
        videoEntityRealm.setAbsolutePath(absolutePath);
        videoEntityRealm.setCreateTime(TimeUtils.format(TimeUtils.format1));
        videoEntityRealm.setTimeStamp(System.currentTimeMillis());
        videoEntityRealm.setFileSize(FileUtil.getFileSizeMb(absolutePath));
        videoEntityRealm.setLockFlag(false);
        videoEntityRealm.setUploadState(VideoEntity.UPLOAD_NORMAL_STATE);
        return  videoEntityRealm;
    }

    public static VideoEntityRealm createVideoEntityRealm(boolean cameraFlag, String absolutePath, String thumbnailAbsolutePath){
        VideoEntityRealm videoEntityRealm = new VideoEntityRealm();
        videoEntityRealm.setFileName(new File(absolutePath).getName());
        videoEntityRealm.setCameraFlag(cameraFlag);
        videoEntityRealm.setAbsolutePath(absolutePath);
        videoEntityRealm.setCreateTime(TimeUtils.format(TimeUtils.format1));
        videoEntityRealm.setTimeStamp(System.currentTimeMillis());
        videoEntityRealm.setFileSize(FileUtil.getFileSizeMb(absolutePath));
        videoEntityRealm.setLockFlag(false);

        videoEntityRealm.setThumbnailAbsolutePath(thumbnailAbsolutePath);
        videoEntityRealm.setThumbnailFileSize(FileUtil.getFileSizeMb(thumbnailAbsolutePath));

        videoEntityRealm.setUploadState(VideoEntity.UPLOAD_NORMAL_STATE);
        return  videoEntityRealm;
    }

    public static PictureEntityRealm createPictureEntityRealm(boolean cameraFlag, String absolutePath){
        PictureEntityRealm pictureEntityRealm = new PictureEntityRealm();
        pictureEntityRealm.setFileName(new File(absolutePath).getName());
        pictureEntityRealm.setCameraFlag(cameraFlag);
        pictureEntityRealm.setAbsolutePath(absolutePath);
        pictureEntityRealm.setCreateTime(TimeUtils.format(TimeUtils.format1));
        pictureEntityRealm.setFileSize(FileUtil.getFileSizeMb(absolutePath));
        pictureEntityRealm.setLockFlag(false);
        pictureEntityRealm.setUploadState(VideoEntity.UPLOAD_NORMAL_STATE);
        pictureEntityRealm.setTimeStamp(System.currentTimeMillis());
        return  pictureEntityRealm;
    }

    public static ArrayList<VideoEntity> getVideoEntityListFromRealmResult(RealmResults<VideoEntityRealm> realmResults){
        ArrayList<VideoEntity> videoEntityArrayList = new ArrayList<VideoEntity>();
        for (VideoEntityRealm videoEntityRealm : realmResults){
            videoEntityArrayList.add(new VideoEntity(videoEntityRealm));
        }
        return videoEntityArrayList;
    }

    public static ArrayList<PictureEntity> getPictureEntityListFromRealResuls(RealmResults<PictureEntityRealm> realmResults){
        ArrayList<PictureEntity> pictureEntityArrayList = new ArrayList<PictureEntity>();
        for (PictureEntityRealm pictureEntityRealm: realmResults){
            pictureEntityArrayList.add(new PictureEntity(pictureEntityRealm));
        }
        return pictureEntityArrayList;
    }
}
