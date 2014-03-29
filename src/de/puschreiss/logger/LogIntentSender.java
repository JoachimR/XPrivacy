package de.puschreiss.logger;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import biz.bokhorst.xprivacy.XParam;

public class LogIntentSender {

    /**
     * Send a log via intent
     * <p/>
     * to a broadcast receiver.
     * <p/>
     * It is recommend to call this method in the
     * 'after' method of a hook of a method
     *
     * @param param           The object that carries all the parameters
     *                        and the results of the method hook
     * @param className       the class name of the method that is hooked
     * @param restrictionName the category of the restriction. See
     * {@link biz.bokhorst.xprivacy.PrivacyManager}
     * @param methodName      the name of the method where the hook took place
     */
    public static void sendLog(XParam param, String className,
                               String restrictionName, String methodName) {
        try {

            /*
             *  First check if logging should happen
             */

            // one can only get the Context of the application
            // from which some method is currently hooked
            // using the XPosed Framework
            Context currentApplication = AndroidAppHelper.currentApplication();
            if (currentApplication == null) {
                return;
            }

            // don't log if applicationInfo cannot be found
            ApplicationInfo applicationInfo = Utils.getApplicationInfo(currentApplication);
            if (applicationInfo == null) {
                return;
            }

            // don't log if it isn't a user application
            // (necessary! otherwise booting phone will crash)
            if (!Utils.isUserApp(applicationInfo)) {
            /*
                TODO Use BootReceiver to figure out if boot complete?
                TODO
                TODO Did not work so far, couldn't save
                TODO some isBootCompleted state globally...
            */
                return;
            }

            // check several things before logging
            if (!shouldThisAppBeLogged(applicationInfo)
                    && !isRestrictionCritical(restrictionName)) {
                return;
            }


            /*
             *  Now send the log
             */
            String content = prepareLogContent(className, methodName, param);
            Intent intent = new Intent();
            intent.setAction(Global.LOG_NOTIFICATION);
            intent.putExtra("packageName", applicationInfo.packageName);
            intent.putExtra("uid", applicationInfo.uid);
            intent.putExtra("content", content);
            intent.putExtra("creationDate", System.currentTimeMillis());
            currentApplication.sendBroadcast(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String prepareLogContent(String className, String methodName, XParam param) {
        StringBuilder sb = new StringBuilder();

        sb.append(className);
        sb.append(".");
        sb.append(methodName);

        if (param.args != null && param.args.length > 0) {
            sb.append(": args: ");
            for (Object o : param.args) {
                if (o != null) {
                    sb.append(o.toString());
                    sb.append(", ");
                }
            }
        }

        // append "RESULT:" and create some space before and after it
        sb.append(String.format("%1$-10" + "s" + "%1$-10", "RESULT:"));

        // Get whatever the hook of the method has put to the result
        // (can also be the untouched result from the OS)
        // and append it to the result
        Object methodResult = param.getResult();
        if (methodResult == null) {
            sb.append("null");
        } else {
            sb.append(methodResult);
        }
        return sb.toString();
    }

    private static boolean shouldThisAppBeLogged(ApplicationInfo applicationInfo) {
        return doesLoggingForThisAppMakeSense(applicationInfo)
                && XPrivacyLogSetting.isLogForThisAppEnabled(applicationInfo);
    }

    private static boolean isRestrictionCritical(String restrictionName) {
        return restrictionName.equals("system")
                || restrictionName.equals("shell")
                || restrictionName.equals("network");
    }

    private static boolean doesLoggingForThisAppMakeSense(ApplicationInfo applicationInfo) {
        return !applicationInfo.packageName.equalsIgnoreCase("de.puschreiss.logger")
                && !applicationInfo.packageName.equalsIgnoreCase("biz.bokhorst.xprivacy")
                && !applicationInfo.packageName.equalsIgnoreCase("de.robv.android.xposed")
                && !applicationInfo.packageName.startsWith("com.android");
    }

}
