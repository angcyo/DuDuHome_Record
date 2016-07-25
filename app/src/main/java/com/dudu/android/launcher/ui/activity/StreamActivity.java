package com.dudu.android.launcher.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.blur.CameraControl;
import com.dudu.android.launcher.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wysaid.camera.CameraInstance;

import de.greenrobot.event.EventBus;
import me.lake.librestreaming.client.RESClient;
import me.lake.librestreaming.core.listener.RESConnectionListener;
import me.lake.librestreaming.model.RESConfig;

public class StreamActivity extends Activity implements TextureView.SurfaceTextureListener, RESConnectionListener {

    public static CameraInstance mCameraInstance;
    public static Logger log = LoggerFactory.getLogger("stream");
    TextureView txv_preview;
    RESClient resClient;
    TextView tv_rtmp;

    public static void launch(Activity activity) {
        mCameraInstance = CameraInstance.getInstance();
        if (mCameraInstance.getCameraDevice() == null) {
            log.info("Camera未打开, 停止监控.");
            return;
        }

        if (CameraControl.isRecorder()) {
            log.info("监控需要临时停止录像.");
            CameraControl.instance().setRecordNoState(false);
        }

        Intent intent = new Intent(activity, StreamActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_stream);

        tv_rtmp = (TextView) findViewById(R.id.tv_rtmp);

        txv_preview = (TextureView) findViewById(R.id.txv_preview);
        txv_preview.setKeepScreenOn(true);
        txv_preview.setSurfaceTextureListener(this);

        resClient = new RESClient();
        resClient.setCamera(mCameraInstance.getCameraDevice());

        final RESConfig resConfig = RESConfig.obtain();
//        resConfig.setTargetVideoSize(new Size(1920, 1080));
//        resConfig.setBitRate(1000 * 1000);
//        resConfig.setRenderingMode(RESConfig.RenderingMode.OpenGLES);
//        resConfig.setDefaultCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
//        resConfig.setFrontCameraDirectionMode(RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_90);
//        resConfig.setBackCameraDirectionMode(RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_90|RESConfig.DirectionMode.FLAG_DIRECTION_FLIP_HORIZONTAL);
        String rtmp = "rtmp://182.254.227.45/myapp/livestream";
        resConfig.setRtmpAddr(rtmp);
//        resConfig.setRtmpAddr("rtmp://10.57.9.190/live/test");
        if (!resClient.prepare(resConfig)) {
            log.error("aa", "prepare,failed!!");
        }
        resClient.setConnectionListener(this);

        log.info("即将连接至:{}", rtmp);
        resClient.start();

        EventBus.getDefault().unregister(this);
        EventBus.getDefault().register(this);
    }

    public void stop(View view) {
        resClient.stop();
    }

    public void finish(View view) {
        log.info("即将停止.");
        resClient.stop();
        finish();
    }

    public void onEventMainThread(EventStreamFinish event) {
        finish(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish(null);
    }

    @Override
    protected void onDestroy() {
//        resClient.destroy();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        resClient.createPreview(surface, width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        resClient.updatePreview(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        resClient.destroyPreview();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onOpenConnectionResult(int result) {
        /**
         * result==0 success
         * result!=0 failed
         */
//        tv_rtmp.setText("open=" + result);

        if (result == 0) {
            tv_rtmp.setText("连接成功");
        } else {
            tv_rtmp.setText("连接失败");
        }
    }

    @Override
    public void onWriteError(int error) {
        /**
         * failed to write data,maybe restart.
         */
        tv_rtmp.setText("writeError = " + error);
    }

    @Override
    public void onCloseConnectionResult(int result) {
        /**
         * result==0 success
         * result!=0 failed
         */
        tv_rtmp.setText("close = " + result);
    }

    public static class EventStreamFinish {

    }
}
