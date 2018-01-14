package org.shadowice.flocke.andotp.Receivers;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.Settings;

import java.io.File;

import static android.content.Context.NOTIFICATION_SERVICE;

public abstract class BackupBroadcastReceiver extends BroadcastReceiver {

    protected boolean canSaveBackup(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            notify(context, R.string.app_name, R.string.backup_receiver_read_permission_failed);
            return false;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            notify(context, R.string.app_name, R.string.backup_receiver_write_permission_failed);
            return false;
        }

        Settings settings = new Settings(context);
        File backupDir = new File(settings.getBackupDir());
        if(!backupDir.exists())
            backupDir.mkdirs();

        return true;
    }

    protected void notify(Context context, int resIdTitle, int resIdBody) {
        notify(context, resIdTitle, context.getText(resIdBody).toString());
    }

    protected void notify(Context context, int resIdTitle, String resBody) {
        String channelId = "andOTP_channel";
        NotificationChannel channel = null;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, null)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getText(resIdTitle))
                        .setContentText(resBody);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(channelId, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        builder.setChannelId(channelId);

        int notificationId = 1;
        notificationManager.notify(notificationId, builder.build());
    }
}
