package org.shadowice.flocke.andotp.Utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import org.shadowice.flocke.andotp.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationHelper {
    public static void notify(Context context, int resIdTitle, int resIdBody) {
        notify(context, resIdTitle, context.getText(resIdBody).toString());
    }

    public static void notify(Context context, int resIdTitle, String resBody) {
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
