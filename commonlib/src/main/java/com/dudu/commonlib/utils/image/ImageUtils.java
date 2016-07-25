package com.dudu.commonlib.utils.image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.dudu.commonlib.CommonLib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by dengjun on 2016/2/21.
 * Description :
 */
public class ImageUtils {
    private static Logger log = LoggerFactory.getLogger("video.ImageUtils");

    public static Drawable getDrawble(int id){
        return CommonLib.getInstance().getContext().getResources().getDrawable(id);
    }


    public static String generateThumbnailFromVideo(String videoAbpath, String thumbnailSaveAbPath){
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoAbpath, MediaStore.Video.Thumbnails.MINI_KIND);
        try {
            saveBitmap(bitmap, thumbnailSaveAbPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return thumbnailSaveAbPath;
    }

    public static void saveBitmap(Bitmap bmp, String fileSavePath) throws Exception {
        File file = new File(fileSavePath);
        FileOutputStream outputStream = new FileOutputStream(file);
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
    }


    public static void savePictureData(byte[] data, File pictureFile){
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            log.error("异常", e);
        } catch (IOException e) {
            log.error("异常", e);
        } catch (Exception e){
            log.error("异常", e);
        }

    }

    public static void saveBitmapToJpg(Bitmap mBitmap,String jpgPath)  {
        File f = new File(jpgPath);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Bitmap convert(Bitmap srcBitmap)

    {

        int w = srcBitmap.getWidth();
        int h = srcBitmap.getHeight();

        Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
        Canvas cv = new Canvas(newb);
        Matrix m = new Matrix();

//        m.postScale(1, -1);   //镜像垂直翻转

        m.postScale(-1, 1);   //镜像水平翻转
//        m.postRotate(-90);  //旋转-90度

        Bitmap new2 = Bitmap.createBitmap(srcBitmap, 0, 0, w, h, m, true);

        cv.drawBitmap(new2, new Rect(0, 0, new2.getWidth(), new2.getHeight()),new Rect(0, 0, w, h), null);

        return newb;

    }
}
