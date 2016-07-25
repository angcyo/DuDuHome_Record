/*******************************************************************************
 * Copyright 2014 AISpeech
 ******************************************************************************/
package com.dudu.voice.speech;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.dudu.aios.ui.bt.Contact;
import com.dudu.android.launcher.utils.BtPhoneUtils;
import com.dudu.commonlib.CommonLib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

/**
 * 辅助生成ebnf语法文件类 <br>
 * 需要权限: {@link android.Manifest.permission#READ_CONTACTS
 * android.permission.READ_CONTACTS}
 * {@link android.Manifest.permission#WRITE_EXTERNAL_STORAGE
 * android.permission.WRITE_EXTERNAL_STORAGE}
 */
public class GrammarHelper {

    public static final String notCnOrNumPattern = "[^\u4e00-\u9fa5\u0030-\u0039]";

    public static final char[] NUMBER_CN = {'一', '二', '三', '四', '五', '六', '七', '八', '九', '零', '幺'};
    public static final int[] CN_NUMBER = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1};

    private Context mContext;

    public GrammarHelper(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 获取联系人列表 返回格式：AA | BB | CC | DD
     */
    public String getConatcts() {

        StringBuilder contactSb = new StringBuilder();

        List<Contact> contactses = BtPhoneUtils.obtainContacts(CommonLib.getInstance().getContext());
        if (!contactses.isEmpty()) {
            for (Contact contacts : contactses) {
                contactSb.append(contacts.getName()).append("\n").append("|");
            }
        }

        if (contactSb.length() > 1) {
            return contactSb.deleteCharAt(contactSb.length() - 1).toString();
        } else {
            return contactSb.toString();
        }

    }

    /**
     * 获取手机内安装了的应用列表 返回格式：AA | BB | CC | DD
     */
    public String getApps() {
        StringBuilder apps = new StringBuilder();

        PackageManager mPackageManager = mContext.getPackageManager();
        Intent mIntent = new Intent(Intent.ACTION_MAIN);
        mIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> mApps = mPackageManager.queryIntentActivities(mIntent, 0);

        for (int i = 0; i < mApps.size(); i++) {
            ResolveInfo tempInfo = mApps.get(i);
            String appLabel = mPackageManager.getApplicationLabel(tempInfo.activityInfo.applicationInfo).toString();
            appLabel = appLabel.replaceAll(notCnOrNumPattern, "");

            if (appLabel != null && !appLabel.trim().equals("")) {
                apps.append(appLabel).append("\n").append("|");

            }
        }
        if (apps.length() > 1) {
            return apps.deleteCharAt(apps.length() - 1).toString();
        } else {
            return apps.toString();
        }
    }

    /**
     * 利用模板文件，生成ebnf格式grammar
     *
     * @param contacts 联系人列表 格式： AA | BB | CC
     * @param appName  获取手机内安装了的应用列表 返回格式：AA | BB | CC | DD
     * @param filename assets目录下模板文件名
     * @return
     */
    public String importAssets(String contacts, String appName, String filename) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new InputStreamReader(mContext.getAssets().open(filename), Charset.forName("UTF-8")));
            String line = null;
            while ((line = br.readLine()) != null) {

                if (line.contains("#CONTACT#")) {
                    line = line.replaceAll(" ", "");
                    line = line.replaceAll("#CONTACT#;", "");
                    sb.append(line);
                    sb.append(contacts);
                    sb.append(";\n");

                } else if (line.contains("#APPNAME#")) {
                    line = line.replaceAll(" ", "");
                    line = line.replaceAll("#APPNAME#;", "");
                    sb.append(line);
                    sb.append(appName);
                    sb.append(";\n");

                } else {
                    sb.append(line + "\n");
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("error", "Template file " + filename + " not found, Pls fix this bug!");
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }
}
