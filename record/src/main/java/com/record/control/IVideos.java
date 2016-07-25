package com.record.control;

import com.dudu.persistence.realmmodel.video.VideoEntity;

import java.util.List;

/**
 * Created by robi on 2016-06-06 14:20.
 */
public interface IVideos {
    List<VideoEntity> getVideos(IVideoListener videoListener);

    void deleteVideo(VideoEntity entity);

    void lockVideo(VideoEntity entity);
}
