package de.reiss.xprivacynative;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;
import de.reiss.xprivacynative.Shell;
import de.reiss.xprivacynative.util.AssetUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadLibraryDependencies {

    public static String perform(Context mContext, ArrayList<ApplicationInfo> appInfos,
                                 String libraryName) {

        String libName = "lib" + libraryName + ".so";

        try {
            if (mContext == null || TextUtils.isEmpty(libraryName)) {
                return "";
            }

            String result = "";

            for (ApplicationInfo oneAppInfo : appInfos) {

                final String sdcard = "sdcard";
                //  Environment.getExternalStorageDirectory() returns wrong path for shell

                // TODO use objdump -p $f | awk '/ NEEDED / { print $2 }'

                // TODO awk does not work....

                final String outFilePath = sdcard + "/" + libName + ".dump.txt";
                final String suCmd = ""
                        + " ."
                        + FileManagement.FILES_DIR + "/" + "objdump"
                        + " --all-headers "
                        + oneAppInfo.nativeLibraryDir + "/"
                        + libName
                        + " > "
                        + outFilePath;

                String objdumpCmd[] = {"su", "-c", suCmd};
                String objdumpCmdResult = Shell.sendShellCommand(objdumpCmd);
                //System.out.println(suCmd);
                //System.out.println(objdumpCmdResult);

                String catCmd[] = {"su", "-c", " cat " + outFilePath};
                String catResult = Shell.sendShellCommand(catCmd);

                if (!TextUtils.isEmpty(catResult)) {

                    for (String lib : readAllNeededLibrariesFromObjdump(catResult)) {
                        result += "\n" + lib;
                    }
                    //System.out.println(result);

                }
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "\n" + "...nothing could be found...";
    }


    private static ArrayList<String> readAllNeededLibrariesFromObjdump(String objdump) {
        ArrayList<String> includedLibraries = new ArrayList<String>();

        Pattern p = Pattern.compile("NEEDED\\s*.*");
        Matcher m = p.matcher(objdump);
        while (m.find()) {
            String matched = m.group(0);

            String noNEEDED = matched.replaceAll("NEEDED", "");
            String noWhiteSpace = noNEEDED.replaceAll("\\s+", "");

            includedLibraries.add(noWhiteSpace);
        }

        return includedLibraries;
    }

}
