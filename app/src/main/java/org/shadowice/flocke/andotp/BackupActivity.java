package org.shadowice.flocke.andotp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.OpenPgpSignatureResult;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class BackupActivity extends AppCompatActivity {
    private final static int INTENT_OPEN_DOCUMENT_PLAIN = 100;
    private final static int INTENT_SAVE_DOCUMENT_PLAIN = 101;
    private final static int INTENT_OPEN_DOCUMENT_PGP = 102;
    private final static int INTENT_SAVE_DOCUMENT_PGP = 103;
    private final static int INTENT_ENCRYPT = 104;
    private final static int INTENT_DECRYPT = 105;

    private final static int PERMISSIONS_REQUEST_READ_IMPORT_PLAIN = 110;
    private final static int PERMISSIONS_REQUEST_WRITE_EXPORT_PLAIN = 111;
    private final static int PERMISSIONS_REQUEST_READ_IMPORT_PGP = 112;
    private final static int PERMISSIONS_REQUEST_WRITE_EXPORT_PGP = 113;

    private static final String DEFAULT_BACKUP_FILENAME_PLAIN = "otp_accounts.json";
    private static final String DEFAULT_BACKUP_FILENAME_PGP = "otp_accounts.json.gpg";
    private static final String DEFAULT_BACKUP_MIMETYPE_PLAIN = "application/json";
    private static final String DEFAULT_BACKUP_MIMETYPE_PGP = "application/pgp-encrypted";

    private SharedPreferences settings;

    private OpenPgpServiceConnection pgpServiceConnection;
    private long pgpKeyId;

    private Uri encryptTargetFile;
    private Uri decryptSourceFile;

    private boolean reload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.backup_activity_title);
        setContentView(R.layout.activity_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.container_toolbar);
        setSupportActionBar(toolbar);

        ViewStub stub = (ViewStub) findViewById(R.id.container_stub);
        stub.setLayoutResource(R.layout.content_backup);
        View v = stub.inflate();

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        LinearLayout exportPlain = (LinearLayout) v.findViewById(R.id.button_export_plain);
        LinearLayout importPlain = (LinearLayout) v.findViewById(R.id.button_import_plain);

        exportPlain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportJSONWithWarning();
            }
        });

        importPlain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                importJSONWithPermissions();
            }
        });

        String PGPProvider = settings.getString(getString(R.string.settings_key_openpgp_provider), "");
        pgpKeyId = settings.getLong(getString(R.string.settings_key_openpgp_keyid), 0);

        TextView setupPGP = (TextView) v.findViewById(R.id.msg_openpgp_setup);
        LinearLayout exportPGP = (LinearLayout) v.findViewById(R.id.button_export_openpgp);
        LinearLayout importPGP = (LinearLayout) v.findViewById(R.id.button_import_openpgp);

        if (TextUtils.isEmpty(PGPProvider)) {
            setupPGP.setVisibility(View.VISIBLE);
            exportPGP.setVisibility(View.GONE);
            importPGP.setVisibility(View.GONE);
        } else if (pgpKeyId == 0){
            setupPGP.setVisibility(View.VISIBLE);
            setupPGP.setText(R.string.backup_desc_openpgp_keyid);
            exportPGP.setVisibility(View.GONE);
        } else {
            pgpServiceConnection = new OpenPgpServiceConnection(BackupActivity.this.getApplicationContext(), PGPProvider);
            pgpServiceConnection.bindToService();

            exportPGP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    exportEncryptedWithPermissions();
                }
            });

            importPGP.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    importEncryptedWithPermissions();
                }
            });
        }

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
                importJSONWithSelector();
            } else {
                Toast.makeText(this, R.string.backup_toast_storage_permissions, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_WRITE_EXPORT_PLAIN) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportJSONWithSelector();
            } else {
                Toast.makeText(this, R.string.backup_toast_storage_permissions, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_READ_IMPORT_PGP) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                importEncryptedWithSelector();
            } else {
                Toast.makeText(this, R.string.backup_toast_storage_permissions, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_WRITE_EXPORT_PGP) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportEncryptedWithSelector();
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
                doImportJSON(intent.getData());
            }
        } else if (requestCode == INTENT_SAVE_DOCUMENT_PLAIN && resultCode == RESULT_OK) {
            if (intent != null) {
                doExportJSON(intent.getData());
            }
        } else if (requestCode == INTENT_OPEN_DOCUMENT_PGP && resultCode == RESULT_OK) {
            if (intent != null)
                importEncryptedWithPGP(intent.getData());
        } else if (requestCode == INTENT_SAVE_DOCUMENT_PGP && resultCode == RESULT_OK) {
            if (intent != null)
                exportEncryptedWithPGP(intent.getData());
        } else if (requestCode == INTENT_ENCRYPT && resultCode == RESULT_OK) {
            exportEncryptedWithPGP(encryptTargetFile);
        } else if (requestCode == INTENT_DECRYPT && resultCode == RESULT_OK) {
            importEncryptedWithPGP(decryptSourceFile);
        }
    }

    // Import from JSON
    private void doImportJSON(Uri uri) {
        if (StorageHelper.isExternalStorageReadable()) {
            boolean success = DatabaseHelper.importFromJSON(this, uri);

            if (success) {
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

    private void importJSONWithSelector() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, INTENT_OPEN_DOCUMENT_PLAIN);
    }

    private void importJSONWithPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            importJSONWithSelector();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_IMPORT_PLAIN);
        }
    }

    // Export to JSON
    private void doExportJSON(Uri uri) {
        if (StorageHelper.isExternalStorageWritable()) {
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

    private void exportJSONWithSelector() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(DEFAULT_BACKUP_MIMETYPE_PLAIN);
        intent.putExtra(Intent.EXTRA_TITLE, DEFAULT_BACKUP_FILENAME_PLAIN);
        startActivityForResult(intent, INTENT_SAVE_DOCUMENT_PLAIN);
    }

    private void exportJSONWithPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            exportJSONWithSelector();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXPORT_PLAIN);
        }
    }

    private void exportJSONWithWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.backup_dialog_title_security_warning)
                .setMessage(R.string.backup_dialog_msg_export_warning)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        exportJSONWithPermissions();
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

    // Import Encrypted
    private void doImportEncrypted(String content) {
        if (StorageHelper.isExternalStorageReadable()) {
            ArrayList<Entry> entries = DatabaseHelper.stringToEntries(this, content);

            if (entries.size() > 0) {
                DatabaseHelper.store(this, entries);

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

    private void importEncryptedWithPGP(Uri uri) {
        Intent decryptIntent = new Intent(OpenPgpApi.ACTION_DECRYPT_VERIFY);

        String input = FileHelper.readFileToString(this, uri);
        Log.d("OpenPGP", input);

        InputStream is = null;
        try {
            is = new ByteArrayInputStream(input.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        OpenPgpApi api = new OpenPgpApi(this, pgpServiceConnection.getService());
        Intent result = api.executeApi(decryptIntent, is, os);
        handleOpenPGPResult(result, os, uri, INTENT_DECRYPT);
    }

    private void importEncryptedWithSelector() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, INTENT_OPEN_DOCUMENT_PGP);
    }

    private void importEncryptedWithPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            importEncryptedWithSelector();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_IMPORT_PGP);
        }
    }

    // Export Encrypted
    private void doExportEncrypted(Uri uri, String data) {
        if (StorageHelper.isExternalStorageWritable()) {
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

    private void exportEncryptedWithPGP(Uri uri) {
        String plainJSON = DatabaseHelper.entriesToString(this);

        Intent encryptIntent = new Intent();

        if (settings.getBoolean(getString(R.string.settings_key_openpgp_sign), false)) {
            encryptIntent.setAction(OpenPgpApi.ACTION_SIGN_AND_ENCRYPT);
            encryptIntent.putExtra(OpenPgpApi.EXTRA_SIGN_KEY_ID, pgpKeyId);
        } else {
            encryptIntent.setAction(OpenPgpApi.ACTION_ENCRYPT);
        }

        encryptIntent.putExtra(OpenPgpApi.EXTRA_KEY_IDS, new long[]{pgpKeyId});
        encryptIntent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);

        InputStream is = null;
        try {
            is = new ByteArrayInputStream(plainJSON.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        OpenPgpApi api = new OpenPgpApi(this, pgpServiceConnection.getService());
        Intent result = api.executeApi(encryptIntent, is, os);
        handleOpenPGPResult(result, os, uri, INTENT_ENCRYPT);
    }

    private void exportEncryptedWithSelector() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(DEFAULT_BACKUP_MIMETYPE_PGP);
        intent.putExtra(Intent.EXTRA_TITLE, DEFAULT_BACKUP_FILENAME_PGP);
        startActivityForResult(intent, INTENT_SAVE_DOCUMENT_PGP);
    }

    private void exportEncryptedWithPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            exportEncryptedWithSelector();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXPORT_PGP);
        }
    }

    public String outputStreamToString(ByteArrayOutputStream os) {
        String string = "";
        try {
            string = os.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return string;
    }

    public void handleOpenPGPResult(Intent result, ByteArrayOutputStream os, Uri file, int requestCode) {
        if (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR) == OpenPgpApi.RESULT_CODE_SUCCESS) {
            if (requestCode == INTENT_ENCRYPT) {
                if (os != null)
                    doExportEncrypted(file, outputStreamToString(os));
            } else if (requestCode == INTENT_DECRYPT) {
                if (os != null) {
                    if (settings.getBoolean(getString(R.string.settings_key_openpgp_verify), false)) {
                        OpenPgpSignatureResult sigResult = result.getParcelableExtra(OpenPgpApi.RESULT_SIGNATURE);

                        if (sigResult.getResult() == OpenPgpSignatureResult.RESULT_VALID_KEY_CONFIRMED) {
                            doImportEncrypted(outputStreamToString(os));
                        } else {
                            Toast.makeText(this, R.string.backup_toast_openpgp_not_verified, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        doImportEncrypted(outputStreamToString(os));
                    }
                }
            }
        } else if (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR) == OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED) {
            PendingIntent pi = result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);

            // Small hack to keep the target file even after user interaction
            if (requestCode == INTENT_ENCRYPT) {
                encryptTargetFile = file;
            } else if (requestCode == INTENT_DECRYPT) {
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
