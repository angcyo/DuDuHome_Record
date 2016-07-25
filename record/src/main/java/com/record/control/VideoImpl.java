package com.record.control;

import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.persistence.factory.RRealm;
import com.dudu.persistence.factory.RealmModelFactory;
import com.dudu.persistence.realmmodel.video.VideoEntity;
import com.dudu.persistence.realmmodel.video.VideoEntityRealm;
import com.record.util.Rx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.RealmResults;

/**
 * Created by robi on 2016-06-06 14:22.
 */
public class VideoImpl implements IVideos {

    VideoListComparator mVideoListComparator;
    private List<VideoEntity> mVideoEntities;
    private IVideoListener mIVideoListener;
    private Logger log = LoggerFactory.getLogger("video.VideoImpl");

    public VideoImpl() {
        mVideoEntities = new ArrayList<>();
        mVideoListComparator = new VideoListComparator();
    }

    private static void filterList(List<VideoEntity> list) {
        for (int i = 0; i < list.size(); i++) {
            if (!FileUtil.isFileExist(list.get(i).getAbsolutePath())) {
                list.remove(i);
                filterList(list);
                return;
            }
        }
    }

    public void setIVideoListener(IVideoListener IVideoListener) {
        mIVideoListener = IVideoListener;
    }

    private void queryVideos() {
        Rx.base(s -> {
            RRealm.operate(realm -> {
                RealmResults<VideoEntityRealm> videoEntityRealms = realm.where(VideoEntityRealm.class).findAll();
                mVideoEntities = RealmModelFactory.getVideoEntityListFromRealmResult(videoEntityRealms);

                Collections.sort(mVideoEntities, mVideoListComparator);
                int size = mVideoEntities.size();
                filterList(mVideoEntities);
                int size2 = mVideoEntities.size();
                log.info("获取到图片数量:{} 过滤不存在的图片数量:{}", size, size - size2);
                for (int i = 0; i < mVideoEntities.size(); i++) {
                    mVideoEntities.get(i).setItemPosition(i);
                }

            });
            return mVideoEntities;
        }, pictureEntities -> {
            if (mIVideoListener != null) {
                mIVideoListener.onVideos(mVideoEntities);
            }
        });
    }

    @Override
    public List<VideoEntity> getVideos(IVideoListener videoListener) {
        setIVideoListener(videoListener);
        queryVideos();
        return mVideoEntities;
    }

    @Override
    public void deleteVideo(VideoEntity entity) {
        Rx.base(s -> {
            RRealm.tran(realm -> {
                RealmResults<VideoEntityRealm> all = realm.where(VideoEntityRealm.class).equalTo("timeStamp", entity.getTimeStamp()).findAll();
                for (int i = 0; i < all.size(); i++) {
                    FileUtil.deleteFile(all.get(i).getAbsolutePath());
                    FileUtil.deleteFile(all.get(i).getThumbnailAbsolutePath());
                }
                all.deleteAllFromRealm();
            });
            return null;
        });
    }

    @Override
    public void lockVideo(VideoEntity entity) {
        Rx.base(s -> {
            RRealm.tran(realm -> {
                RealmResults<VideoEntityRealm> all = realm.where(VideoEntityRealm.class).equalTo("timeStamp", entity.getTimeStamp()).findAll();
                for (int i = 0; i < all.size(); i++) {
                    all.get(i).setLockFlag(!all.get(i).isLockFlag());
                }
            });
            return null;
        });
    }

    static class VideoListComparator implements Comparator<VideoEntity> {

        @Override
        public int compare(VideoEntity lhs, VideoEntity rhs) {
            return (int) (rhs.getTimeStamp() - lhs.getTimeStamp());
        }
    }
}
