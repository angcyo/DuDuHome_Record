package com.dudu.carChecking;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.dudu.aios.ui.utils.StringUtil;
import com.dudu.android.launcher.R;
import com.dudu.android.launcher.utils.FileUtils;
import com.dudu.android.launcher.utils.LogUtils;
import com.dudu.android.launcher.utils.ZoomBitmapFactory;
import com.dudu.workflow.obd.VehicleConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class VehicleCheckResultAnimation extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CarCheckingView";

    private CarCheckingThread mThread;

    private String category;

    private int categorySize;

    private String animColor = "red";

    private int maxCycleCount = 148;

    public VehicleCheckResultAnimation(Context context, String category, int categorySize) {
        super(context);
        this.categorySize = categorySize;
        this.category = category;
        initView(context);
    }

    public String getCategory() {
        return category;
    }

    public VehicleCheckResultAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startAnim();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LogUtils.e(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopAnim();
    }

    public void startAnim() {
        if (mThread == null) {
            mThread = new CarCheckingThread(getContext(), getHolder());
            mThread.setRunning(true);
            mThread.start();
        } else {
            mThread.setRunning(true);
        }
    }

    private class CarCheckingThread extends Thread {

        private static final int MAXIMUM_FRAME_COUNT = 40;



        private Context mContext;

        private boolean mRunning;

        private SurfaceHolder mHolder;

        private Paint mAnimPaint;

        private Paint mTextPaintCh;

        private Paint mTextPaintEn;

        private Bitmap animFrameBitmap;

        private int mFrameCounter = 0;

        private static final String VEHICLE_MALFUNCTION = "vehicle/malfunction/";

        private String path = "appear";

        private static final String PICTURE_FRAME_PREFIX = "Anim_00";

        public CarCheckingThread(Context context, SurfaceHolder holder) {
            mContext = context;
            mHolder = holder;
            initView();

        }

        private void initView() {
            mAnimPaint = new Paint();
            mAnimPaint.setAntiAlias(true);

            mTextPaintCh = new Paint();
            mTextPaintCh.setAntiAlias(true);
            mTextPaintCh.setColor(getResources().getColor(R.color.white));
            mTextPaintCh.setStyle(Paint.Style.FILL);
            mTextPaintCh.setTypeface(Typeface.DEFAULT_BOLD);
            if (categorySize == 1) {
                mTextPaintCh.setTextSize(30);
            } else if (categorySize == 2) {
                mTextPaintCh.setTextSize(25);
            } else {
                mTextPaintCh.setTextSize(20);
            }

            mTextPaintEn = new Paint();
            mTextPaintEn.setAntiAlias(true);
            mTextPaintEn.setColor(getResources().getColor(R.color.blue));
            mTextPaintEn.setTypeface(Typeface.DEFAULT_BOLD);
            mTextPaintEn.setStyle(Paint.Style.FILL);
            if (categorySize == 1) {
                mTextPaintEn.setTextSize(35);
            } else if (categorySize == 2) {
                mTextPaintEn.setTextSize(30);
            } else {
                mTextPaintEn.setTextSize(25);
            }
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.vehicle_anim_frame);
            if (categorySize == 2) {
                animFrameBitmap = ZoomBitmapFactory.reduce(bitmap, (float) 5 / 4);
            } else if (categorySize == 3 || categorySize == 4 || categorySize == 5) {
                animFrameBitmap = bitmap;
            }
        }

        public void setRunning(boolean running) {
            mRunning = running;
        }

        @Override
        public void run() {

            doAppearAnimation();

            doCycleAnimation();
        }

        private String getCategoryCh(String category) {
            String categoryCh = "";

            switch (category) {
                case VehicleConstants.VEHICLE_ENG:
                    categoryCh = VehicleConstants.VEHICLE_ENG_CH;

                    break;
                case VehicleConstants.VEHICLE_GEA:
                    categoryCh = VehicleConstants.VEHICLE_GEA_CH;
                    break;

                case VehicleConstants.VEHICLE_ABS:
                    categoryCh = VehicleConstants.VEHICLE_ABS_CH;

                    break;
                case VehicleConstants.VEHICLE_WSB:
                    categoryCh = VehicleConstants.VEHICLE_WSB_CH;

                    break;
                case VehicleConstants.VEHICLE_SRS:
                    categoryCh = VehicleConstants.VEHICLE_SRS_CH;
                    break;
            }
            return categoryCh;
        }

        private void doAppearAnimation() {
            while (mRunning && mFrameCounter < MAXIMUM_FRAME_COUNT) {
                Canvas c = null;
                try {
                    synchronized (mHolder) {
                        mFrameCounter++;

                        c = mHolder.lockCanvas();

                        doAnimation(c);
                    }
                } finally {
                    if (c != null) {
                        mHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        private void doCycleAnimation() {
            mFrameCounter = 0;
            path = "cycle";
            File file = new File(FileUtils.getAnimDir(), VEHICLE_MALFUNCTION + category + "/"+animColor+"/" + path);
            if (file.exists() && file.isDirectory()) {
                maxCycleCount = file.listFiles().length;
            }
                while (mRunning && mFrameCounter < maxCycleCount - 1) {
                Canvas c = null;
                try {
                    synchronized (mHolder) {
                        mFrameCounter++;

                        LogUtils.v("CarCheckingView", "当前播放帧数: " + mFrameCounter);

                        c = mHolder.lockCanvas();

                        doAnimation(c);
                        if (mFrameCounter == maxCycleCount - 1) {
                            mFrameCounter = 0;
                        }
                    }
                } finally {
                    if (c != null) {
                        mHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        private void doAnimation(Canvas c) {

            try {

                c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                Bitmap bitmap = loadAnimationBitmap();
                if (bitmap != null) {
                    if (categorySize == 1) {
                        c.drawBitmap(bitmap, 0, 0, mAnimPaint);
                        c.drawText(getCategoryCh(category), 35, bitmap.getHeight() + 25, mTextPaintCh);
                        c.drawText(StringUtil.changeUpper(category), mTextPaintCh.measureText(getCategoryCh(category)) + 50, bitmap.getHeight() + 25, mTextPaintEn);
                    } else {
                        Bitmap reduceBitmap = null;
                        if (categorySize == 2) {
                            reduceBitmap = ZoomBitmapFactory.reduce(bitmap, (float) 3 / 4);
                        } else if (categorySize == 3 || categorySize == 4 || categorySize == 5) {
                            reduceBitmap = ZoomBitmapFactory.reduce(bitmap, (float) 3 / 5);
                        }
                        if (reduceBitmap != null) {
                            c.drawBitmap(animFrameBitmap, 0, 0, mAnimPaint);
                            c.drawBitmap(reduceBitmap, (animFrameBitmap.getWidth() - reduceBitmap.getWidth()) / 2, 2, mAnimPaint);

                            if (categorySize == 2) {
                                c.drawText(getCategoryCh(category), 35, reduceBitmap.getHeight() + 25, mTextPaintCh);
                                c.drawText(StringUtil.changeUpper(category), mTextPaintCh.measureText(getCategoryCh(category)) + 50, reduceBitmap.getHeight() + 25, mTextPaintEn);
                            } else {
                                c.drawText(getCategoryCh(category), 25, reduceBitmap.getHeight() + 20, mTextPaintCh);
                                c.drawText(StringUtil.changeUpper(category), mTextPaintCh.measureText(getCategoryCh(category)) + 45, reduceBitmap.getHeight() + 20, mTextPaintEn);
                            }
                        }
                    }
                } else {
                    Bitmap b = loadStaticBitmap();
                    LogUtils.v(TAG, "静态的....");
                    if (b != null) {
                        c.drawBitmap(b, 0, 0, mAnimPaint);
                        if (!b.isRecycled()) {
                            b.recycle();
                        }
                    }
                }
            } catch (Exception e) {

            }
        }

        private Bitmap loadStaticBitmap() {
            AssetManager am = mContext.getAssets();
            InputStream is;
//            LogUtils.v("vehicle", "静态的");
            try {
                is = am.open("animation/" + category + "_NP1.png");
            } catch (IOException e) {
                return null;
            }
            return BitmapFactory.decodeStream(is);
        }


        private Bitmap loadAnimationBitmap() {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;

            // AssetManager am = mContext.getAssets();
            String frameCount;

            if (mFrameCounter < 10) {
                frameCount = "00" + mFrameCounter;
            } else if (mFrameCounter < 100) {
                frameCount = "0" + mFrameCounter;
            } else {
                frameCount = "" + mFrameCounter;
            }

//            LogUtils.v("vehicle", frameCount);
            InputStream is;
            File file = new File(FileUtils.getAnimDir(), VEHICLE_MALFUNCTION + category + "/" + animColor + "/" + path + "/" + PICTURE_FRAME_PREFIX + frameCount + ".png");
            LogUtils.v(TAG, file.getPath());
            // is = am.open("car_checking/" + PICTURE_PREFIX + mFrameCounter + ".png");
            if (file.exists()) {
                try {
                    is = new FileInputStream(file);
                    return BitmapFactory.decodeStream(is);
                } catch (IOException e) {
                    LogUtils.e(TAG, e.getMessage());
                    return null;
                }
            }
            return null;
        }
    }

    public void stopAnim() {
        if (mThread != null) {
            mThread.setRunning(false);
            try {
                mThread.join();
            } catch (InterruptedException e) {
                LogUtils.e("CarCheckingView", e.getMessage());
            }
            mThread = null;
        }
    }

    public void showBlueAnim() {
        animColor = "blue";
        maxCycleCount=332;
    }
}
