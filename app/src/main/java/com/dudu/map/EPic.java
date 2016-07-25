package com.dudu.map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

/**
 * Created by huafeng.hf on 2016/1/19.
 */
public class EPic extends BaseEData{
    private static final String KEY_PIC_DATA = "pic_data";
    private String picData = ""; // 图片数据

    @Override
    public JSONObject wrapToJson() {
        JSONObject jResult = new JSONObject();
        try {
            jResult.put(KEY_EDATA_TYPE, getEDataType());
            jResult.put(KEY_PIC_DATA, getPicData());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jResult;
    }

    @Override
    public BaseEData unwrapFromJson(String data) throws JSONException {
        if(!TextUtils.isEmpty(data)){
            JSONObject jData = new JSONObject(data);
            setPicData(jData.optString(KEY_PIC_DATA));
        }
        return this;
    }

    public String getPicData() {
        return picData;
    }

    public EPic setPicData(String picData) {
        this.picData = picData;
        return this;
    }

    @Override
    public String getEDataType(){
        return EPic.class.toString();
    }

    /**
     * 图片转成string
     *
     * @param bitmap
     * @return
     */
    public static String convertBitmapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appicon = baos.toByteArray();// 转为byte数组
        return Base64.encodeToString(appicon, Base64.DEFAULT);
    }

    /**
     * string转成bitmap
     *
     * @param st
     */
    public static Bitmap convertStringToBitmap(String st){
        Bitmap bitmap = null;
        try
        {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(st, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
            return bitmap;
        }
        catch (Exception e){
            return null;
        }
    }
}
