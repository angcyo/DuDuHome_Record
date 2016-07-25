package com.blur;

/**
 * Created by wangyang on 15/7/27.
 */


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wysaid.camera.CameraInstance;
import org.wysaid.common.Common;
import org.wysaid.nativePort.CGEFrameRenderer;
import org.wysaid.texUtils.TextureRenderer;
import org.wysaid.texUtils.TextureRendererDrawOrigin;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by wangyang on 15/7/17.
 */
public class BlurGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    public static final String LOG_TAG = "video.frontdrivevideo";
    private Logger log = LoggerFactory.getLogger("video.frontdrivevideo");
    public static final String effectConfigs[] = {
            "",
            "@beautify bilateral 10 4 1 @style haze -0.5 -0.5 1 1 1 @curve RGB(0, 0)(94, 20)(160, 168)(255, 255) @curve R(0, 0)(129, 119)(255, 255)B(0, 0)(135, 151)(255, 255)RGB(0, 0)(146, 116)(255, 255)",
            "#unpack @blur lerp 0.5", //可调节模糊强度
            "@blur lerp 1", //可调节混合强度
            "#unpack @dynamic wave 1", //可调节速度
            "@dynamic wave 0.5",       //可调节混合
    };
    private static final int INIT_CAMERA = 0;
    protected final int[] mThunbnailLock = new int[0];
    public int maxTextureSize = 0;
    public int viewWidth;
    public int viewHeight;
    public int maxPreviewWidth = 1920;
    public int maxPreviewHeight = 480;
    public ClearColor clearColor;
    protected int mRecordWidth = 1920;
    protected int mRecordHeight = 1080;//控制大小
    protected SurfaceTexture mSurfaceTexture;
    protected int mTextureID;
    protected CGEFrameRenderer mFrameRecorder;
    protected Context mContext;
    protected TextureRenderer.Viewport mDrawViewport = new TextureRenderer.Viewport();
    protected boolean mIsUsingMask = false;
    protected boolean mFitFullView = false;
    protected float mMaskAspectRatio = 1.0f;
    protected float[] mTransformMatrix = new float[16];
    //是否使用后置摄像头
    protected boolean mIsCameraBackForward = true;
    protected long mTimeCount2 = 0;
    protected long mFramesCount2 = 0;
    protected long mLastTimestamp2 = 0;
    protected Bitmap mThunbnailBmp;
    protected TakeThunbnailCallback mTakeThunbnailCallback;
    protected int mThunbnailWidth, mThunbnailHeight;
    protected IntBuffer mThunbnailBuffer;
    TextureRendererDrawOrigin mBackgroundRenderer;
    int mBackgroundTexture;
    RectF mThumnailClipingArea;
    OnSurfaceCreate mOnSurfaceCreate;

    public BlurGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(LOG_TAG, "MyGLSurfaceView Construct...");

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 8, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        setZOrderOnTop(false);
//        setZOrderMediaOverlay(true);

        clearColor = new ClearColor();
        mContext = context;
    }

    public BlurGLSurfaceView(Context context) {
        super(context, null);
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    public CameraInstance cameraInstance() {
        return CameraInstance.getInstance();
    }

    public synchronized void setFilterWithConfig(final String config) {
        queueEvent(new Runnable() {
            @Override
            public void run() {

                if (mFrameRecorder != null) {
                    mFrameRecorder.setFilterWidthConfig(config);
                } else {
                    Log.e(LOG_TAG, "setFilterWithConfig after release!!");
                }
            }
        });
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(LOG_TAG, "onSurfaceCreated...");
        log.info("BlurGLSurfaceView 创建");
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_STENCIL_TEST);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        int texSize[] = new int[1];

        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, texSize, 0);
        maxTextureSize = texSize[0];

        mTextureID = Common.genSurfaceTextureID();
        mSurfaceTexture = new SurfaceTexture(mTextureID);
        mSurfaceTexture.setOnFrameAvailableListener(this);

        mFrameRecorder = new CGEFrameRenderer();
        if (!mFrameRecorder.init(mRecordWidth, mRecordHeight, mRecordWidth, mRecordHeight)) {
            Log.e(LOG_TAG, "Frame Recorder init failed!");
        }

        mFrameRecorder.setSrcRotation((float) (Math.PI / 2.0));
        mFrameRecorder.setSrcFlipScale(1.0f, -1.0f);
        mFrameRecorder.setRenderFlipScale(1.0f, -1.0f);

        requestRender();

        if (mOnSurfaceCreate != null) {
            mOnSurfaceCreate.onSurfaceCreate();
        }

        setBlur(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(1920, 1080);
    }

    public void setOnSurfaceCreate(OnSurfaceCreate onSurfaceCreate) {
        mOnSurfaceCreate = onSurfaceCreate;
    }

    protected void calcViewport() {

        float scaling;

        if (mIsUsingMask) {
            scaling = mMaskAspectRatio;
        } else {
            scaling = mRecordWidth / (float) mRecordHeight;
        }

        float viewRatio = viewWidth / (float) viewHeight;
        float s = scaling / viewRatio;

        int w, h;

        if (mFitFullView) {
            //撑满全部view(内容大于view)
            if (s > 1.0) {
                w = (int) (viewHeight * scaling);
                h = viewHeight;
            } else {
                w = viewWidth;
                h = (int) (viewWidth / scaling);
            }
        } else {
            //显示全部内容(内容小于view)
            if (s > 1.0) {
                w = viewWidth;
                h = (int) (viewWidth / scaling);
            } else {
                h = viewHeight;
                w = (int) (viewHeight * scaling);
            }
        }

        mDrawViewport.width = w;
        mDrawViewport.height = h;
        mDrawViewport.x = (viewWidth - mDrawViewport.width) / 2;
        mDrawViewport.y = (viewHeight - mDrawViewport.height) / 2;
        Log.i(LOG_TAG, String.format("View port: %d, %d, %d, %d", mDrawViewport.x, mDrawViewport.y, mDrawViewport.width, mDrawViewport.height));
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i(LOG_TAG, String.format("onSurfaceChanged: %d x %d", width, height));

        GLES20.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);

        viewWidth = width;
        viewHeight = height;

        calcViewport();

        log.info("BlurGLSurfaceView onSurfaceChanged 改变");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        Log.i(LOG_TAG, "surfaceDestroyed---------");
//        cameraInstance().stopCamera();
        log.info("BlurGLSurfaceView surfaceDestroyed 销毁");
    }

    @Override
    public void onDrawFrame(GL10 gl) {
//        Log.v(LOG_TAG, "onDrawFrame---------");

        if (mSurfaceTexture == null || !cameraInstance().isPreviewing()) {
            //防止双缓冲情况下最后几帧抖动
            if (mFrameRecorder != null) {
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                mFrameRecorder.render(mDrawViewport.x, mDrawViewport.y, mDrawViewport.width, mDrawViewport.height);
            }

            return;
        }

        mSurfaceTexture.updateTexImage();

        mSurfaceTexture.getTransformMatrix(mTransformMatrix);
        mFrameRecorder.update(mTextureID, mTransformMatrix);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        if (mTakeThunbnailCallback != null && !mTakeThunbnailCallback.isUsingBitmap()) {

            synchronized (mThunbnailLock) {

                // double judgement for mTakeThunbnailCallback ensure both performance and safety
                if (mTakeThunbnailCallback != null) {

                    if (mThumnailClipingArea != null) {
                        int clipW = (int) (mThunbnailWidth / mThumnailClipingArea.width());
                        int clipH = (int) (mThunbnailHeight / mThumnailClipingArea.height());
                        int x = -(int) (clipW * mThumnailClipingArea.left);
                        int y = -(int) (clipH * mThumnailClipingArea.top);

                        GLES20.glViewport(x, y, clipW, clipH);

                    } else {
                        GLES20.glViewport(0, 0, mThunbnailWidth, mThunbnailHeight);
                    }

                    mFrameRecorder.drawCache();

                    mThunbnailBuffer.position(0);
                    GLES20.glReadPixels(0, 0, mThunbnailWidth, mThunbnailHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mThunbnailBuffer);

                    mThunbnailBmp.copyPixelsFromBuffer(mThunbnailBuffer);

                    post(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (mThunbnailLock) {
                                if (mTakeThunbnailCallback != null)
                                    mTakeThunbnailCallback.takeThunbnailOK(mThunbnailBmp);
                            }
                        }
                    });
                }
            }
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        if (mBackgroundRenderer != null) {
            GLES20.glViewport(0, 0, viewWidth, viewHeight);
            mBackgroundRenderer.renderTexture(mBackgroundTexture, null);
        }
        GLES20.glEnable(GLES20.GL_BLEND);
        mFrameRecorder.render(mDrawViewport.x, mDrawViewport.y, mDrawViewport.width, mDrawViewport.height);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "glsurfaceview onResume...");
    }

    @Override
    public void onPause() {
        Log.i(LOG_TAG, "glsurfaceview onPause in...");

//        cameraInstance().stopCamera();
        super.onPause();
        Log.i(LOG_TAG, "glsurfaceview onPause out...");
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

        requestRender();

        if (mLastTimestamp2 == 0)
            mLastTimestamp2 = System.currentTimeMillis();

        long currentTimestamp = System.currentTimeMillis();

        ++mFramesCount2;
        mTimeCount2 += currentTimestamp - mLastTimestamp2;
        mLastTimestamp2 = currentTimestamp;
        if (mTimeCount2 >= 1000) {
//            Log.i(LOG_TAG, String.format("相机每秒采样率: %d", mFramesCount2));
            mTimeCount2 %= 1000;
            mFramesCount2 = 0;
        }
    }

    public void setBlur(boolean blur) {
        if (blur) {
            setFilterWithConfig(effectConfigs[2]);
        } else {
            setFilterWithConfig(effectConfigs[0]);
        }
    }

    public interface TakeThunbnailCallback {
        // 当 TakeThunbnailCallback 被设置之后
        // 每一帧都会获取 isUsingBitmap() 返回值
        // 当 isUsingBitmap() 返回 true 的时候 takeThunbnailOK 将不被调用
        // 当 isUsingBitmap() 返回 false 的时候 takeThunbnailOK 正常执行
        boolean isUsingBitmap();

        void takeThunbnailOK(Bitmap bmp);
    }

    public interface OnSurfaceCreate {
        void onSurfaceCreate();
    }

    public class ClearColor {
        public float r, g, b, a;
    }

    //Not provided by now.
}
