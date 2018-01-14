package org.shadowice.flocke.andotp.Receivers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.DatabaseHelper;
import org.shadowice.flocke.andotp.Utilities.FileHelper;
import org.shadowice.flocke.andotp.Utilities.KeyStoreHelper;
import org.shadowice.flocke.andotp.Utilities.Settings;
import org.shadowice.flocke.andotp.Utilities.Tools;

import java.util.ArrayList;

import javax.crypto.SecretKey;

//Test with: adb shell am broadcast -n org.shadowice.flocke.andotp/.Receivers.PlainTextBackupBroadcastReceiver
public class PlainTextBackupBroadcastReceiver extends BackupBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(!canSaveBackup(context))
            return;

        Settings settings = new Settings(context);

        Uri savePath = Tools.buildUri(settings.getBackupDir(), Constants.BACKUP_FILENAME_PLAIN);

        SecretKey encryptionKey = null;

        if (settings.getEncryption() == Constants.EncryptionType.KEYSTORE) {
            encryptionKey = KeyStoreHelper.loadEncryptionKeyFromKeyStore(context, false);
        } else {
            notify(context, R.string.app_name, R.string.backup_receiver_custom_encryption_failed);
            return;
        }

        if (Tools.isExternalStorageWritable()) {
            ArrayList<Entry> entries = DatabaseHelper.loadDatabase(context, encryptionKey);

            if (FileHelper.writeStringToFile(context, savePath, DatabaseHelper.entriesToString(entries))) {
                notify(context, R.string.app_name, context.getText(R.string.backup_receiver_completed) + savePath.getPath());
            } else {
                notify(context, R.string.app_name, R.string.backup_toast_export_failed);
            }
        } else {
            notify(context, R.string.app_name, R.string.backup_toast_storage_not_accessible);
        }
    }
}
