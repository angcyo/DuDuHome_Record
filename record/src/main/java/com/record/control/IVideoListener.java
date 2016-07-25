package com.record.control;

import com.dudu.persistence.realmmodel.video.VideoEntity;

import java.util.List;

/**
 * Created by robi on 2016-06-06 14:24.
 */
public interface IVideoListener {
    void onVideos(List<VideoEntity> videos);
}
