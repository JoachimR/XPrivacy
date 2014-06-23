package de.reiss.xprivacynative.fileobserve;

import android.content.Context;

import java.util.Random;

/**
 * User: joachim
 * Date: 3/23/14
 * Time: 1:12 PM
 */
public class FileObserveTask {

    private Context mContext;

    public int id;

    public String name;

    public String fileToObserve = "";

    private SdcardLogFileObserver fileObserver;

    public FileObserveTask(Context context, String fileToObserve) {
        this.mContext = context;
        this.id = new Random().nextInt(99);
        this.name = "FileObserver #" + id;
        this.fileToObserve = fileToObserve;
    }

    private boolean isRunning = false;

    public void startWatching() {
        fileObserver = new SdcardLogFileObserver(mContext, fileToObserve);
        fileObserver.startWatching();
        isRunning = true;
    }

    public void stopWatching() {
        if (fileObserver != null && isRunning) {
            fileObserver.stopWatching();
            isRunning = false;
        }
    }

    public boolean isWatching() {
        return isRunning;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileObserveTask that = (FileObserveTask) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }


    @Override
    public String toString() {
        return "File Observe '" + name + '\'' +
                "\nFile to observe:\n'" + fileToObserve + '\'';
    }
}
