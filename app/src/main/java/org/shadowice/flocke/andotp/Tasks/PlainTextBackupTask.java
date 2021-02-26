package org.shadowice.flocke.andotp.Tasks;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.DatabaseHelper;
import org.shadowice.flocke.andotp.Utilities.StorageAccessHelper;

import java.util.ArrayList;

public class PlainTextBackupTask extends GenericBackupTask {
    private final ArrayList<Entry> entries;

    public PlainTextBackupTask(Context context, ArrayList<Entry> entries, @Nullable Uri uri) {
        super(context, uri);
        this.entries = entries;
    }

    @Override
    @NonNull
    protected Constants.BackupType getBackupType() {
        return Constants.BackupType.PLAIN_TEXT;
    }

    @Override
    protected boolean doBackup() {
        String payload = DatabaseHelper.entriesToString(entries);
        return StorageAccessHelper.saveFile(applicationContext, uri, payload);
    }
}