package com.dudu.aios.ui.utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.dudu.android.launcher.model.volley.MultipartRequest;
import com.dudu.android.launcher.model.volley.MultipartRequestParams;
import com.dudu.android.launcher.utils.LogUtils;
import com.dudu.commonlib.CommonLib;

import java.io.File;


/**
 * Created by luo zha on 2016/3/26.
 */
public class UploadFile {

    public static final String url = "http://dudu.gotunnel.org/done/upload/";
    public static final String photoUrl = "uploadImages";
    public static final String videoUrl = "uploadVideos";
    private static final String TAG = "UploadFile";
    private static UploadFile mInstance;
    private static Context mContext;
    private static RequestQueue queue;
    private IUploadFileListener listener;

    private UploadFile() {

    }

    public static UploadFile getInstance(Context context) {
        mContext = context;
        if (mInstance == null) {
            mInstance = new UploadFile();
        }
        queue = Volley.newRequestQueue(context);
        return mInstance;
    }

    public void setListener(IUploadFileListener listener) {
        this.listener = listener;
    }

    public void uploadPhoto(File file, IUploadFileListener listener) {
        setListener(listener);
        uploadFile(file, photoUrl);
    }

    public void uploadVideo(File file, IUploadFileListener listener) {
        setListener(listener);
        uploadFile(file, videoUrl);
    }

    private void uploadFile(File file, String typeUrl) {
        if (file.exists()) {
            LogUtils.v(TAG, "开始上传文件--" + file.getPath());
            new Thread(() -> {
                try {
                    if (listener != null) {
                        listener.start(file.getName());
                    }
                    MultipartRequestParams multiPartParams = new MultipartRequestParams();
                    multiPartParams.put("file", file);
                    LogUtils.v(TAG, "name--" + file.getName());
                    multiPartParams.put("obeId", CommonLib.getInstance().getObeId());
                    multiPartParams.put("fileName", file.getName());
                    MultipartRequest multipartRequest = new MultipartRequest(Request.Method.POST, multiPartParams, url + typeUrl,
                            response -> {
                                LogUtils.v(TAG, "response" + response);
                                if (listener != null) {
                                    listener.success(file.getName());
                                }
                            },
                            error -> {
                                LogUtils.v(TAG, "error:" + error);
                                if (listener != null) {
                                    listener.fail(file.getName());
                                }
                            });
                    queue.add(multipartRequest);
                } catch (Exception e) {
                    LogUtils.e(TAG, "error" + e.toString());
                    if (listener != null) {
                        listener.fail(file.getName());
                    }
                }
            }).start();
        }
    }

    public void cancel() {
        queue.cancelAll(null);
    }

    /**
     * 子线程的回调方法
     */
    public interface IUploadFileListener {

        void start(String name);

        void success(String name);

        void fail(String name);
    }
}
