package com.dudu.video;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.dudu.android.launcher.LauncherApplication;
import com.dudu.android.launcher.model.volley.MultipartRequest;
import com.dudu.android.launcher.model.volley.MultipartRequestParams;
import com.dudu.android.launcher.utils.DeviceIDUtil;
import com.dudu.android.launcher.utils.FileUtils;
import com.dudu.android.launcher.utils.TimeUtils;
import com.dudu.network.event.UploadVideo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dengjun on 2015/12/15.
 * Description :
 */
public class VideoTransfer {
    private final static String UPLOAD_VIDEO_URL_SUFFIX = "carVideoUpload";
    private final static String CONFIRM_START_VIDEO_URL_SUFFIX = "confirmStartVideo";

    private Context mContext;
    private ScheduledExecutorService sendServiceThreadPool = null;

    /* 标记是否上传视频*/
    private boolean uploadThreadRunFlag = false;
    private Logger log;
//    private String uploadUrl = "http://dudu.gotunnel.org/carVideoUpload";
    //debug

    private String uploadUrl = "http://192.168.0.50:8080/carVideoUpload";

    private RequestQueue queue;
    /* 用于存放文件路径*/
    private List<String > videoFileNameList;
    private String videoFileNameListLock = "videoFileNameListLock";

    private String uploadVideoEventLock = "uploadVideoEventLock";

    private int uploadVideoErrorCount = 0;

    private VideoConfirmRequest videoConfirmRequest;

    public VideoTransfer() {
        mContext = LauncherApplication.getContext();

        queue = Volley.newRequestQueue(mContext);

        sendServiceThreadPool = Executors.newScheduledThreadPool(1);
        log = LoggerFactory.getLogger("video.VideoManager");

        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);

        videoFileNameList = Collections.synchronizedList(new ArrayList<String>());

        videoConfirmRequest = new VideoConfirmRequest( this,mContext);
    }

    /* 处理视频上传事件*/
    public void onEventAsync(UploadVideo uploadVideo){
        log.info("收到并处理UploadVideo事件：isStopUploadVideo：{}, hostController：{}", uploadVideo.getIsStopUploadVideo(), uploadVideo.getHostController());

        synchronized (uploadVideoEventLock) {
            if (uploadVideo.getHostController() != null){
                uploadUrl = uploadVideo.getHostController()+"/"+UPLOAD_VIDEO_URL_SUFFIX;
                log.info("视频上传地址：{}",uploadUrl);
                videoConfirmRequest.setConfirmStartVideoUrl(uploadVideo.getHostController()+ "/"+ CONFIRM_START_VIDEO_URL_SUFFIX);
            }

            stopUploadThread();//收到事件如果上传线程在运行先停掉
            if (uploadVideo.getIsStopUploadVideo().equals("true")){
                log.info("收到停止上传指令");
                videoFileNameList.clear();
                if (uploadThreadRunFlag)
                    stopUpload();
                return;
            }

            if (uploadVideo.getObeId().equals(DeviceIDUtil.getIMEI(mContext))){
                uploadThreadRunFlag = true;
                videoConfirmRequest.confirmStartVideo();
                restartRecordVideo(false);
            }
        }
    }

    /* 用新的间隔参数重启摄像*/
    private void restartRecordVideo(boolean optionFlag){
        log.info("用新的间隔参数重启摄像  optionFlag = {}", optionFlag);
//        VideoManager.getInstance().stopRecord();
        if (optionFlag){
//            VideoManager.getInstance().getVideoConfigParam().resetToDefault();
        }else {
//            VideoManager.getInstance().getVideoConfigParam().setToUploadParam();
        }
        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            log.error("异常：", e);
        }
//        VideoManager.getInstance().startRecord();
    }

    /* 加入录像路径*/
    public void addVideoFileName(String videoFileName){
        if (videoFileName == null)
            return;
        if (uploadThreadRunFlag == true){
            log.debug("新加入文件：{}", videoFileName);
            videoFileNameList.add(videoFileName);
            synchronized (videoFileNameListLock){
                log.debug("新加入文件通知-------");
                videoFileNameListLock.notifyAll();
            }
        }

    }


    private  Thread uploadThread = new Thread(){
        @Override
        public void run() {
            try {
                log.info("视频上传线程开始运行----------");
                while (uploadThreadRunFlag) {
                    String filePath = getNextFilepath();
                    if (filePath != null) {//如果为null，就尝试获取下一个
                        String fileLength = FileUtils.fileByte2Mb(new File(filePath).length());
                        log.debug("上传视频  长度：{} M，文件名：{}", fileLength, filePath);

                        if (Float.valueOf(fileLength) < 0.2 || Float.valueOf(fileLength) > 4) {
                            log.info("文件长度 length < 0.2 || length > 4，过滤掉");
                            continue;
                        }

                        doUploadVideo(filePath);

                        log.info("视频上传等待响应");
                        synchronized (uploadThread) {
                            uploadThread.wait();
                            log.info("视频上传等待----结束");
                        }
                    }
                }
            } catch (Exception e) {
                log.error("异常：", e);
            }
        }
    };

    public void uploadVideo(){
        log.info("开启上传线程");
        if (sendServiceThreadPool == null){
            sendServiceThreadPool = Executors.newScheduledThreadPool(1);
        }
        sendServiceThreadPool.schedule(uploadThread, 1, TimeUnit.SECONDS);
    }

    private void stopUploadThread(){
        if (sendServiceThreadPool != null && !sendServiceThreadPool.isShutdown()){
            try {
                sendServiceThreadPool.shutdown();
                sendServiceThreadPool = null;
            } catch (Exception e) {
                log.error("异常：", e);
            }
        }
    }


    private String getNextFilepath(){
        try {
            if (!videoFileNameList.isEmpty()){
                log.debug("获取到下一个视频文件地址");
                synchronized (videoFileNameListLock){
                    if (videoFileNameList.isEmpty())
                        return null;
                    return videoFileNameList.remove(0);
                }
            }else {
                synchronized (videoFileNameListLock){
                    log.debug("videoFileNameList 没有数据，等待有数据");
                    videoFileNameListLock.wait();
                    log.debug("videoFileNameList 没有数据，等待结束");
                    if (videoFileNameList.isEmpty())
                        return null;
                    return videoFileNameList.remove(0);
                }
            }
        } catch (Exception e) {
            log.error("异常：", e);
            return null;
        }
    }

    private void doUploadVideo(String videoFileName){
        File videoFileToUpload = new File(videoFileName);

        MultipartRequestParams multiPartParams = new MultipartRequestParams();
        multiPartParams.put("upload_video", videoFileToUpload, videoFileToUpload.getName());
        multiPartParams.put("obeId", DeviceIDUtil.getIMEI(mContext));
        MultipartRequest multipartRequest = new MultipartRequest(Request.Method.POST, multiPartParams, uploadUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        log.info("上传视频文件响应信息：{}", response);
                        log.debug("上传结束时间：{}", TimeUtils.format(TimeUtils.format1));
                        proUploadResponseInfo(response);

                        if (uploadVideoErrorCount >= 1){
                            uploadVideoErrorCount--;
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            log.error("上传视频文件错误响应：", error);
                            uploadVideoErrorCount++;
                            if (uploadVideoErrorCount >=5){
                                log.info("累计上传错误次数达到：{} 次，停止实时视频上传", uploadVideoErrorCount);
                                stopUpload();
                                return;
                            }
                            goOnUpload();
                        } catch (Exception e) {
                            log.error("异常：", e);
                        }
                    }
                });
        log.debug("上传开始时间：{}", TimeUtils.format(TimeUtils.format1));
        queue.add(multipartRequest);
    }

    private void proUploadResponseInfo(String response){
        if (response == null)
            return;
        try {
            JSONObject responseJsonObject = new JSONObject(response);
            String resultCode = responseJsonObject.getString("resultCode");
            String method = responseJsonObject.getString("method");
            String isContinueRecordVideo = responseJsonObject.getString("isContinueRecordVideo");
            String msg = responseJsonObject.getString("msg");
            if (resultCode.equals("200")){
                if (isContinueRecordVideo.equals("true")){
                    goOnUpload();
                }else {
                    stopUpload();
                }
            }else {
                log.info("上传响应 400");
            }
        } catch (JSONException e) {
            log.error("异常：{}", e);
        }catch (Exception e){
            log.error("异常：{}", e);
        }
    }


    private void goOnUpload(){
        log.info("继续上传视频");
        synchronized (uploadThread) {
            uploadThread.notifyAll();
        }
        /*synchronized (videoFileNameList) {
            videoFileNameList.notifyAll();
        }*/
    }

    private void stopUpload(){
        log.info("结束上传视频");
        uploadThreadRunFlag = false;
        synchronized (uploadThread) {
            uploadThread.notifyAll();
        }
        synchronized (videoFileNameList) {
            videoFileNameList.notifyAll();
        }

        videoFileNameList.clear();

        stopUploadThread();

        restartRecordVideo(true);
    }

    public void release(){
        EventBus.getDefault().unregister(this);
    }
}
