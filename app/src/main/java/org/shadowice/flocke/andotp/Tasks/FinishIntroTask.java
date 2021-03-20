package org.shadowice.flocke.andotp.Tasks;

import android.content.Context;

import androidx.annotation.NonNull;

import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.Settings;

public class FinishIntroTask extends UiBasedBackgroundTask<FinishIntroTask.Result> {
    private final Settings settings;

    private final Constants.EncryptionType encryptionType;
    private final Constants.AuthMethod authMethod;
    private final String password;
    private final boolean androidSyncEnabled;

    public FinishIntroTask(Context context, Constants.EncryptionType encryptionType, Constants.AuthMethod authMethod, String password, boolean androidSyncEnabled) {
        super(new Result(false, null));

        Context applicationContext = context.getApplicationContext();
        this.settings = new Settings(applicationContext);

        this.encryptionType = encryptionType;
        this.authMethod = authMethod;
        this.password = password;
        this.androidSyncEnabled = androidSyncEnabled;
    }

    @NonNull
    @Override
    protected Result doInBackground() {
        settings.setEncryption(encryptionType);
        settings.setAuthMethod(authMethod);
        settings.setAndroidBackupServiceEnabled(androidSyncEnabled);

        byte[] encryptionKey = null;

        if (authMethod == Constants.AuthMethod.PASSWORD || authMethod == Constants.AuthMethod.PIN)
            encryptionKey = settings.setAuthCredentials(password);

        settings.setFirstTimeWarningShown(true);

        return new Result(true, encryptionKey);
    }

    public static class Result {
        public final boolean saveSuccessful;
        public final byte[] encryptionKey;

        public Result(boolean saveSuccessful, byte[] encryptionKey) {
            this.saveSuccessful = saveSuccessful;
            this.encryptionKey = encryptionKey;
        }
    }
}