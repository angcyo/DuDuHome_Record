package com.dudu.android.launcher.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by Administrator on 2016/3/9.
 */
public class ZoomBitmapFactory {

    private static ZoomBitmapFactory mInstance;

    public static ZoomBitmapFactory getInstance() {
        if (mInstance == null) {
            mInstance = new ZoomBitmapFactory();
        }
        return mInstance;
    }

    public static Bitmap enlarge(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(1.5f, 1.5f); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }

    public static Bitmap reduce(Bitmap bitmap, float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (bitmap.isRecycled()) {
            bitmap.recycle();
        }

        return resizeBmp;
    }
}
