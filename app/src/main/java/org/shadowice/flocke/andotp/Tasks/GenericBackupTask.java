package org.shadowice.flocke.andotp.Tasks;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.BackupHelper;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.Settings;

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
        if (uri == null) {
            BackupHelper.BackupFile backupFile = BackupHelper.backupFile(applicationContext, settings.getBackupLocation(), type);

            if (backupFile.file == null)
                return new BackupTaskResult(backupFile.errorMessage);

            uri = backupFile.file.getUri();
        }

        boolean success = doBackup();

        if (success)
            return BackupTaskResult.success();
        else
            return BackupTaskResult.failure();
    }

    @NonNull
    protected abstract Constants.BackupType getBackupType();
    protected abstract boolean doBackup();


    public static class BackupTaskResult {
        public final int messageId;

        public BackupTaskResult(int messageId) {
            this.messageId = messageId;
        }

        public static BackupTaskResult success() {
            return new BackupTaskResult(R.string.backup_toast_export_success);
        }

        public static BackupTaskResult failure() {
            return new BackupTaskResult(R.string.backup_toast_export_failed);
        }
    }
}