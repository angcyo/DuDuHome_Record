package com.record.control;

import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.persistence.factory.RRealm;
import com.dudu.persistence.factory.RealmModelFactory;
import com.dudu.persistence.realmmodel.picture.PictureEntity;
import com.dudu.persistence.realmmodel.picture.PictureEntityRealm;
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
public class PhotosImpl implements IPhotos {

    PhotoListComparator mPhotoListComparator;
    private List<PictureEntity> mPictureEntities;
    private IPhotosListener mIPhotosListener;
    private Logger log = LoggerFactory.getLogger("photo.PhotosImpl");

    public PhotosImpl() {
        mPictureEntities = new ArrayList<>();
        mPhotoListComparator = new PhotoListComparator();
    }

    private static void filterList(List<PictureEntity> list) {
        for (int i = 0; i < list.size(); i++) {
            if (!FileUtil.isFileExist(list.get(i).getAbsolutePath())) {
                list.remove(i);
                filterList(list);
                return;
            }
        }
    }

    @Override
    public List<PictureEntity> getPhotos(IPhotosListener photosListener) {
        setIPhotosListener(photosListener);
        queryPhotos();
        return mPictureEntities;
    }

    @Override
    public void deletePhoto(PictureEntity entity) {
        Rx.base(s -> {
            RRealm.tran(realm -> {
                RealmResults<PictureEntityRealm> all = realm.where(PictureEntityRealm.class).equalTo("timeStamp", entity.getTimeStamp()).findAll();
                for (int i = 0; i < all.size(); i++) {
                    FileUtil.deleteFile(all.get(i).getAbsolutePath());
                }
                all.deleteAllFromRealm();
            });
            return null;
        });
    }

    @Override
    public void lockPhoto(PictureEntity entity) {
        Rx.base(s -> {
            RRealm.tran(realm -> {
                RealmResults<PictureEntityRealm> all = realm.where(PictureEntityRealm.class).equalTo("timeStamp", entity.getTimeStamp()).findAll();
                for (int i = 0; i < all.size(); i++) {
                    all.get(i).setLockFlag(!all.get(i).isLockFlag());
                }
            });
            return null;
        });
    }

    public void setIPhotosListener(IPhotosListener IPhotosListener) {
        mIPhotosListener = IPhotosListener;
    }

    private void queryPhotos() {
        Rx.base(s -> {
            RRealm.operate(realm -> {
                RealmResults<PictureEntityRealm> pictureEntityRealms = realm.where(PictureEntityRealm.class).findAll();
                mPictureEntities = RealmModelFactory.getPictureEntityListFromRealResuls(pictureEntityRealms);

                Collections.sort(mPictureEntities, mPhotoListComparator);
                int size = mPictureEntities.size();
                filterList(mPictureEntities);
                int size2 = mPictureEntities.size();
                log.info("获取到图片数量:{} 过滤不存在的图片数量:{}", size, size - size2);
                for (int i = 0; i < mPictureEntities.size(); i++) {
                    mPictureEntities.get(i).itemPosition = i;
                }

            });
            return mPictureEntities;
        }, pictureEntities -> {
            if (mIPhotosListener != null) {
                mIPhotosListener.onPhotos(mPictureEntities);
            }
        });
    }

    static class PhotoListComparator implements Comparator<PictureEntity> {

        @Override
        public int compare(PictureEntity lhs, PictureEntity rhs) {
            return (int) (rhs.getTimeStamp() - lhs.getTimeStamp());
        }
    }
}
