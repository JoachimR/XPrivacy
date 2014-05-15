package de.reiss.xprivacynative.util;

import java.io.File;

public class FileUtils {


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
