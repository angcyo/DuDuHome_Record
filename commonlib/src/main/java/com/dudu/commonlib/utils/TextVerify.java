package com.dudu.commonlib.utils;

/**
 * Created by Administrator on 2016/2/18.
 */
public class TextVerify {

    public static boolean isEmpty(CharSequence str) {
        if (str == null || str.equals("null")|| str.length() == 0) {
            return true;
        } else {
            return false;
        }
    }
}
