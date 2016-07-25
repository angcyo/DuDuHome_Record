package com.record.control;

import com.dudu.persistence.realmmodel.picture.PictureEntity;

import java.util.List;

/**
 * Created by robi on 2016-06-06 14:20.
 */
public interface IPhotos {
    List<PictureEntity> getPhotos(IPhotosListener photosListener);

    void deletePhoto(PictureEntity entity);

    void lockPhoto(PictureEntity entity);
}
