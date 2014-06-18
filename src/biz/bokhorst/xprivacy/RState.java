package biz.bokhorst.xprivacy;

import android.content.Context;
import android.os.Process;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RState {
    public int mUid;
    public String mRestrictionName;
    public String mMethodName;
    public boolean restricted;
    public boolean asked = false;
    public boolean partialRestricted = false;
    public boolean partialAsk = false;

    public RState(int uid, String restrictionName, String methodName) {
        mUid = uid;
        mRestrictionName = restrictionName;
        mMethodName = methodName;

        int userId = Util.getUserId(Process.myUid());

        // Get if on demand
        boolean onDemand = PrivacyManager.getSettingBool(userId, PrivacyManager.cSettingOnDemand, true, false);
        if (onDemand)
            onDemand = PrivacyManager.getSettingBool(-uid, PrivacyManager.cSettingOnDemand, false, false);

        boolean allRestricted = true;
        boolean someRestricted = false;
        boolean allAsk = true;
        boolean someAsk = false;

        if (methodName == null) {
            if (restrictionName == null) {
                // Examine the category state
                someAsk = onDemand;
                for (String rRestrictionName : PrivacyManager.getRestrictions()) {
                    PRestriction query = PrivacyManager.getRestrictionEx(uid, rRestrictionName, null);
                    allRestricted = (allRestricted && query.restricted);
                    someRestricted = (someRestricted || query.restricted);
                    allAsk = (allAsk && !query.asked);
                    someAsk = (someAsk || !query.asked);
                }
                asked = !onDemand;
            } else {
                // Examine the category/method states
                PRestriction query = PrivacyManager.getRestrictionEx(uid, restrictionName, null);
                someRestricted = query.restricted;
                someAsk = !query.asked;
                for (PRestriction restriction : PrivacyManager.getRestrictionList(uid, restrictionName)) {
                    allRestricted = (allRestricted && restriction.restricted);
                    someRestricted = (someRestricted || restriction.restricted);
                    allAsk = (allAsk && !restriction.asked);
                    someAsk = (someAsk || !restriction.asked);
                }
                asked = query.asked;
            }
        } else {
            // Examine the method state
            PRestriction query = PrivacyManager.getRestrictionEx(uid, restrictionName, methodName);
            allRestricted = query.restricted;
            someRestricted = false;
            asked = query.asked;
        }

        restricted = (allRestricted || someRestricted);
        asked = (!onDemand || !PrivacyManager.isApplication(uid) || asked);
        partialRestricted = (!allRestricted && someRestricted);
        partialAsk = (onDemand && PrivacyManager.isApplication(uid) && !allAsk && someAsk);
    }


    /**
     * @return new restricted state
     */
    public boolean toggleRestriction() {
        if (mMethodName == null) {
            // Get restrictions to change
            List<String> listRestriction;
            if (mRestrictionName == null)
                listRestriction = PrivacyManager.getRestrictions();
            else {
                listRestriction = new ArrayList<String>();
                listRestriction.add(mRestrictionName);
            }

            // Change restriction
            if (restricted) {
                PrivacyManager.deleteRestrictions(mUid, mRestrictionName, (mRestrictionName == null));
            } else {
                for (String restrictionName : listRestriction)
                    PrivacyManager.setRestriction(mUid, restrictionName, null, true, false);
                PrivacyManager.updateState(mUid);
            }
        } else {
            PRestriction query = PrivacyManager.getRestrictionEx(mUid, mRestrictionName, null);
            PrivacyManager.setRestriction(mUid, mRestrictionName, mMethodName, !restricted, query.asked);
            PrivacyManager.updateState(mUid);
        }

        return !restricted;
    }

    public void toggleRestriction(Context xprivacyContext) {
        final boolean newRestrictedState = toggleRestriction();

        // <PEM>

        // additionally, take care of native sdcard access

        if (mUid < 0 || mMethodName == null) {
            return;
        }

        if (mMethodName.equals("sdcard")) {

            final String filename = "disabled_sdcardaccess_uids.txt";
            // file has the format:  uid1,uid2,uid3,...
            // The modified system/bin/sdcard process reads from that file
            // when a file is about to be opened


            final String contentSoFar = readFromFileUsingContext(xprivacyContext, filename);

            if (newRestrictedState) {
                // add the uid to the file
                if (!contentSoFar.startsWith(mUid + ",") && !contentSoFar.contains("," + mUid + ",")) {
                    appendToFileUsingContext(xprivacyContext, filename, mUid + ",");
                }
            } else {
                // remove the uid from the file

                String newContent = null;
                if (contentSoFar.startsWith(mUid + ",")) {
                    // remove uidX, in the file with the format uidX,uid2,uid3,...
                    newContent = contentSoFar.replaceAll(mUid + ",", "");
                } else if (contentSoFar.contains("," + mUid + ",")) {
                    // replace ,uidX, with , in the file with the format uid1,uidX,uid3,...
                    newContent = contentSoFar.replaceAll("," + mUid + ",", ",");
                }

                if (newContent != null) {
                    createFileUsingContext(xprivacyContext, filename);
                    appendToFileUsingContext(xprivacyContext, filename, newContent);
                }
            }
        }
        else if (mMethodName.equals("startRecording")) {

            final String filename = "disabled_recordaudio_uids.txt";
            // file has the format:  uid1,uid2,uid3,...
            // The modified system/lib/libOpenSLES library reads from that file
            // when native recording is about to take place


            final String contentSoFar = readFromFileUsingContext(xprivacyContext, filename);

            if (newRestrictedState) {
                // add the uid to the file
                if (!contentSoFar.startsWith(mUid + ",") && !contentSoFar.contains("," + mUid + ",")) {
                    appendToFileUsingContext(xprivacyContext, filename, mUid + ",");
                }
            } else {
                // remove the uid from the file

                String newContent = null;
                if (contentSoFar.startsWith(mUid + ",")) {
                    // remove uidX, in the file with the format uidX,uid2,uid3,...
                    newContent = contentSoFar.replaceAll(mUid + ",", "");
                } else if (contentSoFar.contains("," + mUid + ",")) {
                    // replace ,uidX, with , in the file with the format uid1,uidX,uid3,...
                    newContent = contentSoFar.replaceAll("," + mUid + ",", ",");
                }

                if (newContent != null) {
                    createFileUsingContext(xprivacyContext, filename);
                    appendToFileUsingContext(xprivacyContext, filename, newContent);
                }
            }
        }

        // </PEM>

    }

    // <PEM>

    private String readFromFileUsingContext(Context context, String fileName) {
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream fis = context.openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String read;
            if (fis != null) {
                while ((read = reader.readLine()) != null
                        && sb.length() <= 1024) {
                    sb.append(read + "\n");
                }
                fis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString().trim();
    }

    private boolean appendToFileUsingContext(Context context, String fileName, String textToAppend) {
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_APPEND);
            fos.write(textToAppend.getBytes());
            fos.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean createFileUsingContext(Context context, String fileName) {
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // </PEM>

    public void toggleAsked() {
        asked = !asked;
        if (mRestrictionName == null)
            PrivacyManager.setSetting(mUid, PrivacyManager.cSettingOnDemand, Boolean.toString(!asked));
        else {
            // Avoid re-doing all exceptions for dangerous functions
            List<PRestriction> listPRestriction = new ArrayList<PRestriction>();
            listPRestriction.add(new PRestriction(mUid, mRestrictionName, mMethodName, restricted, asked));
            PrivacyManager.setRestrictionList(listPRestriction);
            PrivacyManager.setSetting(mUid, PrivacyManager.cSettingState, Integer.toString(ActivityMain.STATE_CHANGED));
            PrivacyManager.setSetting(mUid, PrivacyManager.cSettingModifyTime,
                    Long.toString(System.currentTimeMillis()));
        }
    }

}
