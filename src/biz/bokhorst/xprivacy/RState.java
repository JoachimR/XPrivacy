package biz.bokhorst.xprivacy;

import android.content.Context;
import android.os.Process;
import android.util.Log;
import de.reiss.xprivacynative.NativeAccessManagement;

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
        toggleRestriction();

        // <PEM>

        // additionally, take care of native access

        if (mUid < 0) {
            return;
        }

        if (mMethodName == null) {
           // if no method name is given, then just update all blacklists...

            Log.d(NativeAccessManagement.TAG, "=============================================");
            NativeAccessManagement.updateSdcardBlacklist(mUid);
            NativeAccessManagement.updateRecordAudioBlacklist(mUid);
            NativeAccessManagement.updateRunCommandBlacklist(mUid);
            Log.d(NativeAccessManagement.TAG, "=============================================");


        } else if (mMethodName.equals("sdcard") || mMethodName.equals("open")) {
            Log.d(NativeAccessManagement.TAG, "=============================================");
            NativeAccessManagement.updateSdcardBlacklist(mUid);
            Log.d(NativeAccessManagement.TAG, "=============================================");
        }
        else if (mMethodName.equals("startRecording")) {
            Log.d(NativeAccessManagement.TAG, "=============================================");
            NativeAccessManagement.updateRecordAudioBlacklist(mUid);
            Log.d(NativeAccessManagement.TAG, "=============================================");
        }
        else if (mMethodName.equals("start") ) {
            Log.d(NativeAccessManagement.TAG, "=============================================");
            NativeAccessManagement.updateRunCommandBlacklist(mUid);
            Log.d(NativeAccessManagement.TAG, "=============================================");
        }

        // </PEM>

    }

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
