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

    public final static String TMP_DIR = "/data/local/tmp";
    public final static String SDCARD_DIR = Environment.getExternalStorageDirectory() + "/" + SUBDIR;


    /**
     * Make sure that a file from the apk's
     * asset folder is copied to
     * an Android internal folder.
     * <p/>
     * Requires root obv
     *
     * @param context
     * @param fileName
     */
    public static String putToTmpDir(Context context, String fileName) {

        // copy asset file from compressed .apk to sdcard
        copyAssetFileToSdCard(context, fileName);

        String res = "";

        // copy from sdcard to internal tmp folder (requires root)
        String cmd[] = {"su", "-c",
                        " cp " + "/sdcard" + "/" + SUBDIR + "/" + fileName
                        + " "
                        + TMP_DIR + "/" + fileName
        };
        res += Shell.sendShellCommand(cmd);

        res += "\n\n";

        String chmod777[] = {"su", "-c",
                        " chmod 777 " + TMP_DIR + "/" + fileName
        };
        res += Shell.sendShellCommand(chmod777);

        // delete from sdcard
        String cmdRemove[] = {
                " rm -r " + "/sdcard" + "/" + SUBDIR + "/"};
        res += Shell.sendShellCommand(cmdRemove);

        return res;
    }


    public static boolean isAssetFileInTmpDir(String fileName) {

        String check[] = {"su", "-c",
//                "\"" +
                        "test -f "
                        + TMP_DIR + "/" + fileName + " && echo 'found' "
                        + " || echo 'not found' "
//                                +"\""
        };
        String result = Shell.sendShellCommand(check);
        if (result != null && result.equals("found")) {
            System.out.println(result);
            return true;
        }
        return false;

    }

    /**
     * Copy an asset file of a given app to /sdcard/exported_app_assets/
     *
     * @param context  the Context of the app
     * @param fileName the fileName of the file that is in the assets folder of the given app
     */
    private static void copyAssetFileToSdCard(Context context, String fileName) {
        copyFileOrDir(context, fileName);
    }

    private static void copyFileOrDir(Context context, String path) {
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

    private static void copyFile(Context context, String filename) {
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
