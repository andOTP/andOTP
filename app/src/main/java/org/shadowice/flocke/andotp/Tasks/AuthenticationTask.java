package org.shadowice.flocke.andotp.Tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.shadowice.flocke.andotp.Tasks.AuthenticationTask.Result;
import org.shadowice.flocke.andotp.Utilities.Constants.AuthMethod;
import org.shadowice.flocke.andotp.Utilities.EncryptionHelper;
import org.shadowice.flocke.andotp.Utilities.EncryptionHelper.PBKDF2Credentials;
import org.shadowice.flocke.andotp.Utilities.Settings;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class AuthenticationTask extends UiBasedBackgroundTask<Result> {

    private final Settings settings;

    private final boolean isAuthUpgrade;
    private final String existingAuthCredentials;
    private final String plainPassword;

    /**
     * @param context Context to be used to query settings (the application Context will be used to avoid memory leaks).
     * @param isAuthUpgrade true if this is an authentication upgrade and new credentials should be saved, false if this is just confirmation.
     * @param existingAuthCredentials The existing hashed authentication credentials that we have stored.
     * @param plainPassword The plaintext user-entered password to check authentication with. */
    public AuthenticationTask(Context context, boolean isAuthUpgrade, String existingAuthCredentials, String plainPassword) {
        super(Result.failure());
        Context applicationContext = context.getApplicationContext();
        this.settings = new Settings(applicationContext);

        this.isAuthUpgrade = isAuthUpgrade;
        this.existingAuthCredentials = existingAuthCredentials;
        this.plainPassword = plainPassword;
    }

    @Override
    @NonNull
    protected Result doInBackground() {
        if (isAuthUpgrade) {
            return upgradeAuthentication();
        } else {
            return confirmAuthentication();
        }
    }

    @NonNull
    private Result upgradeAuthentication() {
        String hashedPassword = new String(Hex.encodeHex(DigestUtils.sha256(plainPassword)));
        if (!hashedPassword.equals(existingAuthCredentials))
            return Result.failure();

        byte[] key = settings.setAuthCredentials(plainPassword);

        AuthMethod authMethod = settings.getAuthMethod();
        if (authMethod == AuthMethod.PASSWORD)
            settings.removeAuthPasswordHash();
        else if (authMethod == AuthMethod.PIN)
            settings.removeAuthPINHash();

        if (key == null)
            return Result.upgradeFailure();
        else
            return Result.success(key);
    }

    @NonNull
    private Result confirmAuthentication() {
        try {
            PBKDF2Credentials credentials = EncryptionHelper.generatePBKDF2Credentials(plainPassword, settings.getSalt(), settings.getIterations());
            byte[] passwordArray = Base64.decode(existingAuthCredentials, Base64.URL_SAFE);

            if (Arrays.equals(passwordArray, credentials.password)) {
                return Result.success(credentials.key);
            }
            return Result.failure();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IllegalArgumentException e) {
            Log.e("AuthenticationTask", "Problem decoding password", e);
            return Result.failure();
        }
    }

    public static class Result {
        @Nullable
        public final byte[] encryptionKey;
        public final boolean authUpgradeFailed;

        public Result(@Nullable byte[] encryptionKey, boolean authUpgradeFailed) {
            this.encryptionKey = encryptionKey;
            this.authUpgradeFailed = authUpgradeFailed;
        }

        public static Result success(byte[] encryptionKey) {
            return new Result(encryptionKey, false);
        }

        public static Result upgradeFailure() {
            return new Result(null, true);
        }

        public static Result failure() {
            return new Result(null, false);
        }
    }
}
