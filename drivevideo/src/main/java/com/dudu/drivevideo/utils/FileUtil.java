package com.dudu.drivevideo.utils;

import android.text.TextUtils;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dengjun on 2016/2/15.
 * Description :
 */
public class FileUtil {
    private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte
    private static Logger log = LoggerFactory.getLogger("video.drivevideo");
    private static String T_FLASH_PATH = "/storage/sdcard1";

    public static File getTFlashCardDirFile(String parentDirName, String dirName) {
        File dirFile = new File(getStorageDir(parentDirName), dirName);
        if (!dirFile.exists()) {
            boolean mkFlag = dirFile.mkdirs();
            log.debug("getTFlashCardDirFile创建文件夹：{}", mkFlag);
        }
        return dirFile;
    }


    public static File getStorageDir(String dirString) {
        File dir = new File(T_FLASH_PATH, dirString);
        if (!dir.exists()) {
            boolean mkFlag = dir.mkdirs();
            log.debug("getStorageDir创建文件夹：{}", mkFlag);
        }
        return dir;
    }


    public static boolean isTFlashCardExists() {
        return testNewTfFile(T_FLASH_PATH);
    }

    /**
     * 返回T卡剩余空间比例,0-1f
     */
    public static float getSdFreeSpace() {
        double tFlashCardFreeSpace = com.dudu.commonlib.utils.File.FileUtil.getTFlashCardFreeSpaceMbFloat();//TF卡剩余空间
        double tfCardSpace = com.dudu.commonlib.utils.File.FileUtil.getTFlashCardSpaceMbFloat();//TF卡总空间;
        return (float) (tFlashCardFreeSpace / tfCardSpace);
    }

    public static boolean testNewTfFile(String filePath) {
        File testFile = new File(filePath, "testNewFile");
        boolean returnFlag = false;
        if (!testFile.exists()) {
            try {
                if (testFile.createNewFile()) {
                    returnFlag = true;
                    testFile.delete();
                }
            } catch (IOException e) {
                returnFlag = false;
            }
        } else {
            testFile.delete();
            returnFlag = true;
        }
        return returnFlag;
    }

    public static List<String> getDirFileNameList(String dir, String startString) {
        File dirFile = new File(dir);
        List<String> fileNameList = new ArrayList<String>();
        if (dirFile.isDirectory()) {
            File[] fileArray = dirFile.listFiles();
            for (File file : fileArray) {
                if (file.getName().startsWith(startString) && !file.getName().equals("")) {
                    fileNameList.add(file.getName());
                }
            }
        }
        return fileNameList;
    }

    public static String fileByte2Mb(double size) {
        double mbSize = size / 1024 / 1024;
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(mbSize);
    }

    public static String fileByte2Kb(double size) {
        double mbSize = size / 1024;
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(mbSize);
    }

    public static double getTFlashCardSpace() {
        File tfCard = new File(T_FLASH_PATH);
        return tfCard.getTotalSpace() * 0.8;
    }


    public static double getTFlashCardFreeSpace() {
        File tfCard = new File(T_FLASH_PATH);
        return tfCard.getFreeSpace();
    }

    public static void delectAllFiles(File root) {
        File files[] = root.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                    delectAllFiles(f);
                } else {
                    if (f.exists()) { // 判断是否存在
                        try {
                            f.delete();
                        } catch (Exception e) {
                        }
                    }
                }
            }
    }

    public static boolean deleteFile(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            if (file.exists() && file.canRead()) {
                Log.e("FileUtil 删除文件 :", filePath + "");
                String tmpPath = file.getParent() + File.separator + System.currentTimeMillis();
                File tmp = new File(tmpPath);
                if (file.renameTo(tmp)) {
                    return tmp.delete();
                } else {
                    return file.delete();
                }
            }
        }
        return false;
    }

    public static void clearLostDirFolder() {
        if (isTFlashCardExists()) {
            File root = new File(T_FLASH_PATH, "LOST.DIR");
            if (root != null && root.exists()) {
                delectAllFiles(root);
            }
        }
    }
}
