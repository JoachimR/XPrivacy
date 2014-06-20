package de.reiss.xprivacynative;

import android.util.Log;

import java.io.*;

public class NativeAccessManagement {

    public static final String TAG = "XPrivacy NativeAccessManagement";


    public static void takeCareOfRunCommandNative(boolean newRestrictedState, String mUid) {
        final String filename = "/data/data/biz.bokhorst.xprivacy/files/" + Global.DISABLED_SHELLCMDS_UIDS_TXT;
        takeCare(newRestrictedState, mUid, filename);
    }


    public static void takeCareOfSdCardNativeAccess(boolean newRestrictedState, String mUid) {
        final String filename = "/data/data/biz.bokhorst.xprivacy/files/" + Global.DISABLED_SDCARDACCESS_UIDS;
        takeCare(newRestrictedState, mUid, filename);
    }

    public static void takeCareOfRecordAudioNative(boolean newRestrictedState, String mUid) {
        final String filename = "/data/data/biz.bokhorst.xprivacy/files/" + Global.DISABLED_RECORDAUDIO_UIDS_TXT;
        takeCare(newRestrictedState, mUid, filename);
    }


    private static void takeCare(boolean newRestrictedState, String mUid, String filename) {
        // file has the format:  uid1,uid2,uid3,...
        // The modified system/bin/sdcard process reads from that file
        // when a file is about to be opened


        final String contentSoFar = readFromFile(filename);

        if (newRestrictedState) {
            // add the uid to the file
            Log.d(NativeAccessManagement.TAG, "add the uid to the file");

            if (!contentSoFar.contains(mUid + ",")) {
                overwriteFile(filename, contentSoFar + mUid + ",");
            }
        } else {
            // remove the uid from the file
            Log.d(NativeAccessManagement.TAG, "remove the uid from the file");

            String newContent = null;

            if (contentSoFar.contains("," + mUid + ",")) {
                newContent = contentSoFar.replaceAll("," + mUid + ",", ",");

                if (newContent != null && newContent.contains(mUid + ",")) {
                    newContent = newContent.replaceAll(mUid + ",", "");
                }

            } else if (contentSoFar.contains(mUid + ",")) {
                newContent = contentSoFar.replaceAll(mUid + ",", "");
            }


            Log.d(NativeAccessManagement.TAG, "newContent == " + newContent);
            if (newContent != null) {
                createFile(filename);
                overwriteFile(filename, newContent);
            }
        }
    }


    private static boolean createFile(String pathAndFileName) {

        File file = new File(pathAndFileName);
        try {
            FileOutputStream f = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(NativeAccessManagement.TAG, "error: " + e.getMessage());
            return false;
        }
        return true;
    }


    private static boolean overwriteFile(String pathAndFileName, String textToAppend) {
        try {
            File file = new File(pathAndFileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter buf = new BufferedWriter(new FileWriter(pathAndFileName, false));
            buf.append(textToAppend);
            buf.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(NativeAccessManagement.TAG, "error: " + e.getMessage());
            return false;
        }
        return true;
    }

    private static String readFromFile(String pathAndFileName) {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(pathAndFileName));
            String line;

            while ((line = br.readLine()) != null
                    && text.length() <= 1024) {
                text.append(line);
                text.append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(NativeAccessManagement.TAG, "error: " + e.getMessage());
            return "";
        }

        return text.toString();
    }

}
