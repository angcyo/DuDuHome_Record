package com.dudu.monitor.portal;

import android.content.Context;
import android.util.Log;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.File.FileUtilsOld;
import com.dudu.commonlib.utils.File.SharedPreferencesUtil;
import com.dudu.fdfs.fastdfs.FileProcessUtil;
import com.dudu.monitor.portal.constants.PortalContants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lxh on 2015/11/7.
 */
public class PortalUpdate {
    public static final String FDFS_CLIEND_NAME = "fdfs_client.conf";
    public static final String NODOGSPLASH_NAME = "nodogsplash";
    public static final String TEMP_ZIP_FOLDER_NAME = "temp_zip";
    public static final String HTDOCS_FOLDER_NAME = "/htdocs";
    public static final String HTDOCS_ZIP_NAME = "htdocs.zip";
    public static final String TEMP_ZIP_NAME = "temp.zip";
    private static final String TAG = "PortalUpdate";
    private Context mContext;
    private Logger log;


    public PortalUpdate() {

        log = LoggerFactory.getLogger("monitor.PortalManage");
        mContext = CommonLib.getInstance().getContext();
    }


    private void updatePortalVersion(String portalVersion) {
        try {
            SharedPreferencesUtil.putStringValue(CommonLib.getInstance().getContext(), PortalContants.KEY_PORTAL_VERSION,
                    String.valueOf(portalVersion));
        } catch (NumberFormatException e) {
            log.error("异常",e);
        }
    }


    public void refreshPortal(final String group, final String url, final String portalVersion) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int result = 1;
                try {
                    log.info("处理更新portal事件");
                    //嘟嘟相关文件存储的位置
                    File dirFile = new File(FileUtilsOld.getExternalStorageDirectory(), NODOGSPLASH_NAME);
                    if (!dirFile.exists()) {
                        dirFile.mkdirs();
                    }
                    //放置压缩包的路径
                    File zipDirFile = new File(dirFile.getPath(), TEMP_ZIP_FOLDER_NAME);
                    if (!zipDirFile.exists()) {
                        zipDirFile.mkdirs();
                    }
                    //存放解压文件的额目录
                    File desFile = new File(dirFile.getPath(), HTDOCS_FOLDER_NAME);
                    if (!desFile.exists()) {
                        desFile.mkdirs();
                    }
                    File fdfsFile = new File(dirFile.getPath(), FDFS_CLIEND_NAME);
                    if (!fdfsFile.exists()) {
                        fdfsFile.createNewFile();
                    }
                    InputStream isAsset = mContext.getAssets().open(FDFS_CLIEND_NAME);
                    if (FileUtilsOld.copyFileToSd(isAsset, fdfsFile)) {
                        String path = fdfsFile.getAbsolutePath();
                        //请求网络，下载压缩文件到指定路径下
                        /***
                         * 参数一：网络请求的路径
                         * 参数二:服务器上压缩文件的名字
                         * 参数三：本地存放的路径
                         * 参数四：本地存放的重命名
                         * */
                        result = FileProcessUtil.getInstance(path).downloadFile(group, url, zipDirFile.getPath() + "/", TEMP_ZIP_NAME);


                        Log.i("ji", "" + result);
                        log.info("下载portal文件结果：{}",result);
                    }
                    if (result == 0) {
                        //如果返回的结果为0的话，则下载成功
                        File zipPath = new File(zipDirFile.getPath(), TEMP_ZIP_NAME);
                        if (zipPath.exists()) {
                            //解压文件
                            FileUtilsOld.upZipFile(zipPath, dirFile.getPath());

                            updatePortalVersion(portalVersion);
                        }
                    }
                } catch (IOException e) {
                    log.error("异常 {}",e);

                } catch (Exception e) {
                    log.error("异常",e);

                }
            }
        }).start();
    }

    public void release() {

    }
}
