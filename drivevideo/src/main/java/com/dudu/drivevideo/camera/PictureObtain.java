package com.dudu.drivevideo.camera;

import android.hardware.Camera;

import com.dudu.commonlib.utils.image.ImageUtils;
import com.dudu.drivevideo.config.FrontVideoConfigParam;
import com.dudu.drivevideo.utils.FileUtil;
import com.dudu.drivevideo.utils.TimeUtils;
import com.dudu.persistence.factory.RealmCallFactory;
import com.dudu.persistence.realm.RealmCallBack;
import com.dudu.persistence.realmmodel.picture.PictureEntity;
import com.dudu.persistence.realmmodel.picture.PictureEntityRealm;
import com.google.gson.Gson;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by dengjun on 2016/2/19.
 * Description :
 */
public class PictureObtain implements Camera.PictureCallback {
    Logger log = LoggerFactory.getLogger("video.frontdrivevideo");
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        savePicture(data);
    }


    public void savePicture(byte[] pictureData){
        Observable
                .timer(0, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe(l->{
                    if (com.dudu.commonlib.utils.File.FileUtil.isTFlashCardExists()){
                        savePictureAction(pictureData);
                    }
                }, throwable -> {
                    log.error("异常", throwable);
                });
    }

    private void savePictureAction(byte[] pictureData){
        String pictureAbApth = generatePicturePath();
        log.info("拍摄照片：{}", pictureAbApth);
        ImageUtils.savePictureData(pictureData, new File(pictureAbApth));
        if (new File(pictureAbApth).exists()){
            RealmCallFactory.savePictureInfo(true, pictureAbApth, new RealmCallBack<PictureEntityRealm, Exception>() {
                @Override
                public void onRealm(PictureEntityRealm result) {
                    log.debug("保存的照片信息：{}", new Gson().toJson(new PictureEntity(result)));
                }

                @Override
                public void onError(Exception error) {

                }
            });
        }
    }

    public static String generatePicturePath() {
        return FileUtil.getTFlashCardDirFile("/dudu", FrontVideoConfigParam.VIDEO_PICTURE_STORAGE_PATH).getAbsolutePath()
                + File.separator + TimeUtils.format(TimeUtils.format5) + ".jpg";
    }
}
