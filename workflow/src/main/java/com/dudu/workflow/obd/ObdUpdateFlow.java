package com.dudu.workflow.obd;

import java.io.IOException;

import rx.Observable;

/**
 * Created by Administrator on 2016/3/21.
 */
public class ObdUpdateFlow {

    public static final int OBD_INDEX = 0;
    public static final int LOCAL_INDEX = 1;
    public static final int SERVER_INDEX = 2;

    public static final String ATGETVER = "ATGETVER";

    public static void checkVersion() throws IOException {
        OBDStream.getInstance().exec(ATGETVER);
    }

    public static Observable<String> getObdVersion() throws IOException {
        return OBDStream.getInstance()
                .OBDVersion();
    }

    public static int checkNewestVersion(String[] obdVersions) {
        int[][] versions = new int[3][3];
        for (int i = 0; i < obdVersions.length; i++) {
            String[] verStrings = obdVersions[i].split("\\.");
            for (int j = 0; j < verStrings.length; j++) {
                versions[i][j] = Integer.valueOf(verStrings[j]);
            }
        }
        for (int j = 0; j < obdVersions.length; j++) {
            int max = checkMax(new int[]{versions[OBD_INDEX][j], versions[LOCAL_INDEX][j], versions[SERVER_INDEX][j]});
            if (max != OBD_INDEX) {
                return max;
            }
        }
        return OBD_INDEX;
    }

    public static int compareTwoVersion(int firstKey, int secondKey, String firstVersionString, String secondVersionString) {
        String[] firstVerStrings = firstVersionString.split("\\.");
        String[] secondVerStrings = secondVersionString.split("\\.");
        int[] firstVersion = new int[3];
        int[] secondVersion = new int[3];
        for (int j = 0; j < firstVerStrings.length; j++) {
            firstVersion[j] = Integer.valueOf(firstVerStrings[j]);
        }
        for (int j = 0; j < firstVerStrings.length; j++) {
            secondVersion[j] = Integer.valueOf(secondVerStrings[j]);
        }
        for (int j = 0; j < 3; j++) {
            int max = bigger(firstKey, secondKey, firstVersion[j], secondVersion[j]);
            if (max != firstKey) {
                return max;
            }
        }
        return firstKey;
    }

    private static int checkMax(int[] versions) {
        int max = OBD_INDEX;
        if (versions[LOCAL_INDEX] > versions[max]) {
            max = LOCAL_INDEX;
        }
        if (versions[SERVER_INDEX] > versions[max]) {
            max = SERVER_INDEX;
        }
        return max;
    }

    private static int bigger(int firstKey, int secondKey, int firstVersion, int secondVersion) {
        if (secondVersion > firstVersion) {
            return secondKey;
        }
        return firstKey;
    }

}
