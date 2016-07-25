package com.dudu.rest.service;

import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.TextVerify;
import com.dudu.commonlib.utils.fastdfs.FastDfsTools;
import com.dudu.fdfs.common.MyException;
import com.dudu.fdfs.fastdfs.FileProcessUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/3/22.
 */
public class DownloadFileService {

    private static Logger logger = LoggerFactory.getLogger(DownloadFileService.class);

    public static Observable<File> downloadFile(final String group, final String url, String path, String fileName) {
        logger.debug("downloadFile: group " + group + " url " + url + " path " + path + " fileName " + fileName);
        return Observable
                .create(new Observable.OnSubscribe<File>() {
                    @Override
                    public void call(Subscriber<? super File> subscriber) {
                        logger.debug("Observable.create");
                        if (!TextVerify.isEmpty(path)) {
                            int result = 0;
                            try {
                                /***
                                 * 参数一：网络请求的路径
                                 * 参数二:服务器上压缩文件的名字
                                 * 参数三：本地存放的路径
                                 * 参数四：本地存放的重命名
                                 **/
                                result = FileProcessUtil
                                        .getInstance(FastDfsTools.copyFastDfsConfig(CommonLib.getInstance().getContext()))
                                        .downloadFile(group, url, path, fileName);
                                logger.debug("downloadFile result" + result);
                            } catch (IOException e) {
                                logger.error("refreshPortal", e);
                                subscriber.onError(e);
                            } catch (MyException e) {
                                logger.error("refreshPortal", e);
                                subscriber.onError(e);
                            }
                            logger.debug("下载portal文件结果：{}", result);
                            if (result == 0) {
                                //如果返回的结果为0的话，则下载成功
                                File downloadedFile = new File(path, fileName);
                                if (downloadedFile.exists()) {
                                    //解压文件
                                    subscriber.onNext(downloadedFile);
                                    return;
                                }
                                subscriber.onError(new NullPointerException());
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());


    }
}
