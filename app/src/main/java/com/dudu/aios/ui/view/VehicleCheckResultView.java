package com.dudu.aios.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.dudu.android.launcher.R;

public class VehicleCheckResultView extends View {
    /* drawLine(float startX, float startY, float stopX, float stopY, Paintpaint)
    //画线，参数一起始点的x轴位置，参数二起始点的y轴位置，参数三终点的x轴水平位置，参数四y轴垂直位置，最后一个参数为Paint 画刷对象。*/

    private Paint paintBg;

    private Paint paint;

    private int colorBg;

    private int color;

    private float width = 0;

    private float widthBg = 580;

    private static final String TAG = "VehicleCheckResultView";

    private float targetProgress = 50;//目標進度

    private float curProgress = 10;//当前进度

    private int PROGRESS_TIME = 10;//ms

    private int PROGRESS_STEP;

    private ProgressRunnable progressRunnable;

    private int testState;
    private RectF mBackRect;
    private int defaultHeight;
    private RectF mProgressRect;

    class ProgressRunnable implements Runnable {

        @Override
        public void run() {
            if (curProgress <= targetProgress) {
                curProgress += PROGRESS_STEP;
                if (curProgress > targetProgress) {
                    curProgress = targetProgress;
                }
                width = curProgress * widthBg / 100;
                setWidth(width);
                postDelayed(progressRunnable, PROGRESS_TIME);
                if (curProgress == 0) {
                    paint.setColor(getResources().getColor(R.color.blue));
                }
                if (curProgress == targetProgress) {
                    if (testState == 1) {
                        paint.setColor(getResources().getColor(R.color.red));
                    }
                }

            } else {
                removeCallbacks(progressRunnable);
            }
        }

    }

    public VehicleCheckResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VehicleLine);
        colorBg = array.getColor(R.styleable.VehicleLine_VehicleLineColor, getResources().getColor(R.color.transparent));
        color = array.getColor(R.styleable.VehicleLine_VehicleLineColor, getResources().getColor(R.color.blue));
        array.recycle();
        initView();
        progressRunnable = new ProgressRunnable();
    }

    private void initView() {
        paintBg = new Paint();
        paintBg.setStyle(Paint.Style.FILL);
        paintBg.setAntiAlias(true);//消除锯齿
        paintBg.setColor(colorBg);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);//消除锯齿
        paint.setColor(color);

        mBackRect = new RectF();
        // 设置个新的长方形
        mProgressRect = new RectF(0, 0, 0, defaultHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.AT_MOST) {
            widthBg = MeasureSpec.getSize(widthMeasureSpec);
        }
    }

    public void setWidth(float width) {
        this.width = width;
        refreshProgress(width);
        postInvalidate();
    }

    private void refreshProgress(float width) {
        mProgressRect.set(0, 0, width, defaultHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 设置个新的长方形
        defaultHeight = 20;
        mBackRect.set(0, 0, widthBg, defaultHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
       /* //画背景的直线
        canvas.drawLine(0, 0, widthBg, 0, paintBg);
        //话渲染上的直线
        canvas.drawLine(0, 0, width, 0, paint);*/

        canvas.drawRoundRect(mBackRect, 10, 10, paintBg);//第二个参数是x半径，第三个参数是y半径
        canvas.drawRoundRect(mProgressRect, 10, 10, paint);//第二个参数是x半径，第三个参数是y半径
    }

    public void initProgressColor() {
        paint.setColor(getResources().getColor(R.color.blue));
        testState = 0;
    }

    public void setProgressColor(int color) {
        paint.setColor(color);
        testState = 0;
    }

    public void startAnim(int endProgress) {
        targetProgress = endProgress;
        curProgress = 0;
        removeCallbacks(progressRunnable);
        PROGRESS_STEP = 1;
        PROGRESS_TIME = 5;
        postDelayed(progressRunnable, PROGRESS_TIME);
    }

    public void pauseAndContinueAnim(int enProgress, int state) {
        PROGRESS_STEP = 10;
        PROGRESS_TIME = 20;
        targetProgress = enProgress;
        testState = state;
        removeCallbacks(progressRunnable);
        postDelayed(progressRunnable, PROGRESS_TIME);
    }

    public void reset() {
        removeCallbacks(progressRunnable);
        setWidth(0);
    }
}
