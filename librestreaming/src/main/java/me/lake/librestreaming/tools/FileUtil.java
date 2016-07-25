package me.lake.librestreaming.tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by robi on 2016-05-25 16:42.
 */
public class FileUtil {
    public static void write(byte[] data, String filePath) {
        try {
            BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(filePath, true));
            buf.write(data);
            buf.flush();
            buf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void write(OutputStream stream, byte[] bytes) {
        if (stream != null) {
            try {
                stream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(OutputStream stream) {
        if (stream != null) {
            try {
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getFilePath() {
        String folder = "/storage/sdcard1/dudu/h264/";
        File folderFile = new File(folder);
        if (!folderFile.exists()) {
            folderFile.mkdirs();
        }

        return folder + UUID.randomUUID() + ".h264";
    }
}
