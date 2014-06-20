package de.reiss.xprivacynative.nativesdcard;

import android.content.Context;
import android.os.FileObserver;
import android.text.TextUtils;
import android.util.Log;
import de.reiss.xprivacynative.Global;
import de.reiss.xprivacynative.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SdcardLogFileObserver extends FileObserver {

    private Context mContext;

    private String mFileName;

    private static final File LOG_FILE = new File("/data/data/biz.bokhorst.xprivacy/sdcardlog.txt");
    private static final String PACKAGES_XML_FILE_NAME = "packages.xml";

    // this helps to update an already displaying notification for the observed file
    private int statusBarNotificationId;

    public SdcardLogFileObserver(Context context, String path) {
        super(path);
        mFileName = path;
        mContext = context;

        statusBarNotificationId = new Random().nextInt(9999);
    }

    public void close() {
        super.finalize();
    }

    @Override
    public void onEvent(int event, String path) {

        try {
            // get filename without path
            int slashPos = mFileName.lastIndexOf("/");
            String filename = mFileName.substring(slashPos + 1);

            String lastLines = readLastLineOfTextFile(LOG_FILE);

            String accessInfo = getFileAccessInfo(mContext, filename, lastLines);

            Utils.notifyOnStatusBar(statusBarNotificationId, mContext, filename, accessInfo);

            Log.d(Global.TAG, filename + " " + accessInfo);

        } catch (Exception e) {
            Log.d(Global.TAG, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }


    private static String getFileAccessInfo(Context context, String filename, String lastLines) {
        String result = "";


        int lastIndex = lastLines.lastIndexOf(filename);
        if (lastIndex < 0) {
            return "unknown";
        }

        final String uidAndPidInfo = lastLines.substring(lastIndex);


        if (uidAndPidInfo.contains("uid=")) {

            final String uid = getUidFromInfo(uidAndPidInfo);

            if (TextUtils.isDigitsOnly(uid)) {
                String packageName = getPackageNameFromUid(getPackagesXmlFile(context), uid);

                return "accessed by " + packageName;
            }
        }

        return uidAndPidInfo;
    }

    private static FileInputStream getPackagesXmlFile(Context context) {
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(PACKAGES_XML_FILE_NAME);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return fis;
    }

    private static String getPackageNameFromUid(FileInputStream fis, String uid) {
        if (fis == null) {
            return "";
        }

        final ArrayList<InstalledApp> allApps =
                PackagesXmlParser.getAllInstalledApps(fis);

        if (allApps != null && allApps.size() > 0) {
            for (InstalledApp app : allApps) {
                if (app.userId.equals(uid)) {
                    return app.packageName;
                }
            }
        }

        return "";
    }


    private static String getUidFromInfo(String info) {
        Pattern pattern = Pattern.compile("uid=([0-9]*)");
        Matcher matcher = pattern.matcher(info);
        if (matcher.find()) {

            return matcher.group(1);
        }
        return "";
    }


    private static String readLastLineOfTextFile(File file) {
        return getLastLines(file, 50);
    }

    private static String getLastLines(File file, int amountOflastLines) {
        if (amountOflastLines < 1) {
            return "";
        }

        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (randomAccessFile == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        try {
            int lines = 0;
            StringBuilder builder = new StringBuilder();
            long length = file.length();
            length--;
            randomAccessFile.seek(length);
            for (long seek = length; seek >= 0; --seek) {
                randomAccessFile.seek(seek);
                char c = (char) randomAccessFile.read();
                builder.append(c);
                if (c == '\n') {
                    builder = builder.reverse();
                    sb.append(builder.toString());
                    lines++;
                    builder = new StringBuilder();
                    if (lines == amountOflastLines) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


}
