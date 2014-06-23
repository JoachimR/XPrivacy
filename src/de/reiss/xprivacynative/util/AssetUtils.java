package de.reiss.xprivacynative.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import de.reiss.xprivacynative.Global;
import de.reiss.xprivacynative.Shell;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AssetUtils {


    private final static String SUBDIR = "exported_app_assets";

    public final static String SDCARD_DIR = Environment.getExternalStorageDirectory() + "/" + SUBDIR;


    public static void copyFileOrDir(Context context, String path) {
        AssetManager assetManager = context.getAssets();
        String assets[] = null;
        try {
            Log.i(Global.TAG, "copyFileOrDir() " + path);
            assets = assetManager.list(path);
            if (assets.length == 0) {
                copyFile(context, path);
            } else {

                String fullPath = SDCARD_DIR + "/" + path;
                Log.i(Global.TAG, "path=" + fullPath);
                File dir = new File(fullPath);
                if (!dir.exists() && !path.startsWith("images") &&
                        !path.startsWith("sounds") && !path.startsWith("webkit")) {
                    if (!dir.mkdirs()) {
                        Log.i(Global.TAG, "could not create dir " + fullPath);
                    }
                }
                for (int i = 0; i < assets.length; ++i) {
                    String p;
                    if (path.equals("")) {
                        p = "";
                    } else {
                        p = path + "/";
                    }

                    if (!path.startsWith("images") && !path.startsWith("sounds")
                            && !path.startsWith("webkit")) {
                        copyFileOrDir(context, p + assets[i]);
                    }
                }
            }
        } catch (IOException ex) {
            Log.e(Global.TAG, "I/O Exception", ex);
        }
    }

    public static void copyFile(Context context, String filename) {
        AssetManager assetManager = context.getAssets();

        InputStream in = null;
        OutputStream out = null;
        String newFileName = null;
        try {
            Log.i(Global.TAG, "copyFile() " + filename);
            in = assetManager.open(filename);

            File sdcardDir = new File(SDCARD_DIR);
            // build the directory structure, if needed.
            sdcardDir.mkdirs();


            if (filename.endsWith(".jpg")) { // extension was added to avoid compression on APK file
                newFileName = SDCARD_DIR + "/" + filename.substring(0, filename.length() - 4);
            } else {
                newFileName = SDCARD_DIR + "/" + filename;
            }
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            Log.e(Global.TAG, "Exception in copyFile() of " + newFileName);
            Log.e(Global.TAG, "Exception in copyFile() " + e.toString());
        }

    }

    /**
     * @param directory
     * @return all the files stored in a given directory and its subdirectories
     */
    private static List<File> getAllFilesInDir(File directory) {
        ArrayList<File> inFiles = new ArrayList<File>();
        if (directory == null) {
            return inFiles;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getAllFilesInDir(file));
            } else {
                inFiles.add(file);
            }
        }
        return inFiles;
    }


}
