package com.dudu.carChecking;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.dudu.aios.ui.utils.StringUtil;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.FileUtils;
import com.dudu.workflow.obd.VehicleConstants;

import java.io.File;


/**
 * Created by Administrator on 2016/4/12.
 */
public class AnimVideoView extends RelativeLayout {

    private VideoTextureView videoTextureView;

    private LayoutParams videoParams;

    private boolean isFirst = true;

    private Context mContext;

    private String category = "";

    private String type = "red";

    private Paint mChPaint, mEnPaint;

    private int mWidth;

    private int mHeight;

    private float mTextSize = 20;

    public AnimVideoView(Context context) {
        super(context);
        mContext = context;
        iniView();
    }

    public void setType(String type) {
        this.type = type;
    }

    public AnimVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        iniView();
    }


    private void iniView() {
        videoTextureView = new VideoTextureView(mContext);

        mChPaint = new Paint();
        mChPaint.setColor(getResources().getColor(R.color.white));
        mChPaint.setStyle(Paint.Style.FILL);
        mChPaint.setAntiAlias(true);
        mChPaint.setTypeface(Typeface.DEFAULT_BOLD);

        mEnPaint = new Paint();
        mEnPaint.setColor(getResources().getColor(R.color.blue));
        mEnPaint.setStyle(Paint.Style.FILL);
        mEnPaint.setAntiAlias(true);
        mEnPaint.setTypeface(Typeface.DEFAULT_BOLD);

    }

    public void startPlay() {
        File file;
        file=new File(VehicleConstants.SYSTEM_ANIMATION_DIR+"/"+VehicleConstants.VEHICLE_MALFUNCTION_DIR+"/"+category+"/"+type,VehicleConstants.VEHICLE_APPEAR_MP4);
        if (!file.exists()) {
            file = new File(FileUtils.getAnimDir() + "/" + VehicleConstants.VEHICLE_MALFUNCTION_DIR + "/" + category+"/"+type, VehicleConstants.VEHICLE_APPEAR_MP4);
        }
        if (file.exists()) {
            videoTextureView.setFilePath(file.getPath());
            videoTextureView.setFps(25);
        }
        videoTextureView.setPlayListener(new MyListener(videoTextureView, category));
        videoTextureView.startPlay();
        postInvalidate();
    }


    public void setCategory(String category) {
        this.category = category;
    }

    private class MyListener implements VideoTextureView.PlayListener {
        VideoTextureView videoTextureView;
        String category;

        public MyListener(VideoTextureView videoTextureView, String category) {
            this.videoTextureView = videoTextureView;
            this.category = category;
        }

        @Override
        public void onPlayEnd() {
            File file;
            file=new File(VehicleConstants.SYSTEM_ANIMATION_DIR+"/"+VehicleConstants.VEHICLE_MALFUNCTION_DIR+"/"+category+"/"+type,VehicleConstants.VEHICLE_CYCLE_MP4);
            if (!file.exists()) {
                file = new File(FileUtils.getAnimDir() + "/" + VehicleConstants.VEHICLE_MALFUNCTION_DIR + "/" + category+"/"+type, VehicleConstants.VEHICLE_CYCLE_MP4);
            }            if (file.exists()) {
                videoTextureView.setFilePath(file.getPath());
                videoTextureView.setFps(25);
                videoTextureView.setLoopMode(true);
                videoTextureView.startPlay();
            }
        }

        @Override
        public void onPlayStart() {

        }
    }

    public void stopPlay() {
        if (videoTextureView != null) {
            videoTextureView.setPlayListener(null);
            videoTextureView.stopPlay();
        }
        if (getChildCount() > 0) {
            removeAllViews();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY && MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            mWidth = MeasureSpec.getSize(widthMeasureSpec);
            mHeight = MeasureSpec.getSize(heightMeasureSpec);

            if (isFirst) {
                if (mWidth < 300 && mHeight < 300) {
                    setBackgroundResource(R.drawable.vehicle_anim_frame);
                } else {
                    setBackgroundResource(R.color.black);
                }
                videoParams = new LayoutParams(mWidth - (mWidth / 10 + 14), mHeight - (mHeight / 10 + 14));
                videoParams.setMargins((mWidth / 10 + 14) / 2, 2, (mWidth / 10 + 14) / 2, mHeight / 10 + 12);
                addView(videoTextureView, videoParams);
                isFirst = false;
            }

        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mTextSize = mWidth / 10;
        mChPaint.setTextSize(mTextSize + 5);
        mEnPaint.setTextSize(mTextSize + 5);
        canvas.drawText(StringUtil.changeUpper(category), mTextSize * 3 - 30, mHeight - 10, mChPaint);
        canvas.drawText(obtainCh(category), mWidth / 2, mHeight - 10, mEnPaint);
    }


    private String obtainCh(String category) {
        String categoryEn = "";
        switch (category) {
            case "gea":
                categoryEn = "变速箱";
                break;
            case "abs":
                categoryEn = "防抱死";
                break;
            case "wsb":
                categoryEn = "胎压";
                break;
            case "eng":
                categoryEn = "发动机";
                break;
            case "srs":
                categoryEn = "气囊";
                break;
        }
        return categoryEn;
    }
}
