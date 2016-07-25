package com.dudu.commonlib.utils.fastdfs;

import android.content.Context;

import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.commonlib.utils.File.FileUtilsOld;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dengjun on 2016/4/22.
 * Description :
 */
public class FastDfsTools {
    public static final String FDFS_CLIEND_NAME = "fdfs_client.conf";
    public static final String NODOGSPLASH_NAME = "nodogsplash";

    public static String copyFastDfsConfig(Context context) throws IOException {
        File dirFile = new File(FileUtilsOld.getExternalStorageDirectory(), NODOGSPLASH_NAME);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        File fdfsFile = new File(dirFile.getPath(), FDFS_CLIEND_NAME);
        if (!fdfsFile.exists()) {
            fdfsFile.createNewFile();
        }

        InputStream isAsset = context.getAssets().open(FDFS_CLIEND_NAME);
        if (FileUtil.copyFileToSd(isAsset, fdfsFile)){
            return fdfsFile.getAbsolutePath();
        }else {
            return null;
        }
    }
}
