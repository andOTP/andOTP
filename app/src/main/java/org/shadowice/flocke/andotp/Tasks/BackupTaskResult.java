package org.shadowice.flocke.andotp.Tasks;

import android.content.Intent;
import android.net.Uri;

import org.shadowice.flocke.andotp.R;

public class BackupTaskResult {
    public final boolean success;
    public final String payload;
    public final int messageId;

    public boolean isPGP = false;
    public Intent decryptIntent = null;
    public Uri uri = null;

    public final ResultType resultType;

    public enum ResultType {
        NONE, BACKUP, RESTORE
    }

    public BackupTaskResult(ResultType type, boolean success, String payload, int messageId) {
        this.resultType = type;
        this.success = success;
        this.payload = payload;
        this.messageId = messageId;
    }

    public BackupTaskResult(ResultType type, boolean success, String payload, int messageId, boolean isPGP, Intent decryptIntent, Uri uri) {
        this.resultType = type;
        this.success = success;
        this.payload = payload;
        this.messageId = messageId;
        this.isPGP = isPGP;
        this.decryptIntent = decryptIntent;
        this.uri = uri;
    }

    public static BackupTaskResult success(ResultType type, String payload) {
        return new BackupTaskResult(type, true, payload, R.string.backup_toast_export_success);
    }

    public static BackupTaskResult failure(ResultType type, int messageId) {
        return new BackupTaskResult(type, false, null, messageId);
    }
}