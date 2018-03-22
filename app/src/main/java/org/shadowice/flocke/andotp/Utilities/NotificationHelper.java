/*
 * Copyright (C) 2018 Jakob Nixdorf
 * Copyright (C) 2018 Richy HBM
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.shadowice.flocke.andotp.Utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import org.shadowice.flocke.andotp.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationHelper {
    private static String channelId(Constants.NotificationChannel channel) {
        return "andOTP_" + channel.name().toLowerCase();
    }

    private static void createNotificationChannel(Context context, Constants.NotificationChannel channel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId(channel), context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);

            switch(channel) {
                case BACKUP_FAILED:
                    notificationChannel.setName(context.getString(R.string.notification_channel_name_backup_failed));
                    notificationChannel.setDescription(context.getString(R.string.notification_channel_desc_backup_failed));
                    notificationChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
                    break;
                case BACKUP_SUCCESS:
                    notificationChannel.setName(context.getString(R.string.notification_channel_name_backup_success));
                    notificationChannel.setDescription(context.getString(R.string.notification_channel_desc_backup_success));
                    notificationChannel.setImportance(NotificationManager.IMPORTANCE_LOW);
                    break;
                default:
                    break;
            }

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public static void initializeNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            for (Constants.NotificationChannel channel : Constants.NotificationChannel.values()) {
                NotificationHelper.createNotificationChannel(context, channel);
            }
        }
    }

    public static void notify(Context context, Constants.NotificationChannel channel, int resIdTitle, int resIdBody) {
        notify(context, channel, resIdTitle, context.getText(resIdBody).toString());
    }

    public static void notify(Context context, Constants.NotificationChannel channel , int resIdTitle, String resBody) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, null)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getText(resIdTitle))
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(resBody));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        createNotificationChannel(context, channel);
        builder.setChannelId(channelId(channel));

        int notificationId = 1;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, builder.build());
    }
}
