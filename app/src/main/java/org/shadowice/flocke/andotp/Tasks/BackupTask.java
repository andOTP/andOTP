package org.shadowice.flocke.andotp.Tasks;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.BackupHelper;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.Settings;
import org.shadowice.flocke.andotp.Utilities.StorageAccessHelper;

public class BackupTask implements Runnable {
    final private Context context;
    final private String payload;
    final private Constants.BackupType type;
    final private BackupCallback callback;

    final private Handler handler;

    private boolean silent = false;
    private Uri uri = null;
    private String password = null;

    public BackupTask(Context context, Constants.BackupType type, String payload, BackupCallback callback) {
        this.context = context;
        this.payload = payload;
        this.type = type;
        this.callback = callback;

        this.handler = new Handler(Looper.getMainLooper());
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    @Override
    public void run() {
        boolean success;

        if (uri == null)
            uri = getTargetUri();

        if (uri == null)
            return;

        if (type == Constants.BackupType.PLAIN_TEXT) {
            success = StorageAccessHelper.saveFile(context, uri, payload);
        } else if (type == Constants.BackupType.ENCRYPTED) {
            success = BackupHelper.backupToFile(context, uri, password, payload);
        } else if (type == Constants.BackupType.OPEN_PGP) {
            success = StorageAccessHelper.saveFile(context, uri, payload);
        } else {
            success = false;
        }

        if (success)
            onFinished();
        else
            onFailed();
    }

    private Uri getTargetUri() {
        Settings settings = new Settings(context);

        BackupHelper.BackupFile backupFile = BackupHelper.backupFile(context, settings.getBackupLocation(), type);

        if (backupFile.file != null) {
            return backupFile.file.getUri();
        } else {
            showToast(context, backupFile.errorMessage);
            return null;
        }
    }

    private void showToast(Context context, int msgId) {
        if (!this.silent) {
            this.handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, msgId, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void onFinished() {
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, R.string.backup_toast_export_success, Toast.LENGTH_LONG).show();
                if (callback != null)
                    callback.onBackupFinished();
            }
        });
    }

    private void onFailed() {
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, R.string.backup_toast_export_failed, Toast.LENGTH_LONG).show();
                if (callback != null)
                    callback.onBackupFailed();
            }
        });
    }

    public interface BackupCallback {
        void onBackupFinished();
        void onBackupFailed();
    }
}
