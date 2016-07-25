package com.dudu.drivevideo.spaceguard;

import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.drivevideo.utils.RearVideoSaveTools;

import java.io.File;

/**
 * 空间检测
 * Created by robi on 2016-06-27 16:39.
 */
public class SpaceCheck {

    public static String TF_PATH = "/storage/sdcard1/";
    public static String TF_FLAG = TF_PATH + "/.record_flag_file";
    public static String TF_RECORD_FAIL_FLAG = TF_PATH + "/.record_fail_file";

    /**
     * 是否可以录制,空间是否足够
     */
    public static boolean canRecord() {
//        return !VideoStorageResource.needStopRecord() && !(VideoStorageResource.needClean() && isRecordFolderTooSmall());

        boolean canRecord = false;
//        if (!VideoStorageResource.needStopRecord()) {
//            //空间大于%1
//            if (!VideoStorageResource.needClean()) {
//                //空间大于20%
//                canRecord = true;
//            } else {
//                if (!isRecordFolderTooSmall()) {
//                    //录像文件夹大于3G
//                    canRecord = true;
//                }
//            }
//        }

        if (!FileUtil.isTFlashCardExists()) {
            return canRecord;
        }

        //T卡总空间
        final double tfCardSpace = FileUtil.getTFlashCardSpace();

        //T卡剩余容量
        final double tFlashCardFreeSpace = FileUtil.getTFlashCardFreeSpace();

        //rec文件夹大小
        final long recordFolderSize = getRecordFolderSize();

        if ((tFlashCardFreeSpace + recordFolderSize) > (3l * 1024 * 1024 * 1024 + tfCardSpace * 0.2)) {
            canRecord = true;
        }

        return canRecord;
    }

    /**
     * 判断T容量是否大于8G
     */
    public static boolean isAbove8G() {
        File tf = new File(TF_PATH);

        //大于5GB ,就认为是8G以上的大容量卡
        if (tf.getTotalSpace() > 5l * 1024 * 1024 * 1024) {
            return true;
        }

        return false;
    }


    /**
     * 判断录像文件夹的大小是否满足可清理的条件, 如果不可清理, 需要停止录像
     */
    public static boolean isRecordFolderTooSmall() {
        if (getRecordFolderSize() < 3l * 1024 * 1024 * 1024) {
            return true;
        }

        return false;
    }

    public static long getRecordFolderSize() {
        File recordFolder = new File(TF_PATH + RearVideoSaveTools.BASE_PATH);
        return getFolderSize(recordFolder);
    }

    /**
     * Is new tf card boolean.
     *
     * @return the boolean
     */
    public static boolean isNewTFCard() {
        File file = new File(TF_FLAG);
        if (file.exists()) {
            return false;
        }

        return true;
    }

    public static boolean isRecordFail() {
        File file = new File(TF_RECORD_FAIL_FLAG);
        if (file.exists()) {
            try {
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        return false;
    }

    public static void createRecordFailFlag() {
        try {
            File file = new File(TF_RECORD_FAIL_FLAG);
            file.mkdir();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建旧卡的标识
     */
    public static void createTFCardFlag() {
        try {
            File file = new File(TF_FLAG);
            file.mkdir();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取文件夹大小 bytes
     *
     * @param file File实例
     * @return long
     */
    public static long getFolderSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //return size/1048576;
        return size;
    }

    public static void test() {
        final boolean above8G = isAbove8G();
        final boolean newTFCard = isNewTFCard();
        final boolean recordFolderSizeOk = isRecordFolderTooSmall();
        createTFCardFlag();
        final boolean newTFCard1 = isNewTFCard();
        final boolean newTFCard2 = isNewTFCard();
    }
}
