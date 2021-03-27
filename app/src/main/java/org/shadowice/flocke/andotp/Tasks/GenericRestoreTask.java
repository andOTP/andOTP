package org.shadowice.flocke.andotp.Tasks;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.Settings;

public abstract class GenericRestoreTask extends UiBasedBackgroundTask<BackupTaskResult> {
    protected final Context applicationContext;
    protected final Settings settings;
    protected Uri uri;

    public GenericRestoreTask(Context context, @Nullable Uri uri) {
        super(BackupTaskResult.failure(BackupTaskResult.ResultType.RESTORE, R.string.backup_toast_import_failed));

        this.applicationContext = context.getApplicationContext();
        this.settings = new Settings(applicationContext);

        this.uri = uri;
    }

    @Override
    @NonNull
    protected abstract BackupTaskResult doInBackground();
}
