package org.shadowice.flocke.andotp.Receivers;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;
import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.DatabaseHelper;
import org.shadowice.flocke.andotp.Utilities.FileHelper;
import org.shadowice.flocke.andotp.Utilities.KeyStoreHelper;
import org.shadowice.flocke.andotp.Utilities.Settings;
import org.shadowice.flocke.andotp.Utilities.Tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.crypto.SecretKey;

public class OpenPGPBackupBroadcasrReceiver extends BackupBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(!canSaveBackup(context))
            return;

        Settings settings = new Settings(context);

        Uri savePath = Tools.buildUri(settings.getBackupDir(), Constants.BACKUP_FILENAME_PLAIN);
        File dir = new File(savePath.getPath());
        if(!dir.exists()) {
            dir.mkdirs();
        }

        SecretKey encryptionKey;

        if (settings.getEncryption() == Constants.EncryptionType.KEYSTORE) {
            encryptionKey = KeyStoreHelper.loadEncryptionKeyFromKeyStore(context, false);
        } else {
            notify(context, R.string.app_name, R.string.backup_receiver_custom_encryption_failed);
            return;
        }

        String PGPProvider = settings.getOpenPGPProvider();

        if (PGPProvider.isEmpty()) {
            notify(context, R.string.app_name, R.string.backup_desc_openpgp_keyid /* need to set pgp provider */);
            return;
        }

        if(settings.getOpenPGPKey() == 0) {
            notify(context, R.string.app_name, R.string.backup_desc_openpgp_keyid);
            return;
        }

        OpenPgpServiceConnection pgpServiceConnection = new OpenPgpServiceConnection(context.getApplicationContext(), PGPProvider);
        pgpServiceConnection.bindToService();

        ArrayList<Entry> entries = DatabaseHelper.loadDatabase(context, encryptionKey);
        String plainJSON = DatabaseHelper.entriesToString(entries);

        Intent encryptIntent = new Intent();

        if (settings.getOpenPGPSign()) {
            encryptIntent.setAction(OpenPgpApi.ACTION_SIGN_AND_ENCRYPT);
            encryptIntent.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, settings.getOpenPGPKey());
        } else {
            encryptIntent.setAction(OpenPgpApi.ACTION_ENCRYPT);
        }

        encryptIntent.putExtra(OpenPgpApi.EXTRA_KEY_IDS, new long[]{settings.getOpenPGPKey()});
        encryptIntent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);


        InputStream is = new ByteArrayInputStream(plainJSON.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        OpenPgpApi api = new OpenPgpApi(context, pgpServiceConnection.getService());
        Intent result = api.executeApi(encryptIntent, is, os);
        handleOpenPGPResult(context, result, os, savePath, Constants.INTENT_BACKUP_ENCRYPT_PGP);

    }


    public void handleOpenPGPResult(Context context, Intent result, ByteArrayOutputStream os, Uri file, int requestCode) {
        if (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR) == OpenPgpApi.RESULT_CODE_SUCCESS) {
            if (requestCode == Constants.INTENT_BACKUP_ENCRYPT_PGP) {
                if (os != null) {
                    if (Tools.isExternalStorageWritable()) {
                        boolean success = FileHelper.writeStringToFile(context, file, new String(os.toByteArray(), StandardCharsets.UTF_8));

                        if (success) {
                            notify(context, R.string.app_name, context.getText(R.string.backup_receiver_completed) + file.getPath());
                        } else {
                            notify(context, R.string.app_name, R.string.backup_toast_export_failed);
                        }
                    } else {
                        notify(context, R.string.app_name, R.string.backup_toast_storage_not_accessible);
                    }
                }
            }
        } else {
            OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
            notify(context, R.string.app_name, R.string.backup_toast_export_failed /* error with openpgp */);
        }
    }
}
