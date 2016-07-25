package com.dudu.commonlib.utils.File.assets;

import android.content.Context;

import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by dengjun on 2016/3/9.
 * Description :
 */
public class AssetsFileUtil {
    public static void copyAssetsFile(Context context, String assetsFileName, String strOutFileName) throws IOException
    {
        InputStream inputStream;
        OutputStream outputStream = new FileOutputStream(strOutFileName);
        inputStream = context.getAssets().open(assetsFileName);
        byte[] buffer = new byte[1024];
        int length = inputStream.read(buffer);
        while(length > 0)
        {
            outputStream.write(buffer, 0, length);
            length = inputStream.read(buffer);
        }

        outputStream.flush();
        inputStream.close();
        outputStream.close();
    }

    public static boolean isExistFile(Context context, String fileName){
        boolean existFlag = false;
        try {
            String[] names = context.getAssets().list("");
            for (String name: names){
//                LoggerFactory.getLogger("monitor.obdUpdate").debug("file : {}", name);
                if (name.equals(fileName)){
                    existFlag = true;
                    break;
                }
            }
        } catch (IOException e) {
            LoggerFactory.getLogger("commonlib").error("异常：", e);
        }
        return existFlag;
    }
}
