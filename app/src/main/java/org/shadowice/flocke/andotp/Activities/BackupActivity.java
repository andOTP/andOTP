/*
 * Copyright (C) 2017-2020 Jakob Nixdorf
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.shadowice.flocke.andotp.Activities;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.OpenPgpSignatureResult;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;
import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.Dialogs.PasswordEntryDialog;
import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Tasks.BackupTask;
import org.shadowice.flocke.andotp.Utilities.BackupHelper;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.DatabaseHelper;
import org.shadowice.flocke.andotp.Utilities.EncryptionHelper;
import org.shadowice.flocke.andotp.Utilities.StorageAccessHelper;
import org.shadowice.flocke.andotp.Utilities.Tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.SecretKey;

public class BackupActivity extends BaseActivity
    implements BackupTask.BackupCallback {
    private final static String TAG = BackupActivity.class.getSimpleName();

    private Constants.BackupType backupType = Constants.BackupType.ENCRYPTED;
    private SecretKey encryptionKey = null;

    private OpenPgpServiceConnection pgpServiceConnection;
    private String pgpEncryptionUserIDs;

    private Uri encryptTargetFile;
    private Uri decryptSourceFile;

    private Button btnBackup;
    private Button btnRestore;
    private TextView txtBackupLabel;
    private TextView txtBackupWarning;
    private SwitchMaterial swReplace;
    private CheckBox chkOldFormat;
    private ProgressBar progressBackup;
    private ProgressBar progressRestore;

    private boolean reload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.backup_activity_title);
        setContentView(R.layout.activity_container);

        Toolbar toolbar = findViewById(R.id.container_toolbar);
        setSupportActionBar(toolbar);

        ViewStub stub = findViewById(R.id.container_stub);
        stub.setLayoutResource(R.layout.content_backup);
        View v = stub.inflate();

        Intent callingIntent = getIntent();
        byte[] keyMaterial = callingIntent.getByteArrayExtra(Constants.EXTRA_BACKUP_ENCRYPTION_KEY);
        encryptionKey = EncryptionHelper.generateSymmetricKey(keyMaterial);

        Spinner spBackupType = v.findViewById(R.id.backupType);
        btnBackup = v.findViewById(R.id.buttonBackup);
        btnRestore = v.findViewById(R.id.buttonRestore);
        txtBackupLabel = v.findViewById(R.id.backupLabel);
        txtBackupWarning = v.findViewById(R.id.backupErrorLabel);
        swReplace = v.findViewById(R.id.backup_replace);
        chkOldFormat = v.findViewById(R.id.restoreOldCrypt);
        progressBackup = v.findViewById(R.id.progressBarBackup);
        progressRestore = v.findViewById(R.id.progressBarRestore);

        setupBackupType(settings.getDefaultBackupType());
        spBackupType.setSelection(backupType.ordinal());

        spBackupType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Constants.BackupType type = Constants.BackupType.values()[i];
                setupBackupType(type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        btnBackup.setOnClickListener(view -> {
            switch (backupType) {
                case PLAIN_TEXT:
                    backupPlainWithWarning();
                    break;
                case ENCRYPTED:
                    showSaveFileSelector(Constants.BACKUP_MIMETYPE_CRYPT, Constants.BackupType.ENCRYPTED, Constants.INTENT_BACKUP_SAVE_DOCUMENT_CRYPT);
                    break;
                case OPEN_PGP:
                    showSaveFileSelector(Constants.BACKUP_MIMETYPE_PGP, Constants.BackupType.OPEN_PGP, Constants.INTENT_BACKUP_SAVE_DOCUMENT_PGP);
                    break;
            }
        });

        btnRestore.setOnClickListener(view -> {
            switch (backupType) {
                case PLAIN_TEXT:
                    showOpenFileSelector(Constants.INTENT_BACKUP_OPEN_DOCUMENT_PLAIN);
                    break;
                case ENCRYPTED:
                    if (chkOldFormat.isChecked())
                        showOpenFileSelector(Constants.INTENT_BACKUP_OPEN_DOCUMENT_CRYPT_OLD);
                    else
                        showOpenFileSelector(Constants.INTENT_BACKUP_OPEN_DOCUMENT_CRYPT);
                    break;
                case OPEN_PGP:
                    showOpenFileSelector(Constants.INTENT_BACKUP_OPEN_DOCUMENT_PGP);
                    break;
            }
        });
    }

    private void setupBackupType(Constants.BackupType type) {
        switch (type) {
            case PLAIN_TEXT:
                txtBackupLabel.setText(R.string.backup_label_warning_plain);

                chkOldFormat.setVisibility(View.GONE);
                txtBackupWarning.setVisibility(View.GONE);

                btnBackup.setEnabled(true);
                btnRestore.setEnabled(true);

                break;
            case ENCRYPTED:
                txtBackupLabel.setText(R.string.backup_label_crypt);

                chkOldFormat.setVisibility(View.VISIBLE);
                txtBackupWarning.setVisibility(View.GONE);

                btnBackup.setEnabled(true);
                btnRestore.setEnabled(true);

                break;
            case OPEN_PGP:
                txtBackupLabel.setText(R.string.backup_label_pgp);

                chkOldFormat.setVisibility(View.GONE);

                String PGPProvider = settings.getOpenPGPProvider();
                pgpEncryptionUserIDs = settings.getOpenPGPEncryptionUserIDs();

                if (TextUtils.isEmpty(PGPProvider)) {
                    txtBackupWarning.setText(R.string.backup_desc_openpgp_provider);
                    txtBackupWarning.setVisibility(View.VISIBLE);

                    btnBackup.setEnabled(false);
                    btnRestore.setEnabled(false);
                } else if (TextUtils.isEmpty(pgpEncryptionUserIDs)){
                    txtBackupWarning.setText(R.string.backup_desc_openpgp_keyid);
                    txtBackupWarning.setVisibility(View.VISIBLE);

                    btnBackup.setEnabled(false);
                    btnRestore.setEnabled(false);
                } else {
                    txtBackupWarning.setVisibility(View.GONE);

                    btnBackup.setEnabled(true);
                    btnRestore.setEnabled(true);

                    pgpServiceConnection = new OpenPgpServiceConnection(BackupActivity.this.getApplicationContext(), PGPProvider);
                    pgpServiceConnection.bindToService();
                }

                break;
        }

        backupType = type;
        settings.setDefaultBackupType(type);
    }

    // End with a result
    public void finishWithResult() {
        Intent data = new Intent();
        data.putExtra("reload", reload);
        setResult(RESULT_OK, data);
        finish();
    }

    // Go back to the main activity
    @Override
    public boolean onSupportNavigateUp() {
        finishWithResult();
        return true;
    }

    @Override
    public void onBackPressed() {
        finishWithResult();
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (pgpServiceConnection != null)
            pgpServiceConnection.unbindFromService();
    }

    @Override
    public void onBackupFinished() {
        hideProgress();
    }

    @Override
    public void onBackupFailed() {
        hideProgress();
    }

    private void showProgress(boolean restore) {
        btnBackup.setEnabled(false);
        btnRestore.setEnabled(false);
        chkOldFormat.setEnabled(false);
        swReplace.setEnabled(false);

        if (restore)
            progressRestore.setVisibility(View.VISIBLE);
        else
            progressBackup.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        btnBackup.setEnabled(true);
        btnRestore.setEnabled(true);
        chkOldFormat.setEnabled(true);
        swReplace.setEnabled(true);

        progressRestore.setVisibility(View.GONE);
        progressBackup.setVisibility(View.GONE);
    }

    // Get the result from external activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == Constants.INTENT_BACKUP_OPEN_DOCUMENT_PLAIN && resultCode == RESULT_OK) {
            if (intent != null) {
                doRestorePlain(intent.getData());
            }
        } else if (requestCode == Constants.INTENT_BACKUP_SAVE_DOCUMENT_PLAIN && resultCode == RESULT_OK) {
            if (intent != null) {
                doBackupPlain(intent.getData());
            }
        } else if (requestCode == Constants.INTENT_BACKUP_OPEN_DOCUMENT_CRYPT && resultCode == RESULT_OK) {
            if (intent != null) {
                doRestoreCrypt(intent.getData(), false);
            }
        } else if (requestCode == Constants.INTENT_BACKUP_OPEN_DOCUMENT_CRYPT_OLD && resultCode == RESULT_OK) {
            if (intent != null) {
                doRestoreCrypt(intent.getData(), true);
            }
        } else if (requestCode == Constants.INTENT_BACKUP_SAVE_DOCUMENT_CRYPT && resultCode == RESULT_OK) {
            if (intent != null) {
                doBackupCrypt(intent.getData());
            }
        } else if (requestCode == Constants.INTENT_BACKUP_OPEN_DOCUMENT_PGP && resultCode == RESULT_OK) {
            if (intent != null)
                restoreEncryptedWithPGP(intent.getData(), null);
        } else if (requestCode == Constants.INTENT_BACKUP_SAVE_DOCUMENT_PGP && resultCode == RESULT_OK) {
            if (intent != null)
                backupEncryptedWithPGP(intent.getData(), null);
        } else if (requestCode == Constants.INTENT_BACKUP_ENCRYPT_PGP && resultCode == RESULT_OK) {
            backupEncryptedWithPGP(encryptTargetFile, intent);
        } else if (requestCode == Constants.INTENT_BACKUP_DECRYPT_PGP && resultCode == RESULT_OK) {
            restoreEncryptedWithPGP(decryptSourceFile, intent);
        }
    }

    /* Generic functions for all backup/restore options */

    private void showOpenFileSelector(int intentId) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        try {
            startActivityForResult(intent, intentId);
            return;
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "Failed to use ACTION_OPEN_DOCUMENT, no matching activity found!");
        }

        intent.setAction(Intent.ACTION_GET_CONTENT);

        try {
            startActivityForResult(intent, intentId);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "Failed to use ACTION_GET_CONTENT, no matching activity found!");
            Toast.makeText(this, R.string.backup_toast_file_selection_failed, Toast.LENGTH_LONG).show();
        }
    }

    private void showSaveFileSelector(String mimeType, Constants.BackupType backupType, int intentId) {
        if (settings.getBackupAsk()) {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_TITLE, BackupHelper.backupFilename(this, backupType));
            startActivityForResult(intent, intentId);
        } else {
            if (settings.isBackupLocationSet()) {
                if (intentId == Constants.INTENT_BACKUP_SAVE_DOCUMENT_PLAIN) {
                    doBackupPlain(null);
                } else if (intentId == Constants.INTENT_BACKUP_SAVE_DOCUMENT_CRYPT) {
                    doBackupCrypt(null);
                } else if (intentId == Constants.INTENT_BACKUP_SAVE_DOCUMENT_PGP) {
                    backupEncryptedWithPGP(null, null);
                }
            } else {
                Toast.makeText(this, R.string.backup_toast_no_location, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void restoreEntries(String text) {
        ArrayList<Entry> entries = DatabaseHelper.stringToEntries(text);

        if (entries.size() > 0) {
            if (! swReplace.isChecked()) {
                ArrayList<Entry> currentEntries = DatabaseHelper.loadDatabase(this, encryptionKey);

                entries.removeAll(currentEntries);
                entries.addAll(currentEntries);
            }

            if (DatabaseHelper.saveDatabase(this, entries, encryptionKey)) {
                reload = true;
                Toast.makeText(this, R.string.backup_toast_import_success, Toast.LENGTH_LONG).show();
                finishWithResult();
            } else {
                Toast.makeText(this, R.string.backup_toast_import_save_failed, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.backup_toast_import_no_entries, Toast.LENGTH_LONG).show();
        }
    }

    /* Plain-text backup functions */

    private void doRestorePlain(Uri uri) {
        if (Tools.isExternalStorageReadable()) {
            String content = StorageAccessHelper.loadFileString(this, uri);

            restoreEntries(content);
        } else {
            Toast.makeText(this, R.string.backup_toast_storage_not_accessible, Toast.LENGTH_LONG).show();
        }
    }

    private void doBackupPlain(Uri uri) {
        if (Tools.isExternalStorageWritable()) {
            ArrayList<Entry> entries = DatabaseHelper.loadDatabase(this, encryptionKey);

            ExecutorService executor = Executors.newSingleThreadExecutor();

            BackupTask backupTask = new BackupTask(this, Constants.BackupType.PLAIN_TEXT, DatabaseHelper.entriesToString(entries), this);

            if (uri != null)
                backupTask.setUri(uri);

            showProgress(false);
            executor.execute(backupTask);
        } else {
            Toast.makeText(this, R.string.backup_toast_storage_not_accessible, Toast.LENGTH_LONG).show();
        }
    }

    private void backupPlainWithWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.backup_dialog_title_security_warning)
                .setMessage(R.string.backup_dialog_msg_export_warning)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showSaveFileSelector(Constants.BACKUP_MIMETYPE_PLAIN, Constants.BackupType.PLAIN_TEXT, Constants.INTENT_BACKUP_SAVE_DOCUMENT_PLAIN);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create()
                .show();
    }

    /* Encrypted backup functions */

    private void doRestoreCrypt(final Uri uri, final boolean old_format) {
        String password = settings.getBackupPasswordEnc();

        if (password.isEmpty()) {
            PasswordEntryDialog pwDialog = new PasswordEntryDialog(this, PasswordEntryDialog.Mode.ENTER, settings.getBlockAccessibility(), settings.getBlockAutofill(), new PasswordEntryDialog.PasswordEnteredCallback() {
                @Override
                public void onPasswordEntered(String newPassword) {
                    doRestoreCryptWithPassword(uri, newPassword, old_format);
                }
            });
            pwDialog.show();
        } else {
            doRestoreCryptWithPassword(uri, password, old_format);
        }
    }

    private void doRestoreCryptWithPassword(Uri uri, String password, boolean old_format) {
        if (Tools.isExternalStorageReadable()) {
            boolean success = true;
            String decryptedString = "";

            try {
                byte[] data = StorageAccessHelper.loadFile(this, uri);

                if (old_format) {
                    SecretKey key = EncryptionHelper.generateSymmetricKeyFromPassword(password);
                    byte[] decrypted = EncryptionHelper.decrypt(key, data);

                    decryptedString = new String(decrypted, StandardCharsets.UTF_8);
                } else {
                    byte[] iterBytes = Arrays.copyOfRange(data, 0, Constants.INT_LENGTH);
                    byte[] salt = Arrays.copyOfRange(data, Constants.INT_LENGTH, Constants.INT_LENGTH + Constants.ENCRYPTION_IV_LENGTH);
                    byte[] encrypted = Arrays.copyOfRange(data, Constants.INT_LENGTH + Constants.ENCRYPTION_IV_LENGTH, data.length);

                    int iter = ByteBuffer.wrap(iterBytes).getInt();

                    SecretKey key = EncryptionHelper.generateSymmetricKeyPBKDF2(password, iter, salt);

                    byte[] decrypted = EncryptionHelper.decrypt(key, encrypted);
                    decryptedString = new String(decrypted, StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                success = false;
                e.printStackTrace();
            }

            if (success) {
                restoreEntries(decryptedString);
            } else {
                Toast.makeText(this,R.string.backup_toast_import_decryption_failed, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.backup_toast_storage_not_accessible, Toast.LENGTH_LONG).show();
        }
    }

    private void doBackupCrypt(final Uri uri) {
        String password = settings.getBackupPasswordEnc();

        if (password.isEmpty()) {
            PasswordEntryDialog pwDialog = new PasswordEntryDialog(this, PasswordEntryDialog.Mode.UPDATE, settings.getBlockAccessibility(), settings.getBlockAutofill(), new PasswordEntryDialog.PasswordEnteredCallback() {
                @Override
                public void onPasswordEntered(String newPassword) {
                    doBackupCryptWithPassword(uri, newPassword);
                }
            });
            pwDialog.show();
        } else {
            doBackupCryptWithPassword(uri, password);
        }
    }

    private void doBackupCryptWithPassword(Uri uri, String password) {
        if (Tools.isExternalStorageWritable()) {
            showProgress(false);
            BackupHelper.backupToFileAsync(this, uri, password, encryptionKey, this, false);
        } else {
            Toast.makeText(this, R.string.backup_toast_storage_not_accessible, Toast.LENGTH_LONG).show();
        }
    }

    /* OpenPGP backup functions */

    private void restoreEncryptedWithPGP(Uri uri, Intent decryptIntent) {
        if (decryptIntent == null)
            decryptIntent = new Intent(OpenPgpApi.ACTION_DECRYPT_VERIFY);

        String input = StorageAccessHelper.loadFileString(this, uri);

        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        OpenPgpApi api = new OpenPgpApi(this, pgpServiceConnection.getService());
        Intent result = api.executeApi(decryptIntent, is, os);
        handleOpenPGPResult(result, os, uri, Constants.INTENT_BACKUP_DECRYPT_PGP);
    }

    private void doBackupEncrypted(Uri uri, String data) {
        if (Tools.isExternalStorageWritable()) {
            ExecutorService executor = Executors.newSingleThreadExecutor();

            BackupTask backupTask = new BackupTask(this, Constants.BackupType.OPEN_PGP, data, this);

            if (uri != null)
                backupTask.setUri(uri);

            showProgress(false);
            executor.execute(backupTask);
        } else {
            Toast.makeText(this, R.string.backup_toast_storage_not_accessible, Toast.LENGTH_LONG).show();
        }
    }

    private void backupEncryptedWithPGP(Uri uri, Intent encryptIntent) {
        ArrayList<Entry> entries = DatabaseHelper.loadDatabase(this, encryptionKey);
        String plainJSON = DatabaseHelper.entriesToString(entries);

        if (encryptIntent == null) {
            encryptIntent = new Intent();

            if (settings.getOpenPGPSigningKey() != 0) {
                encryptIntent.setAction(OpenPgpApi.ACTION_SIGN_AND_ENCRYPT);
                encryptIntent.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, settings.getOpenPGPSigningKey());
            } else {
                encryptIntent.setAction(OpenPgpApi.ACTION_ENCRYPT);
            }

            encryptIntent.putExtra(OpenPgpApi.EXTRA_USER_IDS, pgpEncryptionUserIDs.split(","));
            encryptIntent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
        }

        InputStream is = new ByteArrayInputStream(plainJSON.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        OpenPgpApi api = new OpenPgpApi(this, pgpServiceConnection.getService());
        Intent result = api.executeApi(encryptIntent, is, os);
        handleOpenPGPResult(result, os, uri, Constants.INTENT_BACKUP_ENCRYPT_PGP);
    }

    public String outputStreamToString(ByteArrayOutputStream os) {
        return new String(os.toByteArray(), StandardCharsets.UTF_8);
    }

    public void handleOpenPGPResult(Intent result, ByteArrayOutputStream os, Uri file, int requestCode) {
        if (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR) == OpenPgpApi.RESULT_CODE_SUCCESS) {
            if (requestCode == Constants.INTENT_BACKUP_ENCRYPT_PGP) {
                if (os != null)
                    doBackupEncrypted(file, outputStreamToString(os));
            } else if (requestCode == Constants.INTENT_BACKUP_DECRYPT_PGP) {
                if (os != null) {
                    if (settings.getOpenPGPVerify()) {
                        OpenPgpSignatureResult sigResult = result.getParcelableExtra(OpenPgpApi.RESULT_SIGNATURE);

                        if (sigResult.getResult() == OpenPgpSignatureResult.RESULT_VALID_KEY_CONFIRMED) {
                            restoreEntries(outputStreamToString(os));
                        } else {
                            Toast.makeText(this, R.string.backup_toast_openpgp_not_verified, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        restoreEntries(outputStreamToString(os));
                    }
                }
            }
        } else if (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR) == OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED) {
            PendingIntent pi = result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);

            // Small hack to keep the target file even after user interaction
            if (requestCode == Constants.INTENT_BACKUP_ENCRYPT_PGP) {
                encryptTargetFile = file;
            } else if (requestCode == Constants.INTENT_BACKUP_DECRYPT_PGP) {
                decryptSourceFile = file;
            }

            try {
                startIntentSenderForResult(pi.getIntentSender(), requestCode, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else if (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR) == OpenPgpApi.RESULT_CODE_ERROR) {
            OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
            Toast.makeText(this, String.format(getString(R.string.backup_toast_openpgp_error), error.getMessage()), Toast.LENGTH_LONG).show();
        }
    }
}
