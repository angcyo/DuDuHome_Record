package com.dudu.workflow.log;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.dudu.commonlib.CommonLib;
import com.dudu.commonlib.utils.DeviceIDUtil;
import com.dudu.commonlib.utils.File.FileUtilsOld;
import com.dudu.commonlib.utils.http.volley.MultipartRequest;
import com.dudu.commonlib.utils.http.volley.MultipartRequestParams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by Administrator on 2015/11/16.
 */
public class SendLogs {
    private RequestQueue queue;
    private static SendLogs instance = null;
    private static final String DUDU_FOLDER = "/sdcard/dudu";
    private static final String CRASH_FOLDER = "/sdcard/dudu/crash";
    private static final String LOGBACK_FOLDER = "/sdcard/logback";
    private static final String TMP_FOLDER = "/sdcard/dudu/tmp";
    public static final String LOGS_NAME = "logs.zip";

    private static Logger log;

    public SendLogs() {
        log = LoggerFactory.getLogger("network.logs");
    }


    public void uploadLog(String url){
        log.info("收到日志上传事件 上传地址：{}", url);
        queue = Volley.newRequestQueue(CommonLib.getInstance().getContext());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    log.info("日志上传-----------");
                    File dirfile = new File(DUDU_FOLDER, "crash");
                    if (dirfile.exists()) {
                        FileUtilsOld.copyFolder(CRASH_FOLDER, LOGBACK_FOLDER);
                    }
                    try {
                        File dirFile = new File(DUDU_FOLDER, "tmp");
                        if (!dirFile.exists()) {
                            dirFile.mkdirs();
                        }
                        File logZip = new File(TMP_FOLDER + "/" + LOGS_NAME);
                        if (logZip.exists()) {
                            log.info("日志压缩文件存在，删除后再上传");
                            logZip.delete();
                        }
                        FileUtilsOld.zipFolder(LOGBACK_FOLDER, TMP_FOLDER + "/" + LOGS_NAME);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    File file = new File(TMP_FOLDER, LOGS_NAME);
                    MultipartRequestParams multiPartParams = new MultipartRequestParams();
                    multiPartParams.put("upload_logs", file, LOGS_NAME);
                    multiPartParams.put("obeId", DeviceIDUtil.getIMEI(CommonLib.getInstance().getContext()));
                    MultipartRequest multipartRequest = new MultipartRequest
                            (Request.Method.POST, multiPartParams, url, new Response.Listener<String>() {

                                @Override
                                public void onResponse(String response) {
                                    log.info("日志上传响应信息：{}", response);
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    log.error("日志上传错误响应：{}", error.toString());
                                }
                            });
                    queue.add(multipartRequest);
                } catch (Exception e) {
                    log.error("异常 {}", e);
                }
            }
        }).start();
    }
}
