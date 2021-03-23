package org.shadowice.flocke.andotp.Tasks;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.BackupHelper;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.Settings;
import org.shadowice.flocke.andotp.Utilities.StorageAccessHelper;

public abstract class GenericBackupTask extends UiBasedBackgroundTask<GenericBackupTask.BackupTaskResult> {
    protected final Context applicationContext;
    protected final Settings settings;
    protected final Constants.BackupType type;
    protected Uri uri;

    public GenericBackupTask(Context context, @Nullable Uri uri) {
        super(BackupTaskResult.failure());

        this.applicationContext = context.getApplicationContext();
        this.settings = new Settings(applicationContext);

        this.type = getBackupType();
        this.uri = uri;
    }

    @Override
    @NonNull
    protected BackupTaskResult doInBackground() {
        String fileName;

        if (uri == null) {
            BackupHelper.BackupFile backupFile = BackupHelper.backupFile(applicationContext, settings.getBackupLocation(), type);

            if (backupFile.file == null)
                return new BackupTaskResult(false, backupFile.errorMessage, null);

            uri = backupFile.file.getUri();
            fileName = backupFile.file.getName();
        } else {
            fileName = StorageAccessHelper.getContentFileName(applicationContext, uri);
        }

        boolean success = doBackup();

        if (success)
            return BackupTaskResult.success(fileName);
        else
            return BackupTaskResult.failure();
    }

    @NonNull
    protected abstract Constants.BackupType getBackupType();
    protected abstract boolean doBackup();


    public static class BackupTaskResult {
        public final boolean success;
        public final int messageId;
        public final String fileName;

        public BackupTaskResult(boolean success, int messageId, String fileName) {
            this.success = success;
            this.messageId = messageId;
            this.fileName = fileName;
        }

        public static BackupTaskResult success(String fileName) {
            return new BackupTaskResult(true, R.string.backup_toast_export_success, fileName);
        }

        public static BackupTaskResult failure() {
            return new BackupTaskResult(false, R.string.backup_toast_export_failed, null);
        }
    }
}