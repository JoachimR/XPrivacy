package de.reiss.xprivacynative;

import android.util.Log;
import biz.bokhorst.xprivacy.PRestriction;
import biz.bokhorst.xprivacy.PrivacyManager;
import de.reiss.xprivacynative.util.FileUtils;

import java.io.*;

public class NativeAccessManagement {

    public static final String TAG = Global.TAG;//"XPrivacy NativeAccessManagement";

    public static void updateAllBlacklists(int uid) {
        updateSdcardBlacklist(uid);
        updateRecordAudioBlacklist(uid);
        updateRunCommandBlacklist(uid);
    }

    public static void updateSdcardBlacklist(int uid) {

        // If the whole app should not be restricted at all,
        // then no native restriction should be applied either.
        // Therefore remove the app from the blacklist.

        // Using this helper method is necessary
        // because the specific queries for certain function hooks
        // return true even if the whole application is set to false (OFF).
        if (!isAppRestrictedAtAll(uid)) {
            NativeAccessManagement.manageSdCardNativeAccess(false, "" + uid);
            return;
        }

        // Restriction for this app is enabled.
        // Now decide whether or not to add or remove
        // the app from the blacklist by querying the
        // PrivacyService for the respective java restriction.
        PRestriction pRestriction = PrivacyManager.getRestrictionEx(uid, "storage", "sdcard");
        Log.d(NativeAccessManagement.TAG, "PRestriction.restricted for storage->sdcard for uid "
                + uid + " == " + pRestriction.restricted);
        manageSdCardNativeAccess(pRestriction.restricted, "" + uid);
    }


    public static void updateRecordAudioBlacklist(int uid) {

        // If the whole app should not be restricted at all,
        // then no native restriction should be applied either.
        // Therefore remove the app from the blacklist.

        // Using this helper method is necessary
        // because the specific queries for certain function hooks
        // return true even if the whole application is set to false (OFF).
        if (!isAppRestrictedAtAll(uid)) {
            NativeAccessManagement.manageRecordAudioNative(false, "" + uid);
            return;
        }

        // Restriction for this app is enabled.
        // Now decide whether or not to add or remove
        // the app from the blacklist by querying the
        // PrivacyService for the respective java restriction.
        PRestriction pRestriction = PrivacyManager.getRestrictionEx(uid, "media", "startRecording");

        Log.d(NativeAccessManagement.TAG, "PRestriction.restricted for media->startRecording for uid "
                + uid + " == " + pRestriction.restricted);

        manageRecordAudioNative(pRestriction.restricted, "" + uid);
    }


    public static void updateRunCommandBlacklist(int uid) {

        // If the whole app should not be restricted at all,
        // then no native restriction should be applied either.
        // Therefore remove the app from the blacklist.

        // Using this helper method is necessary
        // because the specific queries for certain function hooks
        // return true even if the whole application is set to false (OFF).
        if (!isAppRestrictedAtAll(uid)) {
            NativeAccessManagement.manageRunCommandNative(false, "" + uid);
            return;
        }

        // Restriction for this app is enabled.
        // Now decide whether or not to add or remove
        // the app from the blacklist by querying the
        // PrivacyService for the respective java restriction.
        PRestriction pRestriction = PrivacyManager.getRestrictionEx(uid, "shell", "start");

        Log.d(NativeAccessManagement.TAG, "PRestriction.restricted for media->startRecording for uid "
                + uid + " == " + pRestriction.restricted);

        manageRunCommandNative(pRestriction.restricted, "" + uid);
    }


    /**
     * Check what state the button 'check to restrict' has been set to (ON/OFF).
     *
     * @param uid the uid of the application
     * @return true if the button is set to ON, false otherwise
     */
    private static boolean isAppRestrictedAtAll(int uid) {
        final boolean result = PrivacyManager.getSettingBool(uid, PrivacyManager.cSettingRestricted, false, false);
        if (!result) {
            Log.d(NativeAccessManagement.TAG, "Application is not enabled to be restricted at all");
        }
        return result;
    }


    /**
     * Add or remove the uid for the given application from a blacklist of uids that are not allowed
     * to get results from certain system calls (also when done from native code).
     *
     * @param shouldBeDenied true if the application should be added to the blacklist, false if it should be removed
     * @param mUid           the uid of the application, e.g. 10094
     */
    private static void manageRunCommandNative(boolean shouldBeDenied, String mUid) {
        Log.i(NativeAccessManagement.TAG, "Managing the blacklist '" + Global.DISABLED_SHELLCMDS_UIDS_TXT + "'" +
                " by adding/removing the uid '" + mUid + "'");
        final String filename = "/data/data/biz.bokhorst.xprivacy/files/" + Global.DISABLED_SHELLCMDS_UIDS_TXT;
        updateBlacklistFile(shouldBeDenied, mUid, filename);
    }

    /**
     * Add or remove the uid for the given application from a blacklist of uids that are not allowed
     * to have access to the sdcard (also when done from native code).
     *
     * @param shouldBeDenied true if the application should be added to the blacklist, false if it should be removed
     * @param mUid           the uid of the application, e.g. 10094
     */
    private static void manageSdCardNativeAccess(boolean shouldBeDenied, String mUid) {
        Log.i(NativeAccessManagement.TAG, "Managing the blacklist '" + Global.DISABLED_SDCARDACCESS_UIDS + "'" +
                " by adding/removing the uid '" + mUid + "'");
        final String filename = "/data/data/biz.bokhorst.xprivacy/files/" + Global.DISABLED_SDCARDACCESS_UIDS;
        updateBlacklistFile(shouldBeDenied, mUid, filename);
    }

    /**
     * Add or remove the uid for the given application from a blacklist of uids that are not allowed
     * to record audio using the OpenSL ES library from native code.
     *
     * @param shouldBeDenied true if the application should be added to the blacklist, false if it should be removed
     * @param mUid           the uid of the application, e.g. 10094
     */
    private static void manageRecordAudioNative(boolean shouldBeDenied, String mUid) {
        Log.i(NativeAccessManagement.TAG, "Managing the blacklist '" + Global.DISABLED_RECORDAUDIO_UIDS_TXT + "'" +
                " by adding/removing the uid '" + mUid + "'");
        final String filename = "/data/data/biz.bokhorst.xprivacy/files/" + Global.DISABLED_RECORDAUDIO_UIDS_TXT;
        updateBlacklistFile(shouldBeDenied, mUid, filename);
    }


    private static void updateBlacklistFile(boolean shouldBeDenied, String mUid, String filename) {


        // file has the format:  uid1,uid2,uid3,...
        // The modified system/bin/sdcard process reads from that file
        // when a file is about to be opened

        String contentSoFar = FileUtils.readFromFile(filename);
        contentSoFar = contentSoFar.replaceAll("\\n", "");
        contentSoFar = contentSoFar.replaceAll("\\s+", "");
        Log.d(NativeAccessManagement.TAG, "contentSoFar == " + contentSoFar);

        if (shouldBeDenied) {
            // add the uid to the file
            Log.d(NativeAccessManagement.TAG, "add the uid to the file");

            if (!contentSoFar.contains(mUid + ",")) {
                FileUtils.overwriteFile(filename, contentSoFar + mUid + ",");
            }
        } else {
            // remove the uid from the file
            Log.d(NativeAccessManagement.TAG, "remove the uid from the file");

            String newContent = null;

            if (contentSoFar.contains("," + mUid + ",")) {
                newContent = contentSoFar.replaceAll("," + mUid + ",", ",");

                if (newContent != null && newContent.contains(mUid + ",")) {
                    newContent = newContent.replaceAll(mUid + ",", "");
                }

            } else if (contentSoFar.contains(mUid + ",")) {
                newContent = contentSoFar.replaceAll(mUid + ",", "");
            }

            if (newContent != null) {
                FileUtils.createFile(filename);
                FileUtils.overwriteFile(filename, newContent);
            }
        }
        Log.d(NativeAccessManagement.TAG, "newContent == " + FileUtils.readFromFile(filename));
    }



}
