package org.shadowice.flocke.andotp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.Toast;

public class BackupActivity extends AppCompatActivity {
    private final static int INTENT_OPEN_DOCUMENT_PLAIN = 100;
    private final static int INTENT_SAVE_DOCUMENT_PLAIN = 101;

    private final static int PERMISSIONS_REQUEST_READ_IMPORT_PLAIN = 110;
    private final static int PERMISSIONS_REQUEST_WRITE_EXPORT_PLAIN = 111;

    private static final String DEFAULT_BACKUP_FILENAME = "otp_accounts.json";
    private static final String DEFAULT_BACKUP_MIMETYPE = "application/json";

    private boolean reload = false;

    LinearLayout exportPlain;
    LinearLayout importPlain;

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

        exportPlain = (LinearLayout) v.findViewById(R.id.button_export_plain);
        importPlain = (LinearLayout) v.findViewById(R.id.button_import_plain);

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
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Get the result from external activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == INTENT_OPEN_DOCUMENT_PLAIN && resultCode == Activity.RESULT_OK) {
            Uri file;
            if (intent != null) {
                file = intent.getData();
                doImportJSON(file);
            }
        } else if (requestCode == INTENT_SAVE_DOCUMENT_PLAIN && resultCode == Activity.RESULT_OK) {
            Uri file;
            if (intent != null) {
                file = intent.getData();
                doExportJSON(file);
            }
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

    private void exportJSONWithPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            exportJSONWithSelector();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXPORT_PLAIN);
        }
    }

    private void exportJSONWithSelector() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(DEFAULT_BACKUP_MIMETYPE);
        intent.putExtra(Intent.EXTRA_TITLE, DEFAULT_BACKUP_FILENAME);
        startActivityForResult(intent, INTENT_SAVE_DOCUMENT_PLAIN);
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
}
