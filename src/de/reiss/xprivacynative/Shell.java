package de.reiss.xprivacynative;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Shell {


    public static String sendShellCommand(String[] cmd) {
        Log.d(Global.TAG, "\n###executing: " + cmd[0] + "###");
        String allText = "";
        try {
            String line;
            Process process = new ProcessBuilder(cmd).start();
            BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            try {
                process.waitFor();
            } catch (InterruptedException ex) {
                Logger.getLogger(Shell.class.getName()).log(Level.SEVERE, null, ex);
            }
            while ((line = stdErr.readLine()) != null) {
                allText = allText + "\n" + line;
            }
            while ((line = stdOut.readLine()) != null) {
                allText = allText + "\n" + line;
                while ((line = stdErr.readLine()) != null) {
                    allText = allText + "\n" + line;
                }
            }
            return allText;
        } catch (IOException ex) {
            Log.e(Global.TAG, "Problem while executing in " +
                    "Shell.sendShellCommand() Received " + allText);
            ex.printStackTrace();
            return "CritERROR!!!";
        }

    }
}
