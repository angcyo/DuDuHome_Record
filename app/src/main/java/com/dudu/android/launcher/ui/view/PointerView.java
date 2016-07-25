package com.dudu.android.launcher.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.dudu.android.launcher.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PointerView extends View {

    private Paint mPaint;

    private Bitmap mBitmap;

    private int mWidth;

    private int mHeight;

    private Matrix matrix;

    private float currentAngle = 0.0f;

    private float targetAngle;

    private Bitmap bitmapDisplay = null;

    private AngleRunnable angleRunnable;

    private Context mContext;

    private static final float SPEED_INTERVAL = 1.5f;

    private static final int TIME_INTERVAL = 10;

    private Logger logger = LoggerFactory.getLogger("PointerView");

    private boolean isRight = true;

    public PointerView(Context context) {
        super(context);
        mContext = context;
        iniView();
    }

    public PointerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        iniView();
    }

    private void iniView() {
        angleRunnable = new AngleRunnable();
        mPaint = new Paint();
        mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pointer);
        bitmapDisplay = mBitmap;
        mWidth = mBitmap.getWidth();
        mHeight = mBitmap.getHeight();
        matrix = new Matrix();
        matrix.setRotate(180);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmapDisplay, (1920 - (bitmapDisplay.getWidth())) / 2, (480 - bitmapDisplay.getHeight()) / 2, mPaint);
        invalidate();
    }

    public void setAccelerationSpeed(float targetAngle) {
        if (targetAngle > currentAngle) {
            setRotationRight(targetAngle);
        } else if (targetAngle < currentAngle) {
            setRotationLeft(targetAngle);
        }

    }

    //  向左旋转
    public void setRotationLeft(float targetAngle) {
        isRight = false;
        this.targetAngle = -targetAngle;
        removeCallbacks(angleRunnable);
        postDelayed(angleRunnable, TIME_INTERVAL);

    }

    //  向右旋转
    public void setRotationRight(float targetAngle) {
        isRight = true;
        this.targetAngle = targetAngle;
        removeCallbacks(angleRunnable);
        postDelayed(angleRunnable, TIME_INTERVAL);
    }

    //  设置旋转比例
    private void setAngle() {
        matrix.reset();
        matrix.setRotate(currentAngle);
        bitmapDisplay = Bitmap.createBitmap(mBitmap, 0, 0, mWidth, mHeight, matrix, true);
    }

    class AngleRunnable implements Runnable {

        @Override
        public void run() {
            if (currentAngle != targetAngle) {
                if (isRight) {
                    currentAngle += SPEED_INTERVAL;
                } else {
                    currentAngle -= SPEED_INTERVAL;
                }
                setAngle();
                logger.debug("currentAngle:" + currentAngle);
                postDelayed(angleRunnable, 10);
            } else {
                removeCallbacks(angleRunnable);
            }
        }
    }

    public void reset() {
        currentAngle = 0.0f;
        removeCallbacks(angleRunnable);
        setAngle();
    }
}
