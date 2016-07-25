package com.blur;

import android.hardware.Camera;
import android.util.Log;

import me.lake.librestreaming.client.RESClient;
import me.lake.librestreaming.core.listener.RESConnectionListener;
import me.lake.librestreaming.model.RESConfig;

/**
 * Created by robi on 2016-05-21 17:00.
 */
public class RESControl implements RESConnectionListener {
    RESClient resClient;

    public RESControl() {

    }

    public void init(Camera camera) {
        resClient = new RESClient();
        resClient.setCamera(camera);
        final RESConfig resConfig = RESConfig.obtain();
//        resConfig.setRenderingMode(RESConfig.RenderingMode.OpenGLES);
//        resConfig.setFrontCameraDirectionMode(RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_90);
//        resConfig.setBackCameraDirectionMode(RESConfig.DirectionMode.FLAG_DIRECTION_ROATATION_90|RESConfig.DirectionMode.FLAG_DIRECTION_FLIP_HORIZONTAL);
        resConfig.setRtmpAddr("rtmp://182.254.227.45/myapp/livestream");
        if (!resClient.prepare(resConfig)) {
            Log.e("angcyo", "prepare,failed!!");
        }
        resClient.setConnectionListener(this);
    }

    public void mute() {

    }

    public void start() {
        resClient.start();
    }

    public void queueVideo(byte[] data) {
//        resClient.startVideo(data);
    }

    public void stop() {
        resClient.stop();
    }

    @Override
    public void onOpenConnectionResult(int result) {
        Log.e("angcyo", "推流 open=" + result);
    }

    @Override
    public void onWriteError(int error) {
        Log.e("angcyo", "推流 error=" + error);
    }

    @Override
    public void onCloseConnectionResult(int result) {
        Log.e("angcyo", "推流 close=" + result);
    }
}
