package de.reiss.xprivacynative;

import android.content.Context;
import de.reiss.xprivacynative.util.AssetUtils;

import java.io.File;

public class FileManagement {


    private final static String SDCARD_SUBDIR = "exported_app_assets";
    public static final String FILES_DIR = "/data/data/biz.bokhorst.xprivacy/files";

    /**
     * Make sure that a file from the apk's
     * asset folder is copied to sandbox
     * <p/>
     * Requires root obv
     *
     * @param context
     * @param fileName
     */
    public static String putToFilesDir(Context context, String fileName) {

        // copy asset file from compressed .apk to sdcard
        copyAssetFileToSdCard(context, fileName);

        String res = "";

        // copy from sdcard to app internal folder
        String cmd[] = {"su", "-c",
                " cp " + "/sdcard" + "/" + SDCARD_SUBDIR + "/" + fileName
                        + " "
                        + FILES_DIR + "/" + fileName
        };
        res += Shell.sendShellCommand(cmd);

        res += "\n\n";

        String chmod777[] = {"su", "-c",
                " chmod 777 " + FILES_DIR + "/" + fileName
        };
        res += Shell.sendShellCommand(chmod777);

        // delete from sdcard
        String cmdRemove[] = {"su", "-c",
                " rm -r " + "/sdcard" + "/" + SDCARD_SUBDIR + "/"};
        res += Shell.sendShellCommand(cmdRemove);

        return res;
    }



    /**
     * Copy an asset file of a given app to /sdcard/exported_app_assets/
     *
     * @param context  the Context of the app
     * @param fileName the fileName of the file that is in the assets folder of the given app
     */
    private static void copyAssetFileToSdCard(Context context, String fileName) {
        AssetUtils.copyFileOrDir(context, fileName);
    }


    public static String mkdirFiles() {
        String res = "";

        String mkdir[] = {"su", "-c",
                " mkdir "
                        + " "
                        + FILES_DIR
        };
        res += Shell.sendShellCommand(mkdir);


        String chmod777[] = {"su", "-c",
                " chmod 777 " +
                        FILES_DIR
        };
        res += Shell.sendShellCommand(chmod777);


        return res;
    }

    /**
     * Create sdcardlog.txt
     */
    public static String createSdcardLogFile() {

        String res = "";

        String cmd[] = {
                "su", "-c",
                " touch " + FILES_DIR +
                        "/sdcardlog.txt"
        };
        res += Shell.sendShellCommand(cmd);

        // http://www.user-archiv.de/chmod.html
        // 660 == -rw-rw----

        String chmod[] = {
                "su", "-c",
                " chmod " +
//                        "660" +
                        "777"+
                        FILES_DIR +
                        "/sdcardlog.txt"
        };
        res += Shell.sendShellCommand(chmod);


        return res;

    }


    public static void createDisabledAccessUidsFiles() {
        doCreateDisabledAccessUidsFiles(Global.DISABLED_SDCARDACCESS_UIDS + ".txt");
        doCreateDisabledAccessUidsFiles(Global.DISABLED_RECORDAUDIO_UIDS_TXT);
        doCreateDisabledAccessUidsFiles(Global.DISABLED_SHELLCMDS_UIDS_TXT);
    }

    private static String doCreateDisabledAccessUidsFiles(String filename) {
        String res = "";

        String cmd[] = {
                "su", "-c",
                " touch " + FILES_DIR +
                        "/" + filename
        };
        res += Shell.sendShellCommand(cmd);

        // http://www.user-archiv.de/chmod.html
        // 664 == -rw-rw-r--

        String chmod[] = {
                "su", "-c",
                " chmod " +
//                        "664" +
                        "777"+
                        " " +
                        FILES_DIR +
                        "/" + filename
        };
        res += Shell.sendShellCommand(chmod);


        return res;

    }


    /**
     * Copy /data/system/packages.xml
     */
    public static String copyPackagesXmlFile() {

        String res = "";


        String cmd[] = {"su", "-c",
                " cp " + "/data/system/packages.xml"
                        + " "
                        + FILES_DIR +
                        "/packages.xml"
        };
        res += Shell.sendShellCommand(cmd);

        res += "\n\n";

        // http://www.user-archiv.de/chmod.html
        // 660 == -rw-rw----

        String chmod[] = {
                "su", "-c",
                " chmod " +
//                        "660" +
                        "777"+
                        " " +
                        FILES_DIR +
                        "/packages.xml"
        };
        res += Shell.sendShellCommand(chmod);




        return res;

    }


    public static boolean isFileACompiledLibrary(File file) {
        String ext = getExtension(file);
        if (ext != null) {
            if (ext.equals("so") || ext.equals("o")) {
                return true;
            }
        }
        return false;
    }


    /**
     * @param f
     * @return the extension of a file or null if not found
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }


    public static boolean isAssetFileInTmpDir(String fileName) {

        String check[] = {"su", "-c",
//                "\"" +
                "test -f "
                        + FILES_DIR + "/" + fileName + " && echo 'found' "
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
}
