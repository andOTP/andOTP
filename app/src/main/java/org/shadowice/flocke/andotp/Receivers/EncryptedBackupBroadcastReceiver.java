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
import org.shadowice.flocke.andotp.Utilities.Settings;
import org.shadowice.flocke.andotp.Utilities.Tools;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.crypto.SecretKey;

public class EncryptedBackupBroadcastReceiver extends BackupBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(!canSaveBackup(context))
            return;

        Settings settings = new Settings(context);
        Uri savePath = Tools.buildUri(settings.getBackupDir(), Constants.BACKUP_FILENAME_CRYPT);

        String password = settings.getBackupPasswordEnc();

        if (password.isEmpty()) {
            notify(context, R.string.app_name, R.string.backup_toast_crypt_password_not_set);
            return;
        }

        SecretKey encryptionKey = null;

        if (settings.getEncryption() == Constants.EncryptionType.KEYSTORE) {
            encryptionKey = KeyStoreHelper.loadEncryptionKeyFromKeyStore(context, false);
        } else {
            notify(context, R.string.app_name, R.string.backup_receiver_custom_encryption_failed );
            return;
        }

        if (Tools.isExternalStorageWritable()) {
            ArrayList<Entry> entries = DatabaseHelper.loadDatabase(context, encryptionKey);
            String plain = DatabaseHelper.entriesToString(entries);

            boolean success = true;

            try {
                SecretKey key = EncryptionHelper.generateSymmetricKeyFromPassword(password);
                byte[] encrypted = EncryptionHelper.encrypt(key, plain.getBytes(StandardCharsets.UTF_8));
                FileHelper.writeBytesToFile(context, savePath, encrypted);
                notify(context, R.string.app_name, context.getText(R.string.backup_receiver_completed) + savePath.getPath());
            } catch (Exception e) {
                e.printStackTrace();
                notify(context, R.string.app_name, R.string.backup_toast_export_failed);
            }
        } else {
            notify(context, R.string.app_name, R.string.backup_toast_storage_not_accessible);
        }
    }
}
