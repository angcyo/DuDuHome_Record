package com.dudu.drivevideo.rearcamera.utils;

import com.dudu.commonlib.utils.File.FileUtil;
import com.dudu.drivevideo.config.RearVideoConfigParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dengjun on 2016/5/19.
 * Description :
 */
public class RearVideoDeleteTools {
    private static Logger log = LoggerFactory.getLogger("video.reardrivevideo");

    public static void deleteUnUsed264VideoFile(String curVideoPath){
        try {
            File curVideoFile = new File(curVideoPath);
            if (curVideoFile.exists()){
                doDeleteUnUsed264VideoFile(curVideoFile.getName());
            }
        } catch (Exception e) {
            log.error("异常",e);
        }
    }

    private static void doDeleteUnUsed264VideoFile(String curVideoFileName){
        for (String fileName: getUnUsed264FileList()){
            if (!fileName.equals(curVideoFileName) //只对10分钟之前的文件进行处理，防止删除正在录制的视频
                    && (System.currentTimeMillis() -new File(RearVideoConfigParam.REAR_VIDEO_STORAGE_PATH+"/"+fileName).lastModified() > 10*60*1000)){
                log.info("删除碎片.h264文件：{}", RearVideoConfigParam.REAR_VIDEO_STORAGE_PATH+"/"+fileName);
                FileUtil.deleteFile(RearVideoConfigParam.REAR_VIDEO_STORAGE_PATH+"/"+fileName);
            }
        }
    }

    private static List<String> getUnUsed264FileList(){
        List<String> video264List = new ArrayList<String>();
        File video264dir = new File(RearVideoConfigParam.REAR_VIDEO_STORAGE_PATH);
        if (video264dir.exists()){
            String[] fileNameArray = video264dir.list();
            for (String fileName: fileNameArray){
                if (fileName.endsWith(".h264")){
                    video264List.add(fileName);
                }
            }
        }
        return video264List;
    }
}
