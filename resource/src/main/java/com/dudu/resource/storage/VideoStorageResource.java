package com.dudu.resource.storage;

import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.resource.resource.SyncAbstactResoucre;
import com.dudu.resource.storage.utils.VideoSizeCalculate;
import com.dudu.resource.storage.utils.VideoUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;


/**
 * Created by dengjun on 2016/4/14.
 * Description :
 */
public class VideoStorageResource extends SyncAbstactResoucre {
    private static final double availbleScale = 0.7;
    private float tfCardSpace;
    private double freeSpace;

    private VideoSizeCalculate videoSizeCalculate = new VideoSizeCalculate();

    private Logger log = LoggerFactory.getLogger("video.VideoStorage");

    public static String format(double mbSize) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(mbSize);
    }

    @Override
    protected void initResource() {
        log.debug("初始化VideoStorageResource");
        tfCardSpace = FileUtil.getTFlashCardSpaceMbFloat();
        freeSpace = tfCardSpace * availbleScale;
        log.debug("磁盘空间：{}MB，录像存储最大可用空间 : {}MB", tfCardSpace, freeSpace);
    }

    @Override
    protected void releaseResource() {

    }

    public void guadSpace() {
        if (!FileUtil.isTFlashCardExists()) {
            log.debug("T卡不存在,停止整理.");
            return;
        }

        float allVideoTotalSizeMb = videoSizeCalculate.getAllVideoTotalSizeMb();
        if (allVideoTotalSizeMb < 100) {//总共只剩下3个视频了,不清理(MB)
            log.debug("视频太少了,停止清理");
            return;
        }

        for (int i = 0; needClean() && i < 10; i++) {
            VideoUtils.deleteOldestVideo();
        }

//        tfCardSpace = FileUtil.getTFlashCardSpaceMbFloat();//TF卡总空间
//        freeSpace = tfCardSpace * availbleScale;//允许剩余空间
//
//        videoSizeCalculate.reset();
//        log.info("录像文件总大小：{}", allVideoTotalSizeMb);
//        for (; freeSpace < allVideoTotalSizeMb; ) {
//            VideoUtils.deleteOldestVideo(true);
//            VideoUtils.deleteOldestVideo(false);
//
//            allVideoTotalSizeMb = videoSizeCalculate.getAllVideoTotalSizeMb();
//            videoSizeCalculate.reset();
//            log.info("录像文件总大小：{}", allVideoTotalSizeMb);
//        }
    }

    public boolean needClean() {
        double tFlashCardFreeSpace = FileUtil.getTFlashCardFreeSpaceMbFloat();//TF卡剩余空间
        double tfCardSpace = FileUtil.getTFlashCardSpaceMbFloat();//TF卡总空间
        log.debug("{} TF空间：{}MB，剩余空间 : {}MB, 必须剩余:{}MB",
                Thread.currentThread().getId(),
                tfCardSpace,
                format(tFlashCardFreeSpace),
                format(tfCardSpace * (1 - availbleScale)));
        return (tfCardSpace * (1 - availbleScale)) > tFlashCardFreeSpace;
    }
}
