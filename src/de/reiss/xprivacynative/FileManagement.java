package de.reiss.xprivacynative;

import de.reiss.xprivacynative.Global;
import de.reiss.xprivacynative.Shell;

import java.io.File;

public class FileManagement {


    public static String mkdirFiles() {
        String res = "";

        String mkdir[] = {"su", "-c",
                " mkdir "
                        + " "
                        + "/data/data/biz.bokhorst.xprivacy/files"
        };
        res += Shell.sendShellCommand(mkdir);


        String chmod777[] = {"su", "-c",
                " chmod 777 /data/data/biz.bokhorst.xprivacy/files"
        };
        res += Shell.sendShellCommand(chmod777);


        return res;
    }

    /**
     * Create /data/data/biz.bokhorst.xprivacy/sdcardlog.txt
     * Requires root
     */
    public static String createSdcardLogFile() {

        String res = "";

        String cmd[] = {
                "su", "-c",
                " touch " + "/data/data/biz.bokhorst.xprivacy/files/sdcardlog.txt"
        };
        res += Shell.sendShellCommand(cmd);

        // http://www.user-archiv.de/chmod.html
        // 660 == -rw-rw----

        String chmod[] = {
                "su", "-c",
                " chmod " +
//                        "660" +
                        "777"+
                        " /data/data/biz.bokhorst.xprivacy/files/sdcardlog.txt"
        };
        res += Shell.sendShellCommand(chmod);


        return res;

    }


    /**
     * Requires root
     */
    public static void createDisabledAccessUidsFiles() {
        doCreateDisabledAccessUidsFiles(Global.DISABLED_SDCARDACCESS_UIDS + ".txt");
        doCreateDisabledAccessUidsFiles(Global.DISABLED_RECORDAUDIO_UIDS_TXT);
        doCreateDisabledAccessUidsFiles(Global.DISABLED_SHELLCMDS_UIDS_TXT);


    }

    private static String doCreateDisabledAccessUidsFiles(String filename) {
        String res = "";

        String cmd[] = {
                "su", "-c",
                " touch " + "/data/data/biz.bokhorst.xprivacy/files/" + filename
        };
        res += Shell.sendShellCommand(cmd);

        // http://www.user-archiv.de/chmod.html
        // 664 == -rw-rw-r--

        String chmod[] = {
                "su", "-c",
                " chmod " +
//                        "664" +
                        "777"+
                        " /data/data/biz.bokhorst.xprivacy/files/" + filename
        };
        res += Shell.sendShellCommand(chmod);


        return res;

    }


    /**
     * Copy /data/system/packages.xml to /data/data/biz.bokhorst.xprivacy/files/packages.xml
     * Requires root obv
     */
    public static String copyPackagesXmlFile() {

        String res = "";


        String cmd[] = {"su", "-c",
                " cp " + "/data/system/packages.xml"
                        + " "
                        + "/data/data/biz.bokhorst.xprivacy/files/packages.xml"
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
                        " /data/data/biz.bokhorst.xprivacy/files/packages.xml"
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
}
