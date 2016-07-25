package com.dudu.drivevideo.camera;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dudu.drivevideo.config.FrontVideoConfigParam;
import com.dudu.drivevideo.service.FrontDriveVideoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dengjun on 2016/2/16.
 * Description :
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Logger log = LoggerFactory.getLogger("video.frontdrivevideo");

    public CameraPreview(Context context) {
        super(context);
        getHolder().addCallback(this);
        log.info("创建CameraPreview对象");
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            //重置surfaceview位置, 顶部向上偏移300Opx,可以让预览居中显示
            int topOffset = (FrontVideoConfigParam.DEFAULT_HEIGHT - FrontVideoConfigParam.DEFAULT_PREVIEW_HEIGHT) / 2;
            layout(left, -topOffset, right, FrontVideoConfigParam.DEFAULT_HEIGHT - topOffset);
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        log.info("surfaceCreated创建");
        FrontDriveVideoService.getInstance().startForegroundRecord(holder);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        log.info("surfaceChanged改变  format = {}  w = {}  h=  {}", format, w, h);

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        log.info("surfaceDestroyed销毁");
        FrontDriveVideoService.getInstance().startBackgroundRecord();

    }
}
