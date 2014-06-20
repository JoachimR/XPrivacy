package de.reiss.xprivacynative.util;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import biz.bokhorst.xprivacy.R;

public class Utils {

    /**
     * @param notifyID            Sets an ID for the notification, so it can be updated
     * @param context
     * @param notificationTitle
     * @param notificationMessage
     */
    public static void notifyOnStatusBar(int notifyID, Context context, String notificationTitle,
                                         String notificationMessage) {


        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setSmallIcon(R.drawable.ic_launcher);

        // Because the ID remains unchanged, the existing notification is
        // updated.
        mNotificationManager.notify(
                notifyID,
                mNotifyBuilder.build());

    }
}
