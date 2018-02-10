package org.shadowice.flocke.andotp.Utilities;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

public class BackupAgent extends BackupAgentHelper {
    static final String PREFS_BACKUP_KEY = "prefs";
    static final String FILES_BACKUP_KEY = "files";

    // PreferenceManager.getDefaultSharedPreferencesName is only available in API > 24, this is its implementation
    String getDefaultSharedPreferencesName() {
        return getPackageName() + "_preferences";
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        Settings settings = new Settings(this);

        if(settings.getAndroidBackupServiceEnabled()) {
            synchronized (DatabaseHelper.DatabaseFileLock) {
                super.onBackup(oldState, data, newState);
            }
        }
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        synchronized (DatabaseHelper.DatabaseFileLock) {
            super.onRestore(data, appVersionCode, newState);
        }
    }

    @Override
    public void onCreate() {
        String prefs = getDefaultSharedPreferencesName();

        SharedPreferencesBackupHelper sharedPreferencesBackupHelper = new SharedPreferencesBackupHelper(this, prefs);
        addHelper(PREFS_BACKUP_KEY, sharedPreferencesBackupHelper);

        FileBackupHelper fileBackupHelper = new FileBackupHelper(this, Constants.FILENAME_DATABASE, Constants.FILENAME_DATABASE_BACKUP);
        addHelper(FILES_BACKUP_KEY, fileBackupHelper);
    }
}
