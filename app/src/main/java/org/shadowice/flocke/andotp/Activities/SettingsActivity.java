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

import android.app.backup.BackupManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import androidx.appcompat.widget.Toolbar;

import android.provider.DocumentsContract;
import android.view.ViewStub;
import android.widget.Toast;

import org.openintents.openpgp.util.OpenPgpAppPreference;
import org.openintents.openpgp.util.OpenPgpKeyPreference;
import org.shadowice.flocke.andotp.Database.Entry;
import org.shadowice.flocke.andotp.Preferences.CredentialsPreference;
import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.BackupHelper;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.DatabaseHelper;
import org.shadowice.flocke.andotp.Utilities.EncryptionHelper;
import org.shadowice.flocke.andotp.Utilities.KeyStoreHelper;
import org.shadowice.flocke.andotp.Utilities.Settings;
import org.shadowice.flocke.andotp.Utilities.UIHelper;

import java.util.ArrayList;
import java.util.Locale;

import javax.crypto.SecretKey;

import static org.shadowice.flocke.andotp.Utilities.Constants.AuthMethod;
import static org.shadowice.flocke.andotp.Utilities.Constants.EncryptionType;

public class SettingsActivity extends BaseActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{
    SettingsFragment fragment;

    SecretKey encryptionKey = null;
    boolean encryptionChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.settings_activity_title);
        setContentView(R.layout.activity_container);

        Toolbar toolbar = findViewById(R.id.container_toolbar);
        setSupportActionBar(toolbar);

        ViewStub stub = findViewById(R.id.container_stub);
        stub.inflate();

        Intent callingIntent = getIntent();
        byte[] keyMaterial = callingIntent.getByteArrayExtra(Constants.EXTRA_SETTINGS_ENCRYPTION_KEY);
        if (keyMaterial != null && keyMaterial.length > 0)
            encryptionKey = EncryptionHelper.generateSymmetricKey(keyMaterial);

        if (savedInstanceState != null) {
            encryptionChanged = savedInstanceState.getBoolean(Constants.EXTRA_SETTINGS_ENCRYPTION_CHANGED, false);

            byte[] encKey = savedInstanceState.getByteArray(Constants.EXTRA_SETTINGS_ENCRYPTION_KEY);
            if (encKey != null) {
                encryptionKey = EncryptionHelper.generateSymmetricKey(encKey);
            }
        }

        fragment = new SettingsFragment();

        getFragmentManager().beginTransaction()
                .replace(R.id.container_content, fragment)
                .commit();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(Constants.EXTRA_SETTINGS_ENCRYPTION_CHANGED, encryptionChanged);
        outState.putByteArray(Constants.EXTRA_SETTINGS_ENCRYPTION_KEY, encryptionKey.getEncoded());
    }

    public void finishWithResult() {
        Intent data = new Intent();

        data.putExtra(Constants.EXTRA_SETTINGS_ENCRYPTION_CHANGED, encryptionChanged);
        if (encryptionKey != null)
            data.putExtra(Constants.EXTRA_SETTINGS_ENCRYPTION_KEY, encryptionKey.getEncoded());

        setResult(RESULT_OK, data);
        finish();
    }

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

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        BackupManager backupManager = new BackupManager(this);
        backupManager.dataChanged();

        if (key.equals(getString(R.string.settings_key_theme)) ||
                key.equals(getString(R.string.settings_key_locale)) ||
                key.equals(getString(R.string.settings_key_special_features)) ||
                key.equals(getString(R.string.settings_key_backup_location))) {
            recreate();
        } else if(key.equals(getString(R.string.settings_key_encryption))) {
            if (settings.getEncryption() != EncryptionType.PASSWORD) {
                if (settings.getAndroidBackupServiceEnabled()) {
                    UIHelper.showGenericDialog(this,
                        R.string.settings_dialog_title_android_sync,
                        R.string.settings_dialog_msg_android_sync_disabled_encryption
                    );
                }

                settings.setAndroidBackupServiceEnabled(false);
                if (fragment.useAndroidSync != null) {
                    fragment.useAndroidSync.setEnabled(false);
                    fragment.useAndroidSync.setChecked(false);
                }
            } else {
                if (fragment.useAndroidSync != null)
                    fragment.useAndroidSync.setEnabled(true);
            }
        }

        if (fragment.useAutoBackup != null) {
            fragment.useAutoBackup.setEnabled(BackupHelper.autoBackupType(this) == Constants.BackupType.ENCRYPTED);
            if (!fragment.useAutoBackup.isEnabled())
                fragment.useAutoBackup.setValue(Constants.AutoBackup.OFF.toString().toLowerCase(Locale.ENGLISH));
        }
    }

    private void generateNewEncryptionKey() {
        if (settings.getEncryption() == EncryptionType.KEYSTORE) {
            encryptionKey = KeyStoreHelper.loadEncryptionKeyFromKeyStore(this, false);
            encryptionChanged = true;
        }
    }

    private void tryEncryptionChangeWithAuth(EncryptionType newEnc) {
        Intent authIntent = new Intent(this, AuthenticateActivity.class);
        authIntent.putExtra(Constants.EXTRA_AUTH_NEW_ENCRYPTION, newEnc.name());
        authIntent.putExtra(Constants.EXTRA_AUTH_MESSAGE, R.string.auth_msg_confirm_encryption);
        startActivityForResult(authIntent, Constants.INTENT_SETTINGS_AUTHENTICATE);
    }

    private boolean tryEncryptionChange(EncryptionType newEnc, byte[] newKey) {
        Toast upgrading = Toast.makeText(this, R.string.settings_toast_encryption_changing, Toast.LENGTH_LONG);
        upgrading.show();

        if (DatabaseHelper.backupDatabase(this)) {
            ArrayList<Entry> entries;

            if (encryptionKey != null)
                entries = DatabaseHelper.loadDatabase(this, encryptionKey);
            else
                entries = new ArrayList<>();

            SecretKey newEncryptionKey;

            if (newEnc == EncryptionType.KEYSTORE) {
                newEncryptionKey = KeyStoreHelper.loadEncryptionKeyFromKeyStore(this, true);
            } else if (newKey != null && newKey.length > 0) {
                newEncryptionKey = EncryptionHelper.generateSymmetricKey(newKey);
            } else {
                upgrading.cancel();
                DatabaseHelper.restoreDatabaseBackup(this);
                return false;
            }

            if (DatabaseHelper.saveDatabase(this, entries, newEncryptionKey)) {
                encryptionKey = newEncryptionKey;
                encryptionChanged = true;

                fragment.encryption.setValue(newEnc.name().toLowerCase());

                upgrading.cancel();
                Toast.makeText(this, R.string.settings_toast_encryption_change_success, Toast.LENGTH_LONG).show();

                return true;
            }

            DatabaseHelper.restoreDatabaseBackup(this);

            upgrading.cancel();
            Toast.makeText(this, R.string.settings_toast_encryption_change_failed, Toast.LENGTH_LONG).show();
        } else {
            upgrading.cancel();
            Toast.makeText(this, R.string.settings_toast_encryption_backup_failed, Toast.LENGTH_LONG).show();
        }

        return false;
    }

    private void requestBackupAccess() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && settings.isBackupLocationSet())
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, settings.getBackupLocation());

        startActivityForResult(intent, Constants.INTENT_SETTINGS_BACKUP_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.INTENT_SETTINGS_AUTHENTICATE) {
            if (resultCode == RESULT_OK) {
                byte[] authKey = data.getByteArrayExtra(Constants.EXTRA_AUTH_PASSWORD_KEY);
                String newEnc = data.getStringExtra(Constants.EXTRA_AUTH_NEW_ENCRYPTION);

                if (authKey != null && authKey.length > 0 && newEnc != null && !newEnc.isEmpty()) {
                    EncryptionType newEncType = EncryptionType.valueOf(newEnc);
                    tryEncryptionChange(newEncType, authKey);
                } else {
                    Toast.makeText(this, R.string.settings_toast_encryption_no_key, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, R.string.settings_toast_encryption_auth_failed, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == Constants.INTENT_SETTINGS_BACKUP_LOCATION && resultCode == RESULT_OK) {
            Uri treeUri = data.getData();
            if (treeUri != null) {
                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(treeUri, takeFlags);
                settings.setBackupLocation(treeUri);
            }
        } else if (fragment.pgpSigningKey.handleOnActivityResult(requestCode, resultCode, data)) {
            // handled by OpenPgpKeyPreference
            return;
        }
    }

    public static class SettingsFragment extends PreferenceFragment {
        PreferenceCategory catSecurity;

        Settings settings;
        ListPreference encryption;
        Preference backupLocation;
        ListPreference useAutoBackup;
        CheckBoxPreference useAndroidSync;

        OpenPgpAppPreference pgpProvider;
        EditTextPreference pgpEncryptionKey;
        OpenPgpKeyPreference pgpSigningKey;

        public void encryptionChangeWithDialog(final EncryptionType encryptionType) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.settings_dialog_title_warning)
                    .setMessage(R.string.settings_dialog_msg_encryption_change)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (encryptionType == EncryptionType.PASSWORD)
                                ((SettingsActivity) getActivity()).tryEncryptionChangeWithAuth(encryptionType);
                            else if (encryptionType == EncryptionType.KEYSTORE)
                                ((SettingsActivity) getActivity()).tryEncryptionChange(encryptionType, null);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .create()
                    .show();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            settings = new Settings(getActivity());

            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

            addPreferencesFromResource(R.xml.preferences);

            CredentialsPreference credentialsPreference = (CredentialsPreference) findPreference(getString(R.string.settings_key_auth));
            credentialsPreference.setEncryptionChangeCallback(new CredentialsPreference.EncryptionChangeCallback() {
                @Override
                public boolean testEncryptionChange(byte[] newKey) {
                    return ((SettingsActivity) getActivity()).tryEncryptionChange(settings.getEncryption(), newKey);
                }
            });

            // Authentication
            catSecurity = (PreferenceCategory) findPreference(getString(R.string.settings_key_cat_security));
            encryption = (ListPreference) findPreference(getString(R.string.settings_key_encryption));

            encryption.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, Object o) {
                    String newEncryption = (String) o;
                    EncryptionType encryptionType = EncryptionType.valueOf(newEncryption.toUpperCase());
                    EncryptionType oldEncryptionType = settings.getEncryption();
                    AuthMethod authMethod = settings.getAuthMethod();

                    if (encryptionType != oldEncryptionType) {
                        if (encryptionType == EncryptionType.PASSWORD) {
                            if (authMethod != AuthMethod.PASSWORD && authMethod != AuthMethod.PIN) {
                                UIHelper.showGenericDialog(getActivity(), R.string.settings_dialog_title_error, R.string.settings_dialog_msg_encryption_invalid_with_auth);
                                return false;
                            } else {
                                if (settings.getAuthCredentials().isEmpty()) {
                                    UIHelper.showGenericDialog(getActivity(), R.string.settings_dialog_title_error, R.string.settings_dialog_msg_encryption_invalid_without_credentials);
                                    return false;
                                }
                            }

                            encryptionChangeWithDialog(EncryptionType.PASSWORD);
                        } else if (encryptionType == EncryptionType.KEYSTORE) {
                            encryptionChangeWithDialog(EncryptionType.KEYSTORE);
                        }
                    }

                    return false;
                }
            });

            // Backup location
            backupLocation = findPreference(getString(R.string.settings_key_backup_location));

            if (settings.isBackupLocationSet()) {
                backupLocation.setSummary(R.string.settings_desc_backup_location_set);
            } else {
                backupLocation.setSummary(R.string.settings_desc_backup_location);
            }

            backupLocation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ((SettingsActivity) getActivity()).requestBackupAccess();
                    return true;
                }
            });

            // OpenPGP
            pgpProvider = (OpenPgpAppPreference) findPreference(getString(R.string.settings_key_openpgp_provider));
            pgpEncryptionKey = (EditTextPreference) findPreference(getString(R.string.settings_key_openpgp_key_encrypt));
            pgpSigningKey = (OpenPgpKeyPreference) findPreference(getString(R.string.settings_key_openpgp_key_sign));

            pgpSigningKey.setOpenPgpProvider(pgpProvider.getValue());

            if (pgpProvider.getValue() != null && ! pgpProvider.getValue().isEmpty()) {
                pgpEncryptionKey.setEnabled(true);
            } else {
                pgpEncryptionKey.setEnabled(false);
            }

            pgpProvider.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue != null && ! ((String) newValue).isEmpty()) {
                        pgpEncryptionKey.setEnabled(true);
                    } else {
                        pgpEncryptionKey.setEnabled(false);
                    }

                    pgpSigningKey.setOpenPgpProvider((String) newValue);

                    return true;
                }
            });

            useAutoBackup = (ListPreference)findPreference(getString(R.string.settings_key_auto_backup_password_enc));
            useAutoBackup.setEnabled(BackupHelper.autoBackupType(getActivity()) == Constants.BackupType.ENCRYPTED);
            if(!useAutoBackup.isEnabled())
                useAutoBackup.setValue(Constants.AutoBackup.OFF.toString().toLowerCase(Locale.ENGLISH));

            useAndroidSync = (CheckBoxPreference) findPreference(getString(R.string.settings_key_enable_android_backup_service));
            useAndroidSync.setEnabled(settings.getEncryption() == EncryptionType.PASSWORD);
            if(!useAndroidSync.isEnabled())
                useAndroidSync.setChecked(false);

            if (sharedPref.contains(getString(R.string.settings_key_special_features)) &&
                    sharedPref.getBoolean(getString(R.string.settings_key_special_features), false)) {
                addPreferencesFromResource(R.xml.preferences_special);

                Preference clearKeyStore = findPreference(getString(R.string.settings_key_clear_keystore));
                clearKeyStore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setTitle(R.string.settings_dialog_title_clear_keystore);
                        if (settings.getEncryption() == EncryptionType.PASSWORD)
                            builder.setMessage(R.string.settings_dialog_msg_clear_keystore_password);
                        else if (settings.getEncryption() == EncryptionType.KEYSTORE)
                            builder.setMessage(R.string.settings_dialog_msg_clear_keystore_keystore);

                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                KeyStoreHelper.wipeKeys(getActivity());
                                if (settings.getEncryption() == EncryptionType.KEYSTORE) {
                                    DatabaseHelper.wipeDatabase(getActivity());
                                    ((SettingsActivity) getActivity()).generateNewEncryptionKey();
                                }
                            }
                        });
                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });

                        builder.create().show();

                        return false;
                    }
                });
            }
        }
    }
}
