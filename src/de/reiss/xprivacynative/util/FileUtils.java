package de.reiss.xprivacynative.util;

import android.util.Log;
import de.reiss.xprivacynative.Global;

import java.io.*;

public class FileUtils {


    public static boolean createFile(String pathAndFileName) {

        File file = new File(pathAndFileName);
        try {
            FileOutputStream f = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(Global.TAG, "error: " + e.getMessage());
            return false;
        }
        return true;
    }


    public static boolean overwriteFile(String pathAndFileName, String textToAppend) {
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
            Log.e(Global.TAG, "error: " + e.getMessage());
            return false;
        }
        return true;
    }

    public static String readFromFile(String pathAndFileName) {
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
            Log.e(Global.TAG, "error: " + e.getMessage());
            return "";
        }

        return text.toString();
    }
}
