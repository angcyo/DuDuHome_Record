package com.record.control;

import com.dudu.persistence.realmmodel.picture.PictureEntity;

import java.util.List;

/**
 * Created by robi on 2016-06-06 14:24.
 */
public interface IPhotosListener {
    void onPhotos(List<PictureEntity> photos);
}
