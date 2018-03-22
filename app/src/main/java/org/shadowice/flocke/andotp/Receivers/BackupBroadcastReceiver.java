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

package org.shadowice.flocke.andotp.Receivers;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.NotificationHelper;
import org.shadowice.flocke.andotp.Utilities.Settings;

import java.io.File;

public abstract class BackupBroadcastReceiver extends BroadcastReceiver {

    protected boolean canSaveBackup(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            NotificationHelper.notify(context, Constants.NotificationChannel.BACKUP_FAILED, R.string.backup_receiver_title_backup_failed, R.string.backup_receiver_read_permission_failed);
            return false;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            NotificationHelper.notify(context, Constants.NotificationChannel.BACKUP_FAILED, R.string.backup_receiver_title_backup_failed, R.string.backup_receiver_write_permission_failed);
            return false;
        }

        Settings settings = new Settings(context);
        File backupDir = new File(settings.getBackupDir());
        if(!backupDir.exists())
            backupDir.mkdirs();

        return true;
    }
}
