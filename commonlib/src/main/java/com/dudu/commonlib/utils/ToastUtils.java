package com.dudu.commonlib.utils;

import android.content.Context;
import android.widget.Toast;

import com.dudu.commonlib.CommonLib;


public class ToastUtils {

	public static void showToast(int msgId) {
		Toast.makeText(CommonLib.getInstance().getContext(), msgId, Toast.LENGTH_SHORT).show();
	}

	public static void showToast(String message) {
		Toast.makeText(CommonLib.getInstance().getContext(), message, Toast.LENGTH_SHORT).show();
	}

	public static void showToastLong(String message) {
		Toast.makeText(CommonLib.getInstance().getContext(), message, Toast.LENGTH_LONG).show();
	}

	public static void showTip(String message) {
		Toast.makeText(CommonLib.getInstance().getContext(), message, Toast.LENGTH_SHORT).show();
	}

	public static void showTip(Context mContext, String msg) {
		Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
	}
	
}
