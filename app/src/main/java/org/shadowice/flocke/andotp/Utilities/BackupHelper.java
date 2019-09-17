package org.shadowice.flocke.andotp.Utilities;

import android.content.Context;

public class BackupHelper {
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
}
