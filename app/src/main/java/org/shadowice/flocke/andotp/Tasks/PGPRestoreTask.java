package org.shadowice.flocke.andotp.Tasks;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.shadowice.flocke.andotp.Utilities.StorageAccessHelper;

public class PGPRestoreTask extends GenericRestoreTask {
    private final Intent decryptIntent;

    public PGPRestoreTask(Context context, Uri uri, Intent decryptIntent) {
        super(context, uri);
        this.decryptIntent = decryptIntent;
    }

    @Override
    @NonNull
    protected RestoreTaskResult doInBackground() {
        String data = StorageAccessHelper.loadFileString(applicationContext, uri);

        return new RestoreTaskResult(true, data, 0, true, decryptIntent, uri);
    }
}
