package org.shadowice.flocke.andotp.Tasks;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.shadowice.flocke.andotp.Utilities.StorageAccessHelper;

public class PlainTextRestoreTask extends GenericRestoreTask {
    public PlainTextRestoreTask(Context context, Uri uri) {
        super(context, uri);
    }

    @Override
    @NonNull
    protected BackupTaskResult doInBackground() {
        String data = StorageAccessHelper.loadFileString(applicationContext, uri);
        return BackupTaskResult.success(BackupTaskResult.ResultType.RESTORE, data);
    }
}
