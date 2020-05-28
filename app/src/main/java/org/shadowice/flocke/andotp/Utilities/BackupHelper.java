package org.shadowice.flocke.andotp.Utilities;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.R;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.crypto.SecretKey;

public class BackupHelper {
    private static String backupMimeType(Constants.BackupType type) {
        String mimeType = Constants.BACKUP_MIMETYPE_PLAIN;

        switch(type) {
            case PLAIN_TEXT:
                mimeType = Constants.BACKUP_MIMETYPE_PLAIN;
                break;
            case ENCRYPTED:
                mimeType = Constants.BACKUP_MIMETYPE_CRYPT;
                break;
            case OPEN_PGP:
                mimeType = Constants.BACKUP_MIMETYPE_PGP;
                break;
        }

        return mimeType;
    }

    public static DocumentFile backupFile(Context context, Uri backupLocationUri, Constants.BackupType type) {
        DocumentFile backupFile = null;
        DocumentFile backupLocation = DocumentFile.fromTreeUri(context, backupLocationUri);

        if (backupLocation != null) {
            // Try to find an existing file to overwrite
            backupFile = backupLocation.findFile(BackupHelper.backupFilename(context, type));

            // Try to create a new file
            if (backupFile == null) {
                backupFile = backupLocation.createFile(backupMimeType(type), BackupHelper.backupFilename(context, type));
            }

            // Both failed
            if (backupFile == null)
                Toast.makeText(context, R.string.backup_toast_file_creation_failed, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, R.string.backup_toast_location_access_failed, Toast.LENGTH_LONG).show();
        }

        return backupFile;
    }

    public static String backupFilename(Context context, Constants.BackupType type) {
        Settings settings = new Settings(context);
        switch (type) {
            case PLAIN_TEXT:
                if (settings.getIsAppendingDateTimeToBackups()) {
                    return String.format(Constants.BACKUP_FILENAME_PLAIN_FORMAT, Tools.getDateTimeString());
                } else {
                    return Constants.BACKUP_FILENAME_PLAIN;
                }
            case ENCRYPTED:
                if (settings.getIsAppendingDateTimeToBackups()) {
                    return String.format(Constants.BACKUP_FILENAME_CRYPT_FORMAT, Tools.getDateTimeString());
                } else {
                    return Constants.BACKUP_FILENAME_CRYPT;
                }
            case OPEN_PGP:
                if (settings.getIsAppendingDateTimeToBackups()) {
                    return String.format(Constants.BACKUP_FILENAME_PGP_FORMAT, Tools.getDateTimeString());
                } else {
                    return Constants.BACKUP_FILENAME_PGP;
                }
        }

        return Constants.BACKUP_FILENAME_PLAIN;
    }

    public static Constants.BackupType autoBackupType(Context context) {
        Settings settings = new Settings(context);

        if(!settings.isBackupLocationSet()) {
            return Constants.BackupType.UNAVAILABLE;
        }

        if (!settings.getBackupPasswordEnc().isEmpty()) {
            return Constants.BackupType.ENCRYPTED;
        }

        return Constants.BackupType.UNAVAILABLE;
    }

    public static boolean backupToFile(Context context, Uri uri, String password, SecretKey encryptionKey)
    {
        ArrayList<Entry> entries = DatabaseHelper.loadDatabase(context, encryptionKey);
        String plain = DatabaseHelper.entriesToString(entries);

        try {
            int iter = EncryptionHelper.generateRandomIterations();
            byte[] salt = EncryptionHelper.generateRandom(Constants.ENCRYPTION_IV_LENGTH);

            SecretKey key = EncryptionHelper.generateSymmetricKeyPBKDF2(password, iter, salt);
            byte[] encrypted = EncryptionHelper.encrypt(key, plain.getBytes(StandardCharsets.UTF_8));

            byte[] iterBytes = ByteBuffer.allocate(Constants.INT_LENGTH).putInt(iter).array();
            byte[] data = new byte[Constants.INT_LENGTH + Constants.ENCRYPTION_IV_LENGTH + encrypted.length];

            System.arraycopy(iterBytes, 0, data, 0, Constants.INT_LENGTH);
            System.arraycopy(salt, 0, data, Constants.INT_LENGTH, Constants.ENCRYPTION_IV_LENGTH);
            System.arraycopy(encrypted, 0, data, Constants.INT_LENGTH + Constants.ENCRYPTION_IV_LENGTH, encrypted.length);

            StorageAccessHelper.saveFile(context, uri, data);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
