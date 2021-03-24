package org.shadowice.flocke.andotp.Tasks;

import android.content.Context;

import androidx.annotation.NonNull;

import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.EncryptionHelper;

import javax.crypto.SecretKey;

public class ChangeEncryptionTask extends UiBasedBackgroundTask<ChangeEncryptionTask.Result>
    implements EncryptionHelper.EncryptionChangeCallback {

    private final Context context;
    private final SecretKey oldEncryptionKey;
    private final Constants.EncryptionType newEncryptionType;
    private final byte[] newKeyMaterial;

    private SecretKey newEncryptionKey = null;

    public ChangeEncryptionTask(Context context, SecretKey oldEncryptionKey, Constants.EncryptionType newEncryptionType, byte[] newKey) {
        super(new Result(EncryptionHelper.EncryptionChangeResult.TASK_CREATION_FAILED, null, null));

        this.context = context;
        this.oldEncryptionKey = oldEncryptionKey;
        this.newEncryptionType = newEncryptionType;
        this. newKeyMaterial = newKey;
    }

    @Override
    public void onSuccessfulEncryptionChange(Constants.EncryptionType newEncryptionType, SecretKey newEncryptionKey) {
        this.newEncryptionKey = newEncryptionKey;
    }

    @NonNull
    @Override
    protected Result doInBackground() {
        EncryptionHelper.EncryptionChangeResult result = EncryptionHelper.tryEncryptionChange(context, oldEncryptionKey, newEncryptionType, newKeyMaterial, this);

        return new Result(result, this.newEncryptionKey, this.newEncryptionType);
    }

    public static class Result {
        public final EncryptionHelper.EncryptionChangeResult result;
        public final SecretKey newEncryptionKey;
        public final Constants.EncryptionType encryptionType;

        public Result(EncryptionHelper.EncryptionChangeResult result, SecretKey newKey, Constants.EncryptionType encryptionType) {
            this.result = result;
            this.newEncryptionKey = newKey;
            this.encryptionType = encryptionType;
        }
    }
}
