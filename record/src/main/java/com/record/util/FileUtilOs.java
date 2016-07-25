package com.record.util;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

/**
 * Created by robi on 2016-06-23 18:58.
 */
public class FileUtilOs {
    public static final String TAG = "FileExplorer";
    private static final int TYPE_TEXT = 0;
    private static final int TYPE_AUDIO = 1;
    private static final int TYPE_VIDEO = 2;
    private static final int TYPE_IMAGE = 3;

    public static void openFile(Context context, File f) {
        // We will first guess the MIME type of this file
        final Uri fileUri = Uri.fromFile(f);
        final Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        // Send file name to system application for sometimes it will be used.
        intent.putExtra(Intent.EXTRA_TITLE, f.getName());
        intent.putExtra("org.codeaurora.intent.extra.ALL_VIDEO_FOLDER", true);
        Uri contentUri = null;

        String type = getMIMEType(f);
        // For image file, content uri was needed in Gallery,
        // in order to know the pre/next image.
        if (type != null && type.contains("image/")) {
            String path = fileUri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = context.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(")
                        .append(MediaStore.Images.ImageColumns.DATA)
                        .append("=")
                        .append("'" + path + "'")
                        .append(")");
                Cursor cur = cr.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Images.ImageColumns._ID},
                        buff.toString(), null, null);
                int index = 0;
                if (cur != null) {
                    for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                        index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                        index = cur.getInt(index);
                    }
                }
                if (index == 0) {
                    //not found, do nothing
                    Log.d(TAG, "file not found");
                } else {
                    contentUri = Uri.parse("content://media/external/images/media/" + index);
                }

            }
        }
        if (!"application/octet-stream".equals(type)) {
            if (contentUri != null) {
                intent.setDataAndType(contentUri, type);
            } else {
                intent.setDataAndType(fileUri, type);
            }
            // If no activity can handle the intent then
            // give user a chooser dialog.
            try {
                startActivitySafely(context, intent);

            } catch (ActivityNotFoundException e) {
                showChooserDialog(context, fileUri, intent);
            }
        } else {
            showChooserDialog(context, fileUri, intent);
        }
    }

    private static void showChooserDialog(Context context, final Uri fileUri, final Intent intent) {
        // If this file type can not be recognized then give user a chooser
        // dialog, providing 4 alternative options to open this file: open
        // as plain text, audio, video or image. And the corresponding MIME
        // type will be set to "text/plain", "audio/*", "video/*" and
        // "image/*". By this we can avoid many alternative activity entries
        // from one package in the action chooser dialog.
        final AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle("打开为")
                .setItems(
                        new String[]{
                                "文本",
                                "音频",
                                "视频",
                                "图片"
                        },
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                switch (which) {
                                    case TYPE_TEXT:
                                        intent.setDataAndType(fileUri,
                                                "text/plain");
                                        break;

                                    case TYPE_AUDIO:
                                        intent.setDataAndType(fileUri,
                                                "audio/*");
                                        break;

                                    case TYPE_VIDEO:
                                        intent.setDataAndType(fileUri,
                                                "video/*");
                                        break;

                                    case TYPE_IMAGE:
                                        intent.setDataAndType(fileUri,
                                                "image/*");
                                        break;

                                    default:
                                        break;
                                }
                                startActivitySafely(context, intent);
                            }
                        }).create();
        alertDialog.show();
    }

    public static void startActivitySafely(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context,
                    "Activity Not Found.",
                    Toast.LENGTH_LONG).show();
        }
    }

    public static String getMIMEType(File f) {
        String type = "";
        String fileName = f.getName();
        String ext = fileName.substring(fileName.lastIndexOf(".")
                + 1, fileName.length()).toLowerCase();

        if (ext.equals("jpg") || ext.equals("jpeg") || ext.equals("gif")
                || ext.equals("png") || ext.equals("bmp")) {
            type = "image/*";
        } else if (ext.equals("mp3") || ext.equals("amr") || ext.equals("wma")
                || ext.equals("aac") || ext.equals("m4a") || ext.equals("mid")
                || ext.equals("xmf") || ext.equals("ogg") || ext.equals("wav")
                || ext.equals("qcp") || ext.equals("awb") || ext.equals("flac")) {
            type = "audio/*";
        } else if (ext.equals("3gp") || ext.equals("avi") || ext.equals("mp4")
                || ext.equals("3g2") || ext.equals("wmv") || ext.equals("divx")
                || ext.equals("mkv") || ext.equals("webm") || ext.equals("ts")
                || ext.equals("asf") || ext.equals("3gpp")) {
            type = "video/*";
        } else if (ext.equals("apk")) {
            type = "application/vnd.android.package-archive";
        } else if (ext.equals("vcf")) {
            type = "text/x-vcard";
        } else if (ext.equals("txt")) {
            type = "text/plain";
        } else if (ext.equals("doc") || ext.equals("docx")) {
            type = "application/msword";
        } else if (ext.equals("xls") || ext.equals("xlsx")) {
            type = "application/vnd.ms-excel";
        } else if (ext.equals("ppt") || ext.equals("pptx")) {
            type = "application/vnd.ms-powerpoint";
        } else if (ext.equals("pdf")) {
            type = "application/pdf";
        } else if (ext.equals("xml")) {
            type = "text/xml";
        } else if (ext.equals("html")) {
            type = "text/html";
        } else if (ext.equals("zip")) {
            type = "application/zip";
        } else {
            type = "application/octet-stream";
        }

        return type;
    }
}
