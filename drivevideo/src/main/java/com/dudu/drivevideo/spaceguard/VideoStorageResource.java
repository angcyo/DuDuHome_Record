package com.dudu.drivevideo.spaceguard;

import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.drivevideo.MsgEvent;
import com.dudu.drivevideo.config.RearVideoConfigParam;
import com.dudu.drivevideo.spaceguard.event.VideoSpaceEvent;
import com.dudu.drivevideo.spaceguard.utils.VideoSizeCalculate;
import com.dudu.drivevideo.spaceguard.utils.VideoUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.text.DecimalFormat;

import de.greenrobot.event.EventBus;


/**
 * Created by dengjun on 2016/4/14.
 * Description :
 */
public class VideoStorageResource {
    private static final double availbleScale = 0.8;
    private static Logger log = LoggerFactory.getLogger("video.VideoStorage");
    private float tfCardSpace;
    private double freeSpace;
    private VideoSizeCalculate videoSizeCalculate = new VideoSizeCalculate();

    public static String format(double mbSize) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(mbSize);
    }

    public static boolean needStopRecord() {
        double tFlashCardFreeSpace = FileUtil.getTFlashCardFreeSpaceMbFloat();//TF卡剩余空间
        double tfCardSpace = FileUtil.getTFlashCardSpaceMbFloat();//TF卡总空间
        log.debug("TF空间：{}MB，剩余空间 : {}MB, 最低空间:{}MB",
                format(tfCardSpace),
                format(tFlashCardFreeSpace),
                format(tfCardSpace * 0.1f));
        return (tfCardSpace * 0.1f) > tFlashCardFreeSpace;
    }

    public static boolean needClean() {
        double tFlashCardFreeSpace = FileUtil.getTFlashCardFreeSpaceMbFloat();//TF卡剩余空间
        double tfCardSpace = FileUtil.getTFlashCardSpaceMbFloat();//TF卡总空间
        log.debug("TF空间：{}MB，剩余空间 : {}MB, 必须剩余:{}MB",
                format(tfCardSpace),
                format(tFlashCardFreeSpace),
                format(tfCardSpace * (1 - availbleScale)));
        return (tfCardSpace * (1 - availbleScale)) > tFlashCardFreeSpace;
    }

    /**
     * 删除2天前的tmp文件
     */
    public static void deleteOldTmpFile() {
        File file = new File("/storage/sdcard1/_Record/" + RearVideoConfigParam.VIDEO_STORAGE_PATH_TEMP);
        if (file.exists() && file.isDirectory()) {
            final File[] listFiles = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    final long currentTimeMillis = System.currentTimeMillis();
                    final long lastModified = pathname.lastModified();
                    if (lastModified <= (currentTimeMillis - 2 * 24 * 60 * 60 * 1000)) {
                        //2天之前的文件
                        return true;
                    }
                    return false;
                }
            });

            for (File f : listFiles) {
                try {
                    f.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void initResource() {
        log.debug("初始化VideoStorageResource");
        tfCardSpace = FileUtil.getTFlashCardSpaceMbFloat();
        freeSpace = tfCardSpace * availbleScale;
        log.debug("磁盘空间：{}MB，录像存储最大可用空间 : {}MB", tfCardSpace, freeSpace);
    }

    protected void releaseResource() {

    }

    public void guadSpace() {
        if (!FileUtil.isTFlashCardExists()) {
            log.debug("T卡不存在,停止整理.");
            return;
        }

        //删除2天前的tmp文件
        deleteOldTmpFile();

        float allVideoTotalSizeMb = videoSizeCalculate.getAllVideoTotalSizeMb();
        log.info("视频文件总大小：{} MB", allVideoTotalSizeMb);
        videoSizeCalculate.reset();
        /*if (allVideoTotalSizeMb < 100) {//总共只剩下3个视频了,不清理(MB)
            log.debug("视频太少了,停止清理");
            return;
        }*/

        EventBus.getDefault().post(new MsgEvent("准备清理空间..."));

        for (int i = 0; FileUtil.isTFlashCardExists() && needClean() && i < 50; i++) {
            //判断已经录制的视频是否太少了
            if (SpaceCheck.isRecordFolderTooSmall()) {
                EventBus.getDefault().post(new VideoSpaceEvent("空间太少了,停止录像!"));
                break;
            } else {
                VideoUtils.deleteOldestVideo();
            }
        }

        for (int i = 0; FileUtil.isTFlashCardExists() && needClean() && i < 50; i++) {
            VideoUtils.deleteOldestVideo();
        }

        //此代码暂时不执行，用户视频不主动删除
//        VideoUtils.deleteVideoNoRealmRecord();
//        VideoUtils.deleteFileHaveRecordButNotExist();

        if (FileUtil.isTFlashCardExists() && needStopRecord()) {
            EventBus.getDefault().post(new VideoSpaceEvent("空间不足,停止录像!"));
        }

        FileUtil.clearLostDirFolder();
    }
}
