package org.shadowice.flocke.andotp.Utilities;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.R;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.SecretKey;

public class BackupHelper {
    public static class BackupFile {
        public DocumentFile file = null;
        public int errorMessage;
    }

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

    public static BackupFile backupFile(Context context, Uri backupLocationUri, Constants.BackupType type) {
        BackupFile backupFile = new BackupFile();
        DocumentFile backupLocation = DocumentFile.fromTreeUri(context, backupLocationUri);

        if (backupLocation != null) {
            // Try to find an existing file to overwrite
            backupFile.file = backupLocation.findFile(BackupHelper.backupFilename(context, type));

            // Try to create a new file
            if (backupFile.file == null) {
                backupFile.file = backupLocation.createFile(backupMimeType(type), BackupHelper.backupFilename(context, type));
            }

            // Both failed
            if (backupFile.file == null)
                backupFile.errorMessage = R.string.backup_toast_file_creation_failed;
        } else {
            backupFile.errorMessage = R.string.backup_toast_location_access_failed;
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

    public static void backupToFileAsync(Context context, Uri uri, String password, SecretKey encryptionKey, boolean silent) {
        ArrayList<Entry> entries = DatabaseHelper.loadDatabase(context, encryptionKey);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        BackupCrypt runnable = new BackupCrypt(context, uri, password, entries, silent);

        executor.execute(runnable);
    }

    public static boolean backupToFile(Context context, Uri uri, String password, SecretKey encryptionKey) {
        ArrayList<Entry> entries = DatabaseHelper.loadDatabase(context, encryptionKey);
        return backupToFile(context, uri, password, entries);
    }

    public static boolean backupToFile(Context context, Uri uri, String password, ArrayList<Entry> entries)
    {

        String plain = DatabaseHelper.entriesToString(entries);

        boolean success = true;

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

            success = StorageAccessHelper.saveFile(context, uri, data);
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }

        return success;
    }

    public static class BackupCrypt implements Runnable {
        final private Context context;
        final private Uri uri;
        final private String password;
        final private ArrayList<Entry> entries;
        final private boolean silent;

        public BackupCrypt(Context context, Uri uri, String password, ArrayList<Entry> entries, boolean silent) {
            this.context = context;
            this.uri = uri;
            this.password = password;
            this.entries = entries;
            this.silent = silent;
        }

        @Override
        public void run() {
            boolean success = backupToFile(context, uri, password, entries);

            if (!silent) {
                if (success) {
                    postMessage(R.string.backup_toast_export_success);
                } else {
                    postMessage(R.string.backup_toast_export_failed);
                }
            }
        }

        void postMessage(int msgId) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, msgId, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public static class SaveStringToFile implements Runnable {
        final private Context context;
        final private Uri uri;
        final private String data;

        public SaveStringToFile(Context context, Uri uri, String data) {
            this.context = context;
            this.uri = uri;
            this.data = data;
        }

        @Override
        public void run() {
            boolean success = StorageAccessHelper.saveFile(context, uri, data);

            if (success)
                postMessage(R.string.backup_toast_export_success);
            else
                postMessage(R.string.backup_toast_export_failed);
        }

        void postMessage(int msgId) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, msgId, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
