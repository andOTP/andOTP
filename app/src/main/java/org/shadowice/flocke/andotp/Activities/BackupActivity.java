/*
 * Copyright (C) 2017 Jakob Nixdorf
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

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.OpenPgpSignatureResult;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;
import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.DatabaseHelper;
import org.shadowice.flocke.andotp.Utilities.EncryptionHelper;
import org.shadowice.flocke.andotp.Utilities.FileHelper;
import org.shadowice.flocke.andotp.Utilities.Tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.crypto.SecretKey;

public class BackupActivity extends BaseActivity {
    private final static int INTENT_OPEN_DOCUMENT_PLAIN = 200;
    private final static int INTENT_SAVE_DOCUMENT_PLAIN = 201;
    private final static int INTENT_OPEN_DOCUMENT_CRYPT = 202;
    private final static int INTENT_SAVE_DOCUMENT_CRYPT = 203;
    private final static int INTENT_OPEN_DOCUMENT_PGP   = 204;
    private final static int INTENT_SAVE_DOCUMENT_PGP   = 205;
    private final static int INTENT_ENCRYPT_PGP         = 206;
    private final static int INTENT_DECRYPT_PGP         = 207;

    private final static int PERMISSIONS_REQUEST_READ_IMPORT_PLAIN  = 210;
    private final static int PERMISSIONS_REQUEST_WRITE_EXPORT_PLAIN = 211;
    private final static int PERMISSIONS_REQUEST_READ_IMPORT_CRYPT  = 212;
    private final static int PERMISSIONS_REQUEST_WRITE_EXPORT_CRYPT = 213;
    private final static int PERMISSIONS_REQUEST_READ_IMPORT_PGP    = 214;
    private final static int PERMISSIONS_REQUEST_WRITE_EXPORT_PGP   = 215;

    private static final String DEFAULT_BACKUP_FILENAME_PLAIN   = "otp_accounts.json";
    private static final String DEFAULT_BACKUP_FILENAME_CRYPT   = "otp_accounts.json.aes";
    private static final String DEFAULT_BACKUP_FILENAME_PGP     = "otp_accounts.json.gpg";
    private static final String DEFAULT_BACKUP_MIMETYPE_PLAIN   = "application/json";
    private static final String DEFAULT_BACKUP_MIMETYPE_CRYPT   = "binary/aes";
    private static final String DEFAULT_BACKUP_MIMETYPE_PGP     = "application/pgp-encrypted";

    private OpenPgpServiceConnection pgpServiceConnection;
    private long pgpKeyId;

    private Uri encryptTargetFile;
    private Uri decryptSourceFile;

    private Switch replace;

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

        // Plain-text

        LinearLayout backupPlain = v.findViewById(R.id.button_backup_plain);
        LinearLayout restorePlain = v.findViewById(R.id.button_restore_plain);

        backupPlain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backupPlainWithWarning();
            }
        });

        restorePlain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileWithPermissions(INTENT_OPEN_DOCUMENT_PLAIN, PERMISSIONS_REQUEST_READ_IMPORT_PLAIN);
            }
        });

        // Password

        TextView cryptSetup = v.findViewById(R.id.msg_crypt_setup);
        LinearLayout backupCrypt = v.findViewById(R.id.button_backup_crypt);
        LinearLayout restoreCrypt = v.findViewById(R.id.button_restore_crypt);

        if (settings.getBackupPasswordEnc().isEmpty()) {
            cryptSetup.setVisibility(View.VISIBLE);
            backupCrypt.setVisibility(View.GONE);
            restoreCrypt.setVisibility(View.GONE);
        } else {
            cryptSetup.setVisibility(View.GONE);
            backupCrypt.setVisibility(View.VISIBLE);
            restoreCrypt.setVisibility(View.VISIBLE);
        }

        backupCrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFileWithPermissions(DEFAULT_BACKUP_MIMETYPE_CRYPT, DEFAULT_BACKUP_FILENAME_CRYPT, INTENT_SAVE_DOCUMENT_CRYPT, PERMISSIONS_REQUEST_WRITE_EXPORT_CRYPT);
            }
        });

        restoreCrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileWithPermissions(INTENT_OPEN_DOCUMENT_CRYPT, PERMISSIONS_REQUEST_READ_IMPORT_CRYPT);
            }
        });

        // OpenPGP

        String PGPProvider = settings.getOpenPGPProvider();
        pgpKeyId = settings.getOpenPGPKey();

        TextView setupPGP = v.findViewById(R.id.msg_openpgp_setup);
        LinearLayout backupPGP = v.findViewById(R.id.button_backup_openpgp);
        LinearLayout restorePGP = v.findViewById(R.id.button_restore_openpgp);

        if (TextUtils.isEmpty(PGPProvider)) {
            setupPGP.setVisibility(View.VISIBLE);
            backupPGP.setVisibility(View.GONE);
            restorePGP.setVisibility(View.GONE);
        } else if (pgpKeyId == 0){
            setupPGP.setVisibility(View.VISIBLE);
            setupPGP.setText(R.string.backup_desc_openpgp_keyid);
            backupPGP.setVisibility(View.GONE);
        } else {
            pgpServiceConnection = new OpenPgpServiceConnection(BackupActivity.this.getApplicationContext(), PGPProvider);
            pgpServiceConnection.bindToService();

            backupPGP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveFileWithPermissions(DEFAULT_BACKUP_MIMETYPE_PGP, DEFAULT_BACKUP_FILENAME_PGP, INTENT_SAVE_DOCUMENT_PGP, PERMISSIONS_REQUEST_WRITE_EXPORT_PGP);
                }
            });

            restorePGP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openFileWithPermissions(INTENT_OPEN_DOCUMENT_PGP, PERMISSIONS_REQUEST_READ_IMPORT_PGP);
                }
            });
        }

        replace = v.findViewById(R.id.backup_replace);

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

    // Get the result from permission requests
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_IMPORT_PLAIN) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showOpenFileSelector(INTENT_OPEN_DOCUMENT_PLAIN);
            } else {
                Toast.makeText(this, R.string.backup_toast_storage_permissions, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_WRITE_EXPORT_PLAIN) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSaveFileSelector(DEFAULT_BACKUP_MIMETYPE_PLAIN, DEFAULT_BACKUP_FILENAME_PLAIN, INTENT_SAVE_DOCUMENT_PLAIN);
            } else {
                Toast.makeText(this, R.string.backup_toast_storage_permissions, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_READ_IMPORT_CRYPT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showOpenFileSelector(INTENT_OPEN_DOCUMENT_CRYPT);
            } else {
                Toast.makeText(this, R.string.backup_toast_storage_permissions, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_WRITE_EXPORT_CRYPT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSaveFileSelector(DEFAULT_BACKUP_MIMETYPE_CRYPT, DEFAULT_BACKUP_FILENAME_CRYPT, INTENT_SAVE_DOCUMENT_CRYPT);
            } else {
                Toast.makeText(this, R.string.backup_toast_storage_permissions, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_READ_IMPORT_PGP) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showOpenFileSelector(INTENT_OPEN_DOCUMENT_PGP);
            } else {
                Toast.makeText(this, R.string.backup_toast_storage_permissions, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_WRITE_EXPORT_PGP) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSaveFileSelector(DEFAULT_BACKUP_MIMETYPE_PGP, DEFAULT_BACKUP_FILENAME_PGP, INTENT_SAVE_DOCUMENT_PGP);
            } else {
                Toast.makeText(this, R.string.backup_toast_storage_permissions, Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Get the result from external activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == INTENT_OPEN_DOCUMENT_PLAIN && resultCode == RESULT_OK) {
            if (intent != null) {
                doRestorePlain(intent.getData());
            }
        } else if (requestCode == INTENT_SAVE_DOCUMENT_PLAIN && resultCode == RESULT_OK) {
            if (intent != null) {
                doBackupPlain(intent.getData());
            }
        } else if (requestCode == INTENT_OPEN_DOCUMENT_CRYPT && resultCode == RESULT_OK) {
            if (intent != null) {
                doRestoreCrypt(intent.getData());
            }
        } else if (requestCode == INTENT_SAVE_DOCUMENT_CRYPT && resultCode == RESULT_OK) {
            if (intent != null) {
                doBackupCrypt(intent.getData());
            }
        } else if (requestCode == INTENT_OPEN_DOCUMENT_PGP && resultCode == RESULT_OK) {
            if (intent != null)
                restoreEncryptedWithPGP(intent.getData(), null);
        } else if (requestCode == INTENT_SAVE_DOCUMENT_PGP && resultCode == RESULT_OK) {
            if (intent != null)
                backupEncryptedWithPGP(intent.getData(), null);
        } else if (requestCode == INTENT_ENCRYPT_PGP && resultCode == RESULT_OK) {
            backupEncryptedWithPGP(encryptTargetFile, intent);
        } else if (requestCode == INTENT_DECRYPT_PGP && resultCode == RESULT_OK) {
            restoreEncryptedWithPGP(decryptSourceFile, intent);
        }
    }

    /* Generic functions for all backup/restore options */

    private void showOpenFileSelector(int intentId) {
        if (settings.getBackupAsk()) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, intentId);
        } else {
            if (intentId == INTENT_OPEN_DOCUMENT_PLAIN)
                doRestorePlain(Tools.buildUri(settings.getBackupDir(), DEFAULT_BACKUP_FILENAME_PLAIN));
            else if (intentId == INTENT_OPEN_DOCUMENT_CRYPT)
                doRestoreCrypt(Tools.buildUri(settings.getBackupDir(), DEFAULT_BACKUP_FILENAME_CRYPT));
            else if (intentId == INTENT_OPEN_DOCUMENT_PGP)
                restoreEncryptedWithPGP(Tools.buildUri(settings.getBackupDir(), DEFAULT_BACKUP_FILENAME_PGP), null);
        }
    }

    private void showSaveFileSelector(String mimeType, String fileName, int intentId) {
        if (settings.getBackupAsk()) {
            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_TITLE, fileName);
            startActivityForResult(intent, intentId);
        } else {
            if (Tools.mkdir(settings.getBackupDir())) {
                if (intentId == INTENT_SAVE_DOCUMENT_PLAIN)
                    doBackupPlain(Tools.buildUri(settings.getBackupDir(), DEFAULT_BACKUP_FILENAME_PLAIN));
                else if (intentId == INTENT_SAVE_DOCUMENT_CRYPT)
                    doBackupCrypt(Tools.buildUri(settings.getBackupDir(), DEFAULT_BACKUP_FILENAME_CRYPT));
                else if (intentId == INTENT_SAVE_DOCUMENT_PGP)
                    backupEncryptedWithPGP(Tools.buildUri(settings.getBackupDir(), DEFAULT_BACKUP_FILENAME_PGP), null);
            } else {
                Toast.makeText(this, R.string.backup_toast_mkdir_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void openFileWithPermissions(int intentId, int requestId) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            showOpenFileSelector(intentId);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, requestId);
        }
    }

    private void saveFileWithPermissions(String mimeType, String fileName, int intentId, int requestId) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            showSaveFileSelector(mimeType, fileName, intentId);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestId);
        }
    }

    /* Plain-text backup functions */

    private void doRestorePlain(Uri uri) {
        if (Tools.isExternalStorageReadable()) {
            ArrayList<Entry> entries = DatabaseHelper.importFromJSON(this, uri);

            if (entries != null) {
                if (! replace.isChecked()) {
                    ArrayList<Entry> currentEntries = DatabaseHelper.loadDatabase(this);
                    entries.removeAll(currentEntries);
                    entries.addAll(currentEntries);
                }

                DatabaseHelper.saveDatabase(this, entries);
                reload = true;

                Toast.makeText(this, R.string.backup_toast_import_success, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.backup_toast_import_failed, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.backup_toast_storage_not_accessible, Toast.LENGTH_LONG).show();
        }

        finishWithResult();
    }

    private void doBackupPlain(Uri uri) {
        if (Tools.isExternalStorageWritable()) {
            boolean success = DatabaseHelper.exportAsJSON(this, uri);

            if (success)
                Toast.makeText(this, R.string.backup_toast_export_success, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, R.string.backup_toast_export_failed, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.backup_toast_storage_not_accessible, Toast.LENGTH_LONG).show();
        }

        finishWithResult();
    }

    private void backupPlainWithWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.backup_dialog_title_security_warning)
                .setMessage(R.string.backup_dialog_msg_export_warning)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveFileWithPermissions(DEFAULT_BACKUP_MIMETYPE_PLAIN, DEFAULT_BACKUP_FILENAME_PLAIN, INTENT_SAVE_DOCUMENT_PLAIN, PERMISSIONS_REQUEST_WRITE_EXPORT_PLAIN);
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

    private void doRestoreCrypt(Uri uri) {
        String password = settings.getBackupPasswordEnc();

        if (! password.isEmpty()) {
            if (Tools.isExternalStorageReadable()) {
                boolean success = true;

                try {
                    byte[] encrypted = FileHelper.readFileToBytes(this, uri);

                    SecretKey key = EncryptionHelper.generateSymmetricKeyFromPassword(password);
                    byte[] decrypted = EncryptionHelper.decrypt(key, encrypted);

                    ArrayList<Entry> entries = DatabaseHelper.stringToEntries(this, new String(decrypted, StandardCharsets.UTF_8));

                    if (! replace.isChecked()) {
                        ArrayList<Entry> currentEntries = DatabaseHelper.loadDatabase(this);
                        entries.removeAll(currentEntries);
                        entries.addAll(currentEntries);
                    }

                    DatabaseHelper.saveDatabase(this, entries);
                } catch (Exception e) {
                    e.printStackTrace();
                    success = false;
                }

                if (success) {
                    reload = true;
                    Toast.makeText(this, R.string.backup_toast_import_success, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, R.string.backup_toast_import_failed, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, R.string.backup_toast_storage_not_accessible, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.backup_toast_crypt_password_not_set, Toast.LENGTH_LONG).show();
        }

        finishWithResult();
    }

    private void doBackupCrypt(Uri uri) {
        String password = settings.getBackupPasswordEnc();

        if (! password.isEmpty()) {
            if (Tools.isExternalStorageWritable()) {
                ArrayList<Entry> entries = DatabaseHelper.loadDatabase(this);
                String plain = DatabaseHelper.entriesToString(entries);

                boolean success = true;

                try {
                    SecretKey key = EncryptionHelper.generateSymmetricKeyFromPassword(password);
                    byte[] encrypted = EncryptionHelper.encrypt(key, plain.getBytes(StandardCharsets.UTF_8));

                    FileHelper.writeBytesToFile(this, uri, encrypted);
                } catch (Exception e) {
                    e.printStackTrace();
                    success = false;
                }

                if (success) {
                    Toast.makeText(this, R.string.backup_toast_export_success, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, R.string.backup_toast_export_failed, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, R.string.backup_toast_storage_not_accessible, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.backup_toast_crypt_password_not_set, Toast.LENGTH_LONG).show();
        }

        finishWithResult();
    }

    /* OpenPGP backup functions */

    private void doRestoreEncrypted(String content) {
        if (Tools.isExternalStorageReadable()) {
            ArrayList<Entry> entries = DatabaseHelper.stringToEntries(this, content);

            if (entries.size() > 0) {
                if (! replace.isChecked()) {
                    ArrayList<Entry> currentEntries = DatabaseHelper.loadDatabase(this);
                    entries.removeAll(currentEntries);
                    entries.addAll(currentEntries);
                }

                DatabaseHelper.saveDatabase(this, entries);
                reload = true;

                Toast.makeText(this, R.string.backup_toast_import_success, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.backup_toast_import_failed, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.backup_toast_storage_not_accessible, Toast.LENGTH_LONG).show();
        }

        finishWithResult();
    }

    private void restoreEncryptedWithPGP(Uri uri, Intent decryptIntent) {
        if (decryptIntent == null)
            decryptIntent = new Intent(OpenPgpApi.ACTION_DECRYPT_VERIFY);

        String input = FileHelper.readFileToString(this, uri);
        Log.d("OpenPGP", input);

        InputStream is = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        OpenPgpApi api = new OpenPgpApi(this, pgpServiceConnection.getService());
        Intent result = api.executeApi(decryptIntent, is, os);
        handleOpenPGPResult(result, os, uri, INTENT_DECRYPT_PGP);
    }

    private void doBackupEncrypted(Uri uri, String data) {
        if (Tools.isExternalStorageWritable()) {
            boolean success = FileHelper.writeStringToFile(this, uri, data);

            if (success)
                Toast.makeText(this, R.string.backup_toast_export_success, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, R.string.backup_toast_export_failed, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.backup_toast_storage_not_accessible, Toast.LENGTH_LONG).show();
        }

        finishWithResult();
    }

    private void backupEncryptedWithPGP(Uri uri, Intent encryptIntent) {
        ArrayList<Entry> entries = DatabaseHelper.loadDatabase(this);
        String plainJSON = DatabaseHelper.entriesToString(entries);

        if (encryptIntent == null) {
            encryptIntent = new Intent();

            if (settings.getOpenPGPSign()) {
                encryptIntent.setAction(OpenPgpApi.ACTION_SIGN_AND_ENCRYPT);
                encryptIntent.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, pgpKeyId);
            } else {
                encryptIntent.setAction(OpenPgpApi.ACTION_ENCRYPT);
            }

            encryptIntent.putExtra(OpenPgpApi.EXTRA_KEY_IDS, new long[]{pgpKeyId});
            encryptIntent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
        }

        InputStream is = new ByteArrayInputStream(plainJSON.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        OpenPgpApi api = new OpenPgpApi(this, pgpServiceConnection.getService());
        Intent result = api.executeApi(encryptIntent, is, os);
        handleOpenPGPResult(result, os, uri, INTENT_ENCRYPT_PGP);
    }

    public String outputStreamToString(ByteArrayOutputStream os) {
        return new String(os.toByteArray(), StandardCharsets.UTF_8);
    }

    public void handleOpenPGPResult(Intent result, ByteArrayOutputStream os, Uri file, int requestCode) {
        if (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR) == OpenPgpApi.RESULT_CODE_SUCCESS) {
            if (requestCode == INTENT_ENCRYPT_PGP) {
                if (os != null)
                    doBackupEncrypted(file, outputStreamToString(os));
            } else if (requestCode == INTENT_DECRYPT_PGP) {
                if (os != null) {
                    if (settings.getOpenPGPVerify()) {
                        OpenPgpSignatureResult sigResult = result.getParcelableExtra(OpenPgpApi.RESULT_SIGNATURE);

                        if (sigResult.getResult() == OpenPgpSignatureResult.RESULT_VALID_KEY_CONFIRMED) {
                            doRestoreEncrypted(outputStreamToString(os));
                        } else {
                            Toast.makeText(this, R.string.backup_toast_openpgp_not_verified, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        doRestoreEncrypted(outputStreamToString(os));
                    }
                }
            }
        } else if (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR) == OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED) {
            PendingIntent pi = result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);

            // Small hack to keep the target file even after user interaction
            if (requestCode == INTENT_ENCRYPT_PGP) {
                encryptTargetFile = file;
            } else if (requestCode == INTENT_DECRYPT_PGP) {
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
