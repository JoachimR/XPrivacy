package biz.bokhorst.xprivacy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import de.reiss.xprivacynative.nativesdcard.util.AssetUtils;
import de.reiss.xprivacynative.nativesdcard.util.FileUtils;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent bootIntent) {
        // Start boot update
        Intent changeIntent = new Intent();
        changeIntent.setClass(context, UpdateService.class);
        changeIntent.putExtra(UpdateService.cAction, UpdateService.cActionBoot);
        context.startService(changeIntent);

        initFilesInBackground(context);

        // Check if Xposed enabled
        if (Util.isXposedEnabled()) {
            context.sendBroadcast(new Intent("biz.bokhorst.xprivacy.action.ACTIVE"));
        } else {
            // Create Xposed installer intent
            Intent xInstallerIntent = context.getPackageManager().getLaunchIntentForPackage(
                    "de.robv.android.xposed.installer");

            PendingIntent pi = (xInstallerIntent == null ? null : PendingIntent.getActivity(context, 0,
                    xInstallerIntent, PendingIntent.FLAG_UPDATE_CURRENT));

            // Build notification
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
            notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
            notificationBuilder.setContentTitle(context.getString(R.string.app_name));
            notificationBuilder.setContentText(context.getString(R.string.app_notenabled));
            notificationBuilder.setWhen(System.currentTimeMillis());
            notificationBuilder.setAutoCancel(true);
            if (pi != null)
                notificationBuilder.setContentIntent(pi);
            Notification notification = notificationBuilder.build();

            // Display notification
            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(Util.NOTIFY_NOTXPOSED, notification);
        }

    }

    private void initFilesInBackground(final Context ctx) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                createSdcardLogFile();
                copyObjdumpBinary(ctx);
                copyPackagesXml();
            }
        }).start();
    }

    private void createSdcardLogFile() {
        String s = FileUtils.createSdcardLogFile();
    }

    /**
     * copy objdump to a folder where it can be executed from the shell
     */
    private void copyObjdumpBinary(final Context ctx) {
        AssetUtils.putToInternalTmpDir(ctx, "objdump");
    }

    /**
     * copy /data/system/packages.xml to /data/data/biz.bokhorst.xprivacy/ files
     */
    private void copyPackagesXml() {
        String s = FileUtils.copyPackagesXmlFile();
    }


}
