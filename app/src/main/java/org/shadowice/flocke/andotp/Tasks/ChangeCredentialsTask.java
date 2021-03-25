package org.shadowice.flocke.andotp.Tasks;

import android.content.Context;

import androidx.annotation.NonNull;

import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.EncryptionHelper;
import org.shadowice.flocke.andotp.Utilities.Settings;

import javax.crypto.SecretKey;

public class ChangeCredentialsTask extends UiBasedBackgroundTask<ChangeCredentialsTask.Result>
    implements EncryptionHelper.EncryptionChangeCallback {

    private final Context context;
    private final Settings settings;
    private final Constants.EncryptionType encryptionType;
    private final Constants.AuthMethod newAuthMethod;
    private final SecretKey oldEncryptionKey;
    private final String password;

    private SecretKey newEncryptionKey = null;

    public ChangeCredentialsTask(Context context, Constants.EncryptionType encryptionType, SecretKey oldKey, Constants.AuthMethod newAuth, String password) {
        super(new Result(false, null, null));

        this.context = context.getApplicationContext();
        this.settings = new Settings(this.context);
        this.encryptionType = encryptionType;
        this.newAuthMethod = newAuth;
        this.oldEncryptionKey = oldKey;
        this.password = password;
    }

    @Override
    public void onSuccessfulEncryptionChange(Constants.EncryptionType newEncryptionType, SecretKey newEncryptionKey) {
        this.newEncryptionKey = newEncryptionKey;
    }

    @NonNull
    @Override
    protected Result doInBackground() {
        byte[] newKey = null;

        if (newAuthMethod == Constants.AuthMethod.PASSWORD || newAuthMethod == Constants.AuthMethod.PIN) {
            if (!password.isEmpty()) {
                newKey = settings.setAuthCredentials(password);
            } else {
                return new Result(false, null, null);
            }
        }

        if (settings.getEncryption() == Constants.EncryptionType.PASSWORD) {
            if (newKey == null)
                return new Result(false, null, null);

            if (EncryptionHelper.tryEncryptionChange(context, oldEncryptionKey, encryptionType, newKey, this) != EncryptionHelper.EncryptionChangeResult.SUCCESS)
                return new Result(false, null, null);
        }

        // We already persist the new value here so if something happens to the calling preference
        // everything is already finalized
        settings.setAuthMethod(newAuthMethod);

        return new Result(true, newEncryptionKey, encryptionType);
    }

    public static class Result {
        public final boolean success;
        public final SecretKey newEncryptionKey;
        public final Constants.EncryptionType encryptionType;

        public Result(boolean success, SecretKey newKey, Constants.EncryptionType encryptionType) {
            this.success = success;
            this.newEncryptionKey = newKey;
            this.encryptionType = encryptionType;
        }
    }
}
