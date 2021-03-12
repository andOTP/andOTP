package org.shadowice.flocke.andotp.Tasks;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.Utilities.BackupHelper;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.DatabaseHelper;

import java.util.ArrayList;

public class EncryptedBackupTask extends GenericBackupTask {
    private final String password;
    private final ArrayList<Entry> entries;

    public EncryptedBackupTask(Context context, ArrayList<Entry> entries, String password, @Nullable Uri uri) {
        super(context, uri);
        this.entries = entries;
        this.password = password;
    }

    @Override
    @NonNull
    protected Constants.BackupType getBackupType() {
        return Constants.BackupType.ENCRYPTED;
    }

    @Override
    protected boolean doBackup() {
        String payload = DatabaseHelper.entriesToString(entries);
        return BackupHelper.backupToFile(applicationContext, uri, password, payload);
    }
}
