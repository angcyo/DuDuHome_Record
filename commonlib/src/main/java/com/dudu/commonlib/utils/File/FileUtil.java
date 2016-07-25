package com.dudu.commonlib.utils.File;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by dengjun on 2016/2/15.
 * Description :
 */
public class FileUtil {
    private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte
    private static Logger log = LoggerFactory.getLogger("commonlib.fileUtil");
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

    public static boolean testNewTfFile(String filePath) {
        File testFile = new File(filePath, UUID.randomUUID().toString());
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


    public static String getFileSizeKb(String absolutePath) {
        File file = new File(absolutePath);
        if (file.exists()) {
            return fileByte2Kb(file.length());
        } else
            return "0";
    }

    public static String getFileSizeMb(String absolutePath) {
        File file = new File(absolutePath);
        if (file.exists()) {
            return fileByte2Mb(file.length());
        } else
            return "0";
    }

    public static double getTFlashCardSpace() {
        if (isTFlashCardExists()) {
            File tfCard = new File(T_FLASH_PATH);
            return tfCard.getTotalSpace();
        } else {
            return 0;
        }
    }

    public static String getTFlashCardSpaceMb() {
        return fileByte2Mb(getTFlashCardSpace());
    }

    public static float getTFlashCardSpaceMbFloat() {
        return Float.valueOf(getTFlashCardSpaceMb());
    }

    /**
     * 获取TF卡剩余空间 MB单位
     */
    public static float getTFlashCardFreeSpaceMbFloat() {
        return Float.valueOf(fileByte2Mb(getTFlashCardFreeSpace()));
    }

    public static double getTFlashCardFreeSpace() {
        File tfCard = new File(T_FLASH_PATH);
        return tfCard.getFreeSpace();
    }

    private static void delectAllFiles(File root) {
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

    public static void clearLostDirFolder() {
        if (isTFlashCardExists()) {
            File root = new File(T_FLASH_PATH, "LOST.DIR");
            if (root != null && root.exists()) {
                delectAllFiles(root);
            }
        }
    }

    public static boolean copyFileAndCreateDir(InputStream assetFile, String path, String name) {
        createDirAtDir(path);
        File file = new File(path, name);
        return copyFileToSd(assetFile, file);
    }

    /**
     * 复制文件
     *
     * @param sdFile :目标文件
     * @params assetFile :被复制文件的输入流
     */
    public static boolean copyFileToSd(InputStream assetFile, File sdFile) {
        boolean flags = false;
        try {
            FileOutputStream fos = new FileOutputStream(sdFile);
            byte[] buffer = new byte[1024];
            int count;
            while ((count = assetFile.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            flags = true;
            fos.close();
            assetFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return flags;
    }

    public static boolean createDirAtDir(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists()) {
            log.info("创建文件夹" + dirPath + "：{}", directory.mkdirs());
        }
        if (directory.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean createDirAtDir(String dir, String dirPath) {
        File sd = Environment.getExternalStorageDirectory();
        String path = sd.getPath() + dir + File.separator + dirPath;
        File directory = new File(path);
        if (!directory.exists()) {
            log.info("创建文件夹：{}", directory.mkdirs());
        }
        if (directory.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static String createSdcardDir(String dir) {
        File file = new File(Environment.getExternalStorageDirectory(), dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }


    public static boolean deleteFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        try {
            File file = new File(filePath);
            if (file.exists() && file.canRead()) {
                String tmpPath = file.getParent() + File.separator + System.currentTimeMillis();
                File tmp = new File(tmpPath);
                Log.e("FileUtil 删除文件 :", filePath + "");
                if (file.renameTo(tmp)) {
                    return tmp.delete();
                } else {
                    return file.delete();
                }
            }
        } catch (Exception e) {
        }

        return false;
    }

    public static boolean detectFileExist(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * 返回sdCard路径
     */
    public static String getSdPath() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(
                Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    /**
     * 判断sdcard是否存在
     */
    public static boolean isSdCard() {
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            return true;
        } else {
            return false;
        }
    }
}
