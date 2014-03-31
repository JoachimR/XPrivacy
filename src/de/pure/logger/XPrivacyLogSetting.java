package de.pure.logger;

import android.content.pm.ApplicationInfo;
import biz.bokhorst.xprivacy.PSetting;
import biz.bokhorst.xprivacy.PrivacyManager;

import java.util.List;

public class XPrivacyLogSetting {

    public static boolean isLogForThisAppEnabled(ApplicationInfo applicationInfo) {
        if (applicationInfo == null) {
            return false;
        }
        boolean shouldLog = false;
        // this returns wrong values...
//      shouldLog = PrivacyManager.getSettingBool(applicationInfo.uid,
//              PrivacyManager.cSettingAppLog, false, true);
        // ... thus get it via the list
        List<PSetting> listAppSetting = PrivacyManager.getSettingList(applicationInfo.uid);
        for (PSetting setting : listAppSetting) {
            if (PrivacyManager.cSettingAppLog.equals(setting.name)) {
                shouldLog = Boolean.parseBoolean(setting.value);
            }
        }
        return shouldLog;
    }
}
