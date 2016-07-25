package com.dudu.aios.ui.fragment.video;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.dudu.android.launcher.model.volley.MultipartRequest;
import com.dudu.android.launcher.model.volley.MultipartRequestParams;
import com.dudu.android.launcher.utils.LogUtils;
import com.dudu.commonlib.CommonLib;

import java.io.File;
import java.util.Vector;

/**
 * Created by robi on 2016-04-20 16:45.
 */
public class FileUploadHelper {
//    public static final String url = "http://dudu.gotunnel.org/done/upload/";
    public static final String url = "http://192.168.0.186/done/upload/";
    public static final String photoUrl = "uploadImages";
    public static final String videoUrl = "uploadVideos";
    private static final String TAG = "FileUploadHelper";
    private static FileUploadHelper fileUploadHelper = new FileUploadHelper();
    private static RequestQueue queue;
    private Vector<VideoTask> videoTasks;
    private Vector<PhotoTask> photoTasks;
    private Object lock = new Object();
    private TaskThread thread = null;
    private IUploadFileListener listener;

    private FileUploadHelper() {
        videoTasks = new Vector<>();
        photoTasks = new Vector<>();
    }

    public static void uploadPhoto(Context context, long timeStamp, String path, IUploadFileListener listener) {
        fileUploadHelper.init(context);
        fileUploadHelper.setListener(listener);
        fileUploadHelper.addPhotoTask(new PhotoTask(timeStamp, path));
    }

    public static void uploadVideo(Context context, long timeStamp, String path, IUploadFileListener listener) {
        fileUploadHelper.init(context);
        fileUploadHelper.setListener(listener);
        fileUploadHelper.addVideoTask(new VideoTask(timeStamp, path));
    }

    public static void cancelPhoto(long timeStamp) {
        fileUploadHelper.deletePhoto(timeStamp);
    }

    public static void cancelVideo(long timeStamp) {
        fileUploadHelper.deleteVideo(timeStamp);
    }

    public void setListener(IUploadFileListener listener) {
        this.listener = listener;
    }

    private void deletePhoto(long timeStamp) {
        for (int i = 0; i < photoTasks.size(); i++) {
            PhotoTask photoTask = photoTasks.get(i);
            if (photoTask.timeStamp == timeStamp) {
                photoTasks.remove(i);
            }
        }
    }

    private void deleteVideo(long timeStamp) {
        for (int i = 0; i < videoTasks.size(); i++) {
            VideoTask task = videoTasks.get(i);
            if (task.timeStamp == timeStamp) {
                videoTasks.remove(i);
            }
        }
    }

    private void init(Context context) {
        if (queue == null) {
            synchronized (lock) {
                if (queue == null) {
                    queue = Volley.newRequestQueue(context);
                }
            }
        }
    }

    private void addVideoTask(VideoTask task) {
        for (VideoTask videoTask : videoTasks) {
            if (videoTask.timeStamp == task.timeStamp) {
                return;
            }
        }
        videoTasks.add(task);
        startThread();
    }

    private void addPhotoTask(PhotoTask task) {
        for (PhotoTask photoTask : photoTasks) {
            if (photoTask.timeStamp == task.timeStamp) {
                return;
            }
        }

        photoTasks.add(task);
        startThread();
    }

    private void startThread() {
        synchronized (lock) {
            if (thread == null) {
                thread = new TaskThread();
                thread.start();
            }
        }
    }

    private void startNext() {
        if (thread != null) {
            thread.next();
        }
    }

    private void uploadFile(BaseTask task, String typeUrl) {
        File file = new File(task.path);
        if (file.exists()) {
            LogUtils.v(TAG, "开始上传文件--" + file.getPath());
            try {
                if (listener != null) {
                    listener.start(task);
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
                                listener.success(task);
                            }
                            startNext();
                        },
                        error -> {
                            LogUtils.v(TAG, "error:" + error);
                            if (listener != null) {
                                listener.fail(task);
                            }
                            startNext();
                        });
                queue.add(multipartRequest);
            } catch (Exception e) {
                LogUtils.e(TAG, "error" + e.toString());
                if (listener != null) {
                    listener.fail(task);
                }
                startNext();
            }
        }
    }

    public void cancel() {
        queue.cancelAll(null);
    }

    /**
     * 子线程的回调方法
     */
    public interface IUploadFileListener {

        void start(BaseTask task);

        void success(BaseTask task);

        void fail(BaseTask task);
    }

    public static class BaseTask {
        public long timeStamp;//时间戳,唯一标识任务的变量
        public String path;//文件路径
    }

    public static class VideoTask extends BaseTask {
        public VideoTask(long timeStamp, String path) {
            this.timeStamp = timeStamp;
            this.path = path;
        }
    }

    public static class PhotoTask extends BaseTask {
        public PhotoTask(long timeStamp, String path) {
            this.timeStamp = timeStamp;
            this.path = path;
        }
    }

    class TaskThread extends Thread {

        private void next() {
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        @Override
        public void run() {
            while (!photoTasks.isEmpty()) {
                PhotoTask task = photoTasks.remove(0);
                uploadFile(task, photoUrl);
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {

                    }
                }
            }

            while (!videoTasks.isEmpty()) {
                VideoTask task = videoTasks.remove(0);
//                uploadFile(task, videoUrl);//视频太大,上传会OOM,需要优化
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {

                    }
                }
            }
            thread = null;
        }
    }
}
