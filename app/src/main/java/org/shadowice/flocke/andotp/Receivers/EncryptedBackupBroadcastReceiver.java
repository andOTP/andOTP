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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.DatabaseHelper;
import org.shadowice.flocke.andotp.Utilities.EncryptionHelper;
import org.shadowice.flocke.andotp.Utilities.FileHelper;
import org.shadowice.flocke.andotp.Utilities.KeyStoreHelper;
import org.shadowice.flocke.andotp.Utilities.NotificationHelper;
import org.shadowice.flocke.andotp.Utilities.Settings;
import org.shadowice.flocke.andotp.Utilities.Tools;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.crypto.SecretKey;

// Use the following command to test in the dev version:
//   adb shell am broadcast -a org.shadowice.flocke.andotp.broadcast.ENCRYPTED_BACKUP org.shadowice.flocke.andotp.dev
public class EncryptedBackupBroadcastReceiver extends BackupBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Settings settings = new Settings(context);

        if (settings.isEncryptedBackupBroadcastEnabled()) {
            if (!canSaveBackup(context))
                return;

            Uri savePath = Tools.buildUri(settings.getBackupDir(), FileHelper.backupFilename(context, Constants.BackupType.ENCRYPTED));

            String password = settings.getBackupPasswordEnc();

            if (password.isEmpty()) {
                NotificationHelper.notify(context, Constants.NotificationChannel.BACKUP_FAILED, R.string.backup_receiver_title_backup_failed, R.string.backup_toast_crypt_password_not_set);
                return;
            }

            SecretKey encryptionKey = null;

            if (settings.getEncryption() == Constants.EncryptionType.KEYSTORE) {
                encryptionKey = KeyStoreHelper.loadEncryptionKeyFromKeyStore(context, false);
            } else {
                NotificationHelper.notify(context, Constants.NotificationChannel.BACKUP_FAILED, R.string.backup_receiver_title_backup_failed, R.string.backup_receiver_custom_encryption_failed);
                return;
            }

            if (Tools.isExternalStorageWritable()) {
                ArrayList<Entry> entries = DatabaseHelper.loadDatabase(context, encryptionKey);
                String plain = DatabaseHelper.entriesToString(entries);

                try {
                    SecretKey key = EncryptionHelper.generateSymmetricKeyFromPassword(password);
                    byte[] encrypted = EncryptionHelper.encrypt(key, plain.getBytes(StandardCharsets.UTF_8));
                    FileHelper.writeBytesToFile(context, savePath, encrypted);
                    NotificationHelper.notify(context, Constants.NotificationChannel.BACKUP_SUCCESS, R.string.backup_receiver_title_backup_success, savePath.getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                    NotificationHelper.notify(context, Constants.NotificationChannel.BACKUP_FAILED, R.string.backup_receiver_title_backup_failed, R.string.backup_toast_export_failed);
                }
            } else {
                NotificationHelper.notify(context, Constants.NotificationChannel.BACKUP_FAILED, R.string.backup_receiver_title_backup_failed, R.string.backup_toast_storage_not_accessible);
            }
        } else {
            NotificationHelper.notify(context, Constants.NotificationChannel.BACKUP_FAILED, R.string.backup_receiver_title_backup_failed, R.string.backup_receiver_encrypted_disabled);
        }
    }
}
