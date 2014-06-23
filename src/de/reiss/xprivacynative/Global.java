package de.reiss.xprivacynative;

import de.reiss.xprivacynative.fileobserve.FileObserveTask;

import java.util.ArrayList;

public class Global {

    public static final String TAG = "de.reiss.xprivacynative";


    public static final String DISABLED_SDCARDACCESS_UIDS = "disabled_sdcardaccess_uids.txt";
    public static final String DISABLED_RECORDAUDIO_UIDS_TXT = "disabled_recordaudio_uids.txt";
    public static final String DISABLED_SHELLCMDS_UIDS_TXT = "disabled_shellcmds_uids.txt";

    public static ArrayList<FileObserveTask> fileObserveTasks;

}
